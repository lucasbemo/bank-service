package com.lz.bank.adapter.out.persistence;

import com.lz.bank.adapter.out.persistence.entity.OutboxEventEntity;
import com.lz.bank.adapter.out.persistence.entity.OutboxStatus;
import com.lz.bank.adapter.out.persistence.repository.OutboxJpaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxPersistenceAdapterTest {

    @Test
    void savesTransferNotification() {
        OutboxJpaRepository repository = mock(OutboxJpaRepository.class);
        OutboxPersistenceAdapter adapter = new OutboxPersistenceAdapter(repository);

        adapter.saveTransferNotification(100L);

        ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(repository).save(captor.capture());
        OutboxEventEntity saved = captor.getValue();
        assertThat(saved.getTransferId()).isEqualTo(100L);
        assertThat(saved.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void returnsPendingNotificationsWithLimit() {
        OutboxJpaRepository repository = mock(OutboxJpaRepository.class);
        OutboxPersistenceAdapter adapter = new OutboxPersistenceAdapter(repository);

        OutboxEventEntity first = new OutboxEventEntity();
        first.setTransferId(1L);
        first.setStatus(OutboxStatus.PENDING);
        first.setCreatedAt(Instant.now());

        OutboxEventEntity second = new OutboxEventEntity();
        second.setTransferId(2L);
        second.setStatus(OutboxStatus.PENDING);
        second.setCreatedAt(Instant.now());

        OutboxEventEntity third = new OutboxEventEntity();
        third.setTransferId(3L);
        third.setStatus(OutboxStatus.PENDING);
        third.setCreatedAt(Instant.now());

        when(repository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING))
                .thenReturn(List.of(first, second, third));

        List<Long> result = adapter.findPendingTransferNotifications(2);

        assertThat(result).containsExactly(1L, 2L);
    }

    @Test
    void marksNotificationAsSent() {
        OutboxJpaRepository repository = mock(OutboxJpaRepository.class);
        OutboxPersistenceAdapter adapter = new OutboxPersistenceAdapter(repository);

        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setId(5L);
        entity.setTransferId(10L);
        entity.setStatus(OutboxStatus.PENDING);
        entity.setCreatedAt(Instant.now());

        when(repository.findByTransferId(10L)).thenReturn(Optional.of(entity));
        when(repository.save(any(OutboxEventEntity.class))).thenReturn(entity);

        adapter.markSent(10L);

        ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OutboxStatus.SENT);
    }
}
