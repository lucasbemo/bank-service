package com.lz.bank.adapter.out.persistence.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class WalletEntityTest {

    @Test
    void storesWalletFields() {
        WalletEntity entity = new WalletEntity();
        entity.setUserId(7L);
        entity.setBalance(new BigDecimal("88.50"));

        assertThat(entity.getUserId()).isEqualTo(7L);
        assertThat(entity.getBalance()).isEqualByComparingTo("88.50");
    }
}
