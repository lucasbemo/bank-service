package com.lz.bank.adapter.out.persistence.entity;

import com.lz.bank.domain.model.TransferStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TransferEntityTest {

    @Test
    void storesTransferFields() {
        TransferEntity entity = new TransferEntity();
        Instant now = Instant.now();
        entity.setId(5L);
        entity.setPayerId(1L);
        entity.setPayeeId(2L);
        entity.setAmount(new BigDecimal("15.25"));
        entity.setStatus(TransferStatus.APPROVED);
        entity.setCreatedAt(now);
        entity.setIdempotencyKey("key");

        assertThat(entity.getId()).isEqualTo(5L);
        assertThat(entity.getPayerId()).isEqualTo(1L);
        assertThat(entity.getPayeeId()).isEqualTo(2L);
        assertThat(entity.getAmount()).isEqualByComparingTo("15.25");
        assertThat(entity.getStatus()).isEqualTo(TransferStatus.APPROVED);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getIdempotencyKey()).isEqualTo("key");
    }
}
