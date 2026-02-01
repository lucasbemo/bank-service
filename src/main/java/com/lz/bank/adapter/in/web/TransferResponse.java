package com.lz.bank.adapter.in.web;

import java.time.Instant;

public record TransferResponse(Long id, String status, Instant createdAt) {
}
