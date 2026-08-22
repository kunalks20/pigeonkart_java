package com.pigeonkart.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AdminUpdate {
    @NotBlank
    private String status;
    private String remarks;
}
