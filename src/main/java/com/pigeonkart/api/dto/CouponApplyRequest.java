package com.pigeonkart.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CouponApplyRequest {
    @NotBlank
    private String code;
}
