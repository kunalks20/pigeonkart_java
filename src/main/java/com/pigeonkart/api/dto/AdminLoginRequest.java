package com.pigeonkart.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AdminLoginRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
