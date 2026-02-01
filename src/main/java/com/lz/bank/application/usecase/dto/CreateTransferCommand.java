package com.lz.bank.application.usecase.dto;

import java.math.BigDecimal;

public record CreateTransferCommand(Long payerId, Long payeeId, BigDecimal amount, String idempotencyKey) {
}
