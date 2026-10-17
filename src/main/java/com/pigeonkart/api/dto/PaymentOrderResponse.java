package com.pigeonkart.api.dto;

public class PaymentOrderResponse {
    private String razorpayOrderId;
    private long amount; // in paise
    private String currency;
    private String keyId;
    private String checkoutToken;

    public PaymentOrderResponse(String razorpayOrderId, long amount, String currency, String keyId,
                                String checkoutToken) {
        this.razorpayOrderId = razorpayOrderId;
        this.amount = amount;
        this.currency = currency;
        this.keyId = keyId;
        this.checkoutToken = checkoutToken;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public long getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getKeyId() {
        return keyId;
    }

    public String getCheckoutToken() {
        return checkoutToken;
    }
}
