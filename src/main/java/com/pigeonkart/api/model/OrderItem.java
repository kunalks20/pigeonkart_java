package com.pigeonkart.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private CustomerOrder order;

    private Long productId;
    private String productName;
    private int unitPrice;
    private int qty;

    protected OrderItem() {
        // JPA
    }

    public OrderItem(Long productId, String productName, int unitPrice, int qty) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.qty = qty;
    }
}
