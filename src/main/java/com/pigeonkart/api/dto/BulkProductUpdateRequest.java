package com.pigeonkart.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class BulkProductUpdateRequest {
    @NotEmpty
    @Valid // cascades validation into each AdminProductRequest in the list
    private List<AdminProductRequest> products;

    public List<AdminProductRequest> getProducts() { return products; }
    public void setProducts(List<AdminProductRequest> products) { this.products = products; }
}
