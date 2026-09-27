package com.pigeonkart.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code; // e.g. "ACHAR500", entered by the customer as-is (case-insensitive on lookup)

    private String description;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    private int discountValue; // % if PERCENTAGE, rupees if FLAT

    // Scope — both optional. Matching a product means:
    //   (scopeCategory is null OR product.category == scopeCategory)
    //   AND (scopeUnitContains is null OR product.unit contains scopeUnitContains, case-insensitive)
    // Leaving both null makes the coupon apply storewide — this is what lets
    // you add unrelated future coupons without any schema/code changes.
    @Enumerated(EnumType.STRING)
    private ProductCategory scopeCategory;

    private String scopeUnitContains; // e.g. "500g" — matches "500g jar", "500g pack", etc.

    private boolean active = true;

    public boolean matches(ProductCategory category, String unit) {
        boolean categoryOk = scopeCategory == null || scopeCategory == category;
        boolean unitOk = scopeUnitContains == null || scopeUnitContains.isBlank()
                || (unit != null && unit.toLowerCase().contains(scopeUnitContains.toLowerCase()));
        return categoryOk && unitOk;
    }

    /** Applies this coupon's discount to a single unit price, floored at 0. */
    public int discountedUnitPrice(int unitPrice) {
        int discounted = discountType == DiscountType.PERCENTAGE
                ? unitPrice - (unitPrice * discountValue / 100)
                : unitPrice - discountValue;
        return Math.max(discounted, 0);
    }

}
