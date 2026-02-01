package com.lz.bank.adapter.out.persistence;

import com.lz.bank.adapter.out.persistence.entity.TransferEntity;
import com.lz.bank.adapter.out.persistence.repository.TransferJpaRepository;
import com.lz.bank.domain.model.Money;
import com.lz.bank.domain.model.Transfer;
import com.lz.bank.domain.port.TransferRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TransferPersistenceAdapter implements TransferRepositoryPort {
    private final TransferJpaRepository transferJpaRepository;

    public TransferPersistenceAdapter(TransferJpaRepository transferJpaRepository) {
        this.transferJpaRepository = transferJpaRepository;
    }

    @Override
    public Optional<Transfer> findByIdempotencyKey(String idempotencyKey) {
        return transferJpaRepository.findByIdempotencyKey(idempotencyKey)
                .map(TransferPersistenceAdapter::toDomain);
    }

    @Override
    public Transfer save(Transfer transfer) {
        TransferEntity entity = toEntity(transfer);
        TransferEntity saved = transferJpaRepository.save(entity);
        return toDomain(saved);
    }

    private static Transfer toDomain(TransferEntity entity) {
        return new Transfer(
                entity.getId(),
                entity.getPayerId(),
                entity.getPayeeId(),
                new Money(entity.getAmount()),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getIdempotencyKey()
        );
    }

    private static TransferEntity toEntity(Transfer transfer) {
        TransferEntity entity = new TransferEntity();
        entity.setId(transfer.id());
        entity.setPayerId(transfer.payerId());
        entity.setPayeeId(transfer.payeeId());
        entity.setAmount(transfer.amount().amount());
        entity.setStatus(transfer.status());
        entity.setCreatedAt(transfer.createdAt());
        entity.setIdempotencyKey(transfer.idempotencyKey());
        return entity;
    }
}
