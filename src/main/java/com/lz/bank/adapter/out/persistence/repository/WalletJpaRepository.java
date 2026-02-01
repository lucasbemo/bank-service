package com.lz.bank.adapter.out.persistence.repository;

import com.lz.bank.adapter.out.persistence.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface WalletJpaRepository extends JpaRepository<WalletEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<WalletEntity> findByUserId(Long userId);
}
