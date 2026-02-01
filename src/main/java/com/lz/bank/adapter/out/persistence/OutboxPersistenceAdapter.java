package com.lz.bank.adapter.out.persistence;

import com.lz.bank.adapter.out.persistence.entity.OutboxEventEntity;
import com.lz.bank.adapter.out.persistence.entity.OutboxStatus;
import com.lz.bank.adapter.out.persistence.repository.OutboxJpaRepository;
import com.lz.bank.domain.port.OutboxPort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OutboxPersistenceAdapter implements OutboxPort {
    private final OutboxJpaRepository outboxJpaRepository;

    public OutboxPersistenceAdapter(OutboxJpaRepository outboxJpaRepository) {
        this.outboxJpaRepository = outboxJpaRepository;
    }

    @Override
    public void saveTransferNotification(Long transferId) {
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setTransferId(transferId);
        entity.setStatus(OutboxStatus.PENDING);
        entity.setCreatedAt(Instant.now());
        outboxJpaRepository.save(entity);
    }

    @Override
    public List<Long> findPendingTransferNotifications(int batchSize) {
        return outboxJpaRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING)
                .stream()
                .limit(batchSize)
                .map(OutboxEventEntity::getTransferId)
                .collect(Collectors.toList());
    }

    @Override
    public void markSent(Long transferId) {
        outboxJpaRepository.findByTransferId(transferId).ifPresent(entity -> {
            entity.setStatus(OutboxStatus.SENT);
            outboxJpaRepository.save(entity);
        });
    }
}
