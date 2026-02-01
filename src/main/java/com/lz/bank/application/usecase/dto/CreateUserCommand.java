package com.lz.bank.application.usecase.dto;

import java.math.BigDecimal;

public record CreateUserCommand(String fullName, String document, String email, String password, String type, BigDecimal initialBalance) {
}
