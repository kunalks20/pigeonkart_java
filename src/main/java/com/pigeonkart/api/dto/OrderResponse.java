package com.pigeonkart.api.dto;

import com.pigeonkart.api.model.CustomerOrder;
import com.pigeonkart.api.model.OrderStatus;
import lombok.Getter;

@Getter
public class OrderResponse {
    private Long id;
    private String customerName;
    private String customerPhone;
    private int totalAmount;
    private OrderStatus status;

    public OrderResponse(CustomerOrder order) {
        this.id = order.getId();
        this.customerName = order.getCustomerName();
        this.customerPhone = order.getCustomerPhone();
        this.totalAmount = order.getTotalAmount();
        this.status = order.getStatus();
    }
}
