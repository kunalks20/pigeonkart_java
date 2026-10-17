package com.pigeonkart.api.controller;

import com.pigeonkart.api.dto.OrderRequest;
import com.pigeonkart.api.dto.PaymentOrderResponse;
import com.pigeonkart.api.dto.PaymentVerifyRequest;
import com.pigeonkart.api.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments/razorpay")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/order")
    public PaymentOrderResponse createOrder(@Valid @RequestBody OrderRequest request) throws Exception {
        return paymentService.createPaymentOrder(request);
    }

    @PostMapping("/verify")
    public Map<String, Long> verify(@Valid @RequestBody PaymentVerifyRequest request) throws Exception {
        return Map.of("orderId", paymentService.verify(request));
    }
}
