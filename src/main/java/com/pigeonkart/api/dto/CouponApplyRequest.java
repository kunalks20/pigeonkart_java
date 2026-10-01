package com.pigeonkart.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CouponApplyRequest {
    @NotBlank
    private String code;

    @Valid
    @NotEmpty
    private List<Item> items;

    @Getter
    @Setter
    public static class Item {
        @NotNull
        @Positive
        private Long productId;

        @Positive
        private int qty;
    }
}
