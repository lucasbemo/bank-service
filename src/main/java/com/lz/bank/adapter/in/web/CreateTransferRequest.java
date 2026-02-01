package com.lz.bank.adapter.in.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateTransferRequest(
        @NotNull @DecimalMin("0.01") BigDecimal value,
        @NotNull Long payer,
        @NotNull Long payee
) {
}
