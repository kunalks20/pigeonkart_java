package com.pigeonkart.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Setter
@Getter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    private int price; // rupees, kept as integer paise-free for simplicity

    private int stock;

    private String unit;

    private String description;

    @Column(name = "product_code")
    private String productCode;

    public void decreaseStock(int qty) {
        if (qty > this.stock) {
            throw new IllegalStateException("Not enough stock for product " + id);
        }
        this.stock -= qty;
    }
}
