package com.pigeonkart.api.dto;

import com.pigeonkart.api.model.Coupon;
import com.pigeonkart.api.model.DiscountType;
import com.pigeonkart.api.model.ProductCategory;
import lombok.Getter;

@Getter
public class CouponResponse {
    private String code;
    private String description;
    private DiscountType discountType;
    private int discountValue;
    private ProductCategory scopeCategory;
    private String scopeUnitContains;

    public CouponResponse(Coupon coupon) {
        this.code = coupon.getCode();
        this.description = coupon.getDescription();
        this.discountType = coupon.getDiscountType();
        this.discountValue = coupon.getDiscountValue();
        this.scopeCategory = coupon.getScopeCategory();
        this.scopeUnitContains = coupon.getScopeUnitContains();
    }
}
