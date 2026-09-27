package com.pigeonkart.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminCouponRequest {
    @NotBlank
    private String code;
    private String description;
    @NotBlank private String discountType; // "PERCENTAGE" or "FLAT"
    @Min(0) private int discountValue;
    private String scopeCategory;      // "NAMKIN", "ACHAR", or null/blank for storewide
    private String scopeUnitContains;  // e.g. "500g", or null/blank for no unit restriction
    private boolean active = true;
}
