package com.lz.bank.adapter.out.persistence.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxStatusTest {

    @Test
    void exposesEnumValues() {
        assertThat(OutboxStatus.valueOf("PENDING")).isEqualTo(OutboxStatus.PENDING);
        assertThat(OutboxStatus.valueOf("SENT")).isEqualTo(OutboxStatus.SENT);
        assertThat(OutboxStatus.valueOf("FAILED")).isEqualTo(OutboxStatus.FAILED);
    }
}
