package com.pigeonkart.api.service;

import com.pigeonkart.api.dto.PaymentOrderResponse;
import com.pigeonkart.api.dto.PaymentVerifyRequest;
import com.pigeonkart.api.model.CustomerOrder;
import com.pigeonkart.api.model.OrderStatus;
import com.razorpay.Order;
import com.razorpay.RazorpayException;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderService orderService;

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    @Value("${razorpay.mock-mode}")
    private boolean mockMode;

    /**
     * Opens a Razorpay order for the given internal order id, restricted to UPI.
     * In mock mode (no real Razorpay account yet) this returns a fake order id
     * so the frontend flow can be built/demoed end to end.
     */
    @Transactional
    public PaymentOrderResponse createPaymentOrder(Long orderId) throws Exception {
        CustomerOrder order = orderService.getOrderForUpdate(orderId);
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order is not awaiting payment");
        }

        long amountInPaise = order.getTotalAmount() * 100L;
        if (amountInPaise < 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment amount must be at least 100 paise");
        }

        if (order.getRazorpayOrderId() != null && !order.getRazorpayOrderId().isBlank()) {
            return new PaymentOrderResponse(order.getRazorpayOrderId(), amountInPaise, "INR", keyId);
        }

        if (mockMode) {
            String fakeRazorpayOrderId = "order_mock_" + UUID.randomUUID().toString().substring(0, 12);
            order.setRazorpayOrderId(fakeRazorpayOrderId);
            return new PaymentOrderResponse(fakeRazorpayOrderId, amountInPaise, "INR", keyId);
        }

        RazorpayClient client = new RazorpayClient(keyId, keySecret);
        JSONObject options = new JSONObject();
        options.put("amount", amountInPaise);
        options.put("currency", "INR");
        options.put("receipt", order.getId().toString());
        // Method restriction to UPI is applied on the frontend checkout config
        // (see method: { upi: true } in Checkout.jsx); Razorpay orders themselves
        // are method-agnostic.
        Order rpOrder;
        try {
            rpOrder = client.orders.create(options);
        } catch (RazorpayException ex) {
            HttpStatus status = ex.getStatusCode() == HttpStatus.UNAUTHORIZED.value()
                    ? HttpStatus.UNAUTHORIZED
                    : HttpStatus.INTERNAL_SERVER_ERROR;
            throw new ResponseStatusException(status, "Could not create Razorpay order", ex);
        }

        order.setRazorpayOrderId(rpOrder.get("id"));
        return new PaymentOrderResponse(rpOrder.get("id"), amountInPaise, "INR", keyId);
    }

    /**
     * Verifies the payment signature Razorpay's checkout.js returns, then marks
     * the order paid and decrements stock. Throws if verification fails.
     */
    @Transactional
    public void verify(PaymentVerifyRequest req) throws Exception {
        CustomerOrder order = orderService.getOrderForUpdate(req.getOrderId());
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order is not awaiting payment");
        }
        if (order.getRazorpayOrderId() == null
                || !order.getRazorpayOrderId().equals(req.getRazorpay_order_id())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Razorpay order does not match this order");
        }

        boolean valid;
        if (mockMode) {
            // Demo mode: accept any payment id so the flow can be tested without keys.
            valid = req.getRazorpay_payment_id() != null && !req.getRazorpay_payment_id().isBlank();
        } else {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", req.getRazorpay_order_id());
            attributes.put("razorpay_payment_id", req.getRazorpay_payment_id());
            attributes.put("razorpay_signature", req.getRazorpay_signature());
            valid = Utils.verifyPaymentSignature(attributes, keySecret);
        }

        if (!valid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment signature verification failed");
        }

        order.setRazorpayPaymentId(req.getRazorpay_payment_id());
        order.setStatus(OrderStatus.PAID);
        orderService.applyStockAndSave(order);
    }
}
