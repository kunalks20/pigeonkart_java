package com.pigeonkart.api.model;

public enum DiscountType {
    PERCENTAGE, // discountValue is a % (e.g. 10 = 10% off)
    FLAT        // discountValue is a flat ₹ amount off, per matching unit
}
