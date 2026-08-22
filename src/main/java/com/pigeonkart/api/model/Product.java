package com.pigeonkart.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "products")
@Setter
@Getter
@AllArgsConstructor
public class Product {

    @Id
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    private int price; // rupees, kept as integer paise-free for simplicity

    private int stock;

    private String unit;

    private String description;

    protected Product() {
        // JPA
    }

    public void decreaseStock(int qty) {
        if (qty > this.stock) {
            throw new IllegalStateException("Not enough stock for product " + id);
        }
        this.stock -= qty;
    }
}
