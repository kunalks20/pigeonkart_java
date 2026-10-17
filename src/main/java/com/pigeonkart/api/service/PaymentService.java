package com.pigeonkart.api.service;

import com.pigeonkart.api.dto.PaymentOrderResponse;
import com.pigeonkart.api.dto.PaymentVerifyRequest;
import com.pigeonkart.api.dto.OrderRequest;
import com.pigeonkart.api.model.CustomerOrder;
import com.pigeonkart.api.model.OrderItem;
import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayException;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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

    public PaymentOrderResponse createPaymentOrder(OrderRequest request) throws Exception {
        CustomerOrder order = orderService.prepareOrder(request);
        long amountInPaise = order.getTotalAmount() * 100L;
        if (amountInPaise < 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment amount must be at least 100 paise");
        }

        String razorpayOrderId;
        if (mockMode) {
            razorpayOrderId = "order_mock_" + UUID.randomUUID().toString().substring(0, 12);
        } else {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            JSONObject options = new JSONObject();
            options.put("amount", amountInPaise);
            options.put("currency", "INR");
            options.put("receipt", UUID.randomUUID().toString());
            Order rpOrder;
            try {
                rpOrder = client.orders.create(options);
            } catch (RazorpayException ex) {
                HttpStatus status = ex.getStatusCode() == HttpStatus.UNAUTHORIZED.value()
                        ? HttpStatus.UNAUTHORIZED
                        : HttpStatus.INTERNAL_SERVER_ERROR;
                throw new ResponseStatusException(status, "Could not create Razorpay order", ex);
            }
            razorpayOrderId = rpOrder.get("id");
        }

        String checkoutToken = createCheckoutToken(order, razorpayOrderId);
        return new PaymentOrderResponse(razorpayOrderId, amountInPaise, "INR", keyId, checkoutToken);
    }

    public Long verify(PaymentVerifyRequest req) throws Exception {
        JSONObject checkout = readCheckoutToken(req.getCheckoutToken());
        if (!checkout.getString("razorpayOrderId").equals(req.getRazorpayOrderId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Razorpay order does not match this order");
        }

        boolean valid;
        if (mockMode) {
            // Demo mode: accept any payment id so the flow can be tested without keys.
            valid = req.getRazorpayPaymentId() != null && !req.getRazorpayPaymentId().isBlank();
        } else {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", req.getRazorpayOrderId());
            attributes.put("razorpay_payment_id", req.getRazorpayPaymentId());
            attributes.put("razorpay_signature", req.getRazorpaySignature());
            valid = Utils.verifyPaymentSignature(attributes, keySecret);
        }

        if (!valid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment signature verification failed");
        }

        int totalAmount = checkout.getInt("totalAmount");
        if (!mockMode) {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            Payment payment = client.payments.fetch(req.getRazorpayPaymentId());
            if (!req.getRazorpayOrderId().equals(payment.get("order_id"))
                    || !"captured".equalsIgnoreCase(payment.get("status"))
                    || !"INR".equalsIgnoreCase(payment.get("currency"))
                    || ((Number) payment.get("amount")).longValue() != totalAmount * 100L) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment was not captured for this order");
            }
        }

        var existingOrder = orderService.findByPaymentId(req.getRazorpayPaymentId());
        if (existingOrder.isPresent()) {
            return existingOrder.get().getId();
        }

        CustomerOrder order = orderFromCheckout(checkout);
        order.setRazorpayOrderId(req.getRazorpayOrderId());
        order.setRazorpayPaymentId(req.getRazorpayPaymentId());
        return orderService.savePaidOrder(order).getId();
    }

    private String createCheckoutToken(CustomerOrder order, String razorpayOrderId)
            throws GeneralSecurityException {
        JSONObject checkout = new JSONObject();
        checkout.put("razorpayOrderId", razorpayOrderId);
        checkout.put("expiresAt", Instant.now().plusSeconds(7200).getEpochSecond());
        checkout.put("customerName", order.getCustomerName());
        checkout.put("customerPhone", order.getCustomerPhone());
        checkout.put("customerAddress", order.getCustomerAddress());
        checkout.put("subtotalAmount", order.getSubtotalAmount());
        checkout.put("discountAmount", order.getDiscountAmount());
        checkout.put("totalAmount", order.getTotalAmount());
        JSONArray items = new JSONArray();
        for (OrderItem item : order.getItems()) {
            JSONObject itemData = new JSONObject();
            itemData.put("productId", item.getProductId());
            itemData.put("productName", item.getProductName());
            itemData.put("unitPrice", item.getUnitPrice());
            itemData.put("qty", item.getQty());
            items.put(itemData);
        }
        checkout.put("items", items);

        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(checkout.toString().getBytes(StandardCharsets.UTF_8));
        return payload + "." + sign(payload);
    }

    private JSONObject readCheckoutToken(String token) throws GeneralSecurityException {
        String[] parts = token.split("\\.", -1);
        if (parts.length != 2 || !MessageDigest.isEqual(
                sign(parts[0]).getBytes(StandardCharsets.US_ASCII),
                parts[1].getBytes(StandardCharsets.US_ASCII))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid checkout token");
        }

        try {
            JSONObject checkout = new JSONObject(new String(
                    Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8));
            if (checkout.getLong("expiresAt") <= Instant.now().getEpochSecond()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Checkout session has expired");
            }
            return checkout;
        } catch (org.json.JSONException | IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid checkout token", ex);
        }
    }

    private String sign(String payload) throws GeneralSecurityException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.US_ASCII)));
    }

    private CustomerOrder orderFromCheckout(JSONObject checkout) {
        CustomerOrder order = new CustomerOrder(
                checkout.getString("customerName"),
                checkout.getString("customerPhone"),
                checkout.getString("customerAddress"));
        order.setSubtotalAmount(checkout.getInt("subtotalAmount"));
        order.setDiscountAmount(checkout.getInt("discountAmount"));
        order.setTotalAmount(checkout.getInt("totalAmount"));
        JSONArray items = checkout.getJSONArray("items");
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            order.addItem(new OrderItem(
                    item.getLong("productId"),
                    item.getString("productName"),
                    item.getInt("unitPrice"),
                    item.getInt("qty")));
        }
        return order;
    }
}
