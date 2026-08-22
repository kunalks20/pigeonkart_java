package com.pigeonkart.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AdminProductRequest {
    private Long id;
    @NotBlank private String name;
    @NotBlank private String category; // "NAMKIN" or "ACHAR"
    @Min(0) private int price;
    @Min(0) private int stock;
    private String unit;
    private String description;
}
