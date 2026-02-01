package com.lz.bank.domain.port;

import com.lz.bank.domain.model.Transfer;

import java.util.Optional;

public interface TransferRepositoryPort {
    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);
    Transfer save(Transfer transfer);
}
