package com.lz.bank.domain.exception;

public class IdempotencyRequiredException extends DomainException {
    public IdempotencyRequiredException() {
        super("IDEMPOTENCY_REQUIRED", "Idempotency-Key header is required");
    }
}
