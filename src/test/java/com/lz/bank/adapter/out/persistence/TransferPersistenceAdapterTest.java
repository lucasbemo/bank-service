package com.lz.bank.adapter.out.persistence;

import com.lz.bank.adapter.out.persistence.entity.TransferEntity;
import com.lz.bank.adapter.out.persistence.repository.TransferJpaRepository;
import com.lz.bank.domain.model.Money;
import com.lz.bank.domain.model.Transfer;
import com.lz.bank.domain.model.TransferStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransferPersistenceAdapterTest {

    @Test
    void mapsEntityToDomainOnFind() {
        TransferJpaRepository repository = mock(TransferJpaRepository.class);
        TransferPersistenceAdapter adapter = new TransferPersistenceAdapter(repository);

        Instant now = Instant.now();
        TransferEntity entity = new TransferEntity();
        entity.setId(8L);
        entity.setPayerId(1L);
        entity.setPayeeId(2L);
        entity.setAmount(new BigDecimal("19.99"));
        entity.setStatus(TransferStatus.APPROVED);
        entity.setCreatedAt(now);
        entity.setIdempotencyKey("key");

        when(repository.findByIdempotencyKey("key")).thenReturn(Optional.of(entity));

        Optional<Transfer> result = adapter.findByIdempotencyKey("key");

        assertThat(result).isPresent();
        Transfer transfer = result.get();
        assertThat(transfer.id()).isEqualTo(8L);
        assertThat(transfer.payerId()).isEqualTo(1L);
        assertThat(transfer.payeeId()).isEqualTo(2L);
        assertThat(transfer.amount().amount()).isEqualByComparingTo("19.99");
        assertThat(transfer.status()).isEqualTo(TransferStatus.APPROVED);
        assertThat(transfer.createdAt()).isEqualTo(now);
        assertThat(transfer.idempotencyKey()).isEqualTo("key");
    }

    @Test
    void mapsDomainToEntityOnSave() {
        TransferJpaRepository repository = mock(TransferJpaRepository.class);
        TransferPersistenceAdapter adapter = new TransferPersistenceAdapter(repository);

        Instant now = Instant.now();
        Transfer toSave = new Transfer(null, 3L, 4L, new Money(new BigDecimal("50.00")), TransferStatus.APPROVED, now, "abc");
        TransferEntity saved = new TransferEntity();
        saved.setId(55L);
        saved.setPayerId(3L);
        saved.setPayeeId(4L);
        saved.setAmount(new BigDecimal("50.00"));
        saved.setStatus(TransferStatus.APPROVED);
        saved.setCreatedAt(now);
        saved.setIdempotencyKey("abc");

        when(repository.save(any(TransferEntity.class))).thenReturn(saved);

        Transfer result = adapter.save(toSave);

        ArgumentCaptor<TransferEntity> captor = ArgumentCaptor.forClass(TransferEntity.class);
        verify(repository).save(captor.capture());
        TransferEntity captured = captor.getValue();
        assertThat(captured.getPayerId()).isEqualTo(3L);
        assertThat(captured.getPayeeId()).isEqualTo(4L);
        assertThat(captured.getAmount()).isEqualByComparingTo("50.00");
        assertThat(captured.getStatus()).isEqualTo(TransferStatus.APPROVED);
        assertThat(captured.getCreatedAt()).isEqualTo(now);
        assertThat(captured.getIdempotencyKey()).isEqualTo("abc");

        assertThat(result.id()).isEqualTo(55L);
        assertThat(result.idempotencyKey()).isEqualTo("abc");
    }
}
