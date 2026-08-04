package com.pigeonkart.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
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

    public Product(Long id, String name, ProductCategory category, int price, int stock, String unit, String description) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.unit = unit;
        this.description = description;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public ProductCategory getCategory() { return category; }
    public int getPrice() { return price; }
    public int getStock() { return stock; }
    public String getUnit() { return unit; }
    public String getDescription() { return description; }

    public void decreaseStock(int qty) {
        if (qty > this.stock) {
            throw new IllegalStateException("Not enough stock for product " + id);
        }
        this.stock -= qty;
    }
}
