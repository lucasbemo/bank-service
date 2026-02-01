package com.lz.bank.adapter.out.persistence.repository;

import com.lz.bank.adapter.out.persistence.entity.OutboxEventEntity;
import com.lz.bank.adapter.out.persistence.entity.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OutboxJpaRepository extends JpaRepository<OutboxEventEntity, Long> {
    List<OutboxEventEntity> findByStatusOrderByCreatedAtAsc(OutboxStatus status);
    Optional<OutboxEventEntity> findByTransferId(Long transferId);
}
