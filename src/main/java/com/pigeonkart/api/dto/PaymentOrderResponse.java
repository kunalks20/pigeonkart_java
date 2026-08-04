package com.pigeonkart.api.dto;

public class PaymentOrderResponse {
    private String razorpayOrderId;
    private long amount; // in paise
    private String currency;
    private String keyId;

    public PaymentOrderResponse(String razorpayOrderId, long amount, String currency, String keyId) {
        this.razorpayOrderId = razorpayOrderId;
        this.amount = amount;
        this.currency = currency;
        this.keyId = keyId;
    }

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public long getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getKeyId() { return keyId; }
}
