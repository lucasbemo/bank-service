package com.lz.bank.adapter.out.persistence.repository;

import com.lz.bank.adapter.out.persistence.entity.TransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransferJpaRepository extends JpaRepository<TransferEntity, Long> {
    Optional<TransferEntity> findByIdempotencyKey(String idempotencyKey);
}
