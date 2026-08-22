package com.pigeonkart.api.service;

import com.pigeonkart.api.dto.PaymentOrderResponse;
import com.pigeonkart.api.dto.PaymentVerifyRequest;
import com.pigeonkart.api.model.CustomerOrder;
import com.pigeonkart.api.model.OrderStatus;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
    public PaymentOrderResponse createPaymentOrder(String orderId) throws Exception {
        CustomerOrder order = orderService.getOrder(orderId);
        long amountInPaise = order.getTotalAmount() * 100L;

        if (mockMode) {
            String fakeRazorpayOrderId = "order_mock_" + UUID.randomUUID().toString().substring(0, 12);
            order.setRazorpayOrderId(fakeRazorpayOrderId);
            return new PaymentOrderResponse(fakeRazorpayOrderId, amountInPaise, "INR", keyId);
        }

        RazorpayClient client = new RazorpayClient(keyId, keySecret);
        JSONObject options = new JSONObject();
        options.put("amount", amountInPaise);
        options.put("currency", "INR");
        options.put("receipt", order.getId());
        // Method restriction to UPI is applied on the frontend checkout config
        // (see method: { upi: true } in Checkout.jsx); Razorpay orders themselves
        // are method-agnostic.
        Order rpOrder = client.orders.create(options);

        order.setRazorpayOrderId(rpOrder.get("id"));
        return new PaymentOrderResponse(rpOrder.get("id"), amountInPaise, "INR", keyId);
    }

    /**
     * Verifies the payment signature Razorpay's checkout.js returns, then marks
     * the order paid and decrements stock. Throws if verification fails.
     */
    public void verify(PaymentVerifyRequest req) throws Exception {
        CustomerOrder order = orderService.getOrder(req.getOrderId());

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
            order.setStatus(OrderStatus.FAILED);
            throw new SecurityException("Payment signature verification failed for order " + order.getId());
        }

        order.setRazorpayPaymentId(req.getRazorpay_payment_id());
        order.setStatus(OrderStatus.PAID);
        orderService.applyStockAndSave(order);
    }
}
