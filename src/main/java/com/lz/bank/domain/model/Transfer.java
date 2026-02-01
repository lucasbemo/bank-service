package com.lz.bank.domain.model;

import java.time.Instant;

public final class Transfer {
    private final Long id;
    private final Long payerId;
    private final Long payeeId;
    private final Money amount;
    private final TransferStatus status;
    private final Instant createdAt;
    private final String idempotencyKey;

    public Transfer(Long id, Long payerId, Long payeeId, Money amount, TransferStatus status, Instant createdAt, String idempotencyKey) {
        this.id = id;
        this.payerId = payerId;
        this.payeeId = payeeId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
        this.idempotencyKey = idempotencyKey;
    }

    public Long id() {
        return id;
    }

    public Long payerId() {
        return payerId;
    }

    public Long payeeId() {
        return payeeId;
    }

    public Money amount() {
        return amount;
    }

    public TransferStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public String idempotencyKey() {
        return idempotencyKey;
    }
}
