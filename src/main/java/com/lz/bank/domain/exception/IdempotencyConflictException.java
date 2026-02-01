package com.lz.bank.domain.exception;

public class IdempotencyConflictException extends DomainException {
    public IdempotencyConflictException() {
        super("IDEMPOTENCY_CONFLICT", "Idempotency key already used for a different transfer");
    }
}
