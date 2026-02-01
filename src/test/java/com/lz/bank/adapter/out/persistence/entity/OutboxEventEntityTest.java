package com.lz.bank.adapter.out.persistence.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxEventEntityTest {

    @Test
    void storesOutboxFields() {
        OutboxEventEntity entity = new OutboxEventEntity();
        Instant now = Instant.now();
        entity.setId(3L);
        entity.setTransferId(77L);
        entity.setStatus(OutboxStatus.SENT);
        entity.setCreatedAt(now);

        assertThat(entity.getId()).isEqualTo(3L);
        assertThat(entity.getTransferId()).isEqualTo(77L);
        assertThat(entity.getStatus()).isEqualTo(OutboxStatus.SENT);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
    }
}
