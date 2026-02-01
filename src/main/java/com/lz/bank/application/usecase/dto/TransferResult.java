package com.lz.bank.application.usecase.dto;

import java.time.Instant;

public record TransferResult(Long id, String status, Instant createdAt) {
}
