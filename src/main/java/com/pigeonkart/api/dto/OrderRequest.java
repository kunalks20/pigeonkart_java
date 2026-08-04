package com.pigeonkart.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequest {

    @Valid
    private Customer customer;

    @NotEmpty
    private List<Item> items;

    @Getter
    public static class Customer {
        @NotBlank private String name;
        @NotBlank
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone must be a 10-digit mobile number")
        private String phone;
        @NotBlank private String address;
    }

    @Getter
    @Setter
    public static class Item {
        @NotBlank private Long productId;
        private int qty;
    }
}
