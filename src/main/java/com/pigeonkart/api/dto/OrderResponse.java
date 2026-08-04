package com.pigeonkart.api.dto;

import com.pigeonkart.api.model.CustomerOrder;
import com.pigeonkart.api.model.OrderStatus;

public class OrderResponse {
    private Long id;
    private int totalAmount;
    private OrderStatus status;

    public OrderResponse(CustomerOrder order) {
        this.id = order.getId();
        this.totalAmount = order.getTotalAmount();
        this.status = order.getStatus();
    }

    public Long getId() { return id; }
    public int getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
}
