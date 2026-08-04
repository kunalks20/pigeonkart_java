package com.pigeonkart.api.controller;

import com.pigeonkart.api.dto.OrderRequest;
import com.pigeonkart.api.dto.OrderResponse;
import com.pigeonkart.api.model.CustomerOrder;
import com.pigeonkart.api.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderResponse create(@Valid @RequestBody OrderRequest request) {
        CustomerOrder order = orderService.createOrder(request);
        return new OrderResponse(order);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable String id) {
        return new OrderResponse(orderService.getOrder(id));
    }
}
