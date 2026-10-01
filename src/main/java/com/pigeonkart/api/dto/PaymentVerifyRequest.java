package com.pigeonkart.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PaymentVerifyRequest {
    @NotNull private Long orderId; // our internal order id
    @NotBlank private String razorpay_payment_id;
    @NotBlank private String razorpay_order_id;
    @NotBlank private String razorpay_signature;
}
