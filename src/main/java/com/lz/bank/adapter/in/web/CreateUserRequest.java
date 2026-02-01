package com.lz.bank.adapter.in.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateUserRequest(
        @NotBlank String fullName,
        @NotBlank String document,
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotBlank String type,
        @NotNull @DecimalMin("0.00") BigDecimal initialBalance
) {
}
