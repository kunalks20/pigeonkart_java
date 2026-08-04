package com.pigeonkart.api.controller;

import com.pigeonkart.api.dto.PaymentOrderResponse;
import com.pigeonkart.api.dto.PaymentVerifyRequest;
import com.pigeonkart.api.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/razorpay")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/order/{orderId}")
    public PaymentOrderResponse createOrder(@PathVariable String orderId) throws Exception {
        return paymentService.createPaymentOrder(orderId);
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verify(@Valid @RequestBody PaymentVerifyRequest request) throws Exception {
        paymentService.verify(request);
        return ResponseEntity.ok().build();
    }
}
