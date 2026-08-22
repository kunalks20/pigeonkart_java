package com.pigeonkart.api.model;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    FAILED
}
