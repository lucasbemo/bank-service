package com.lz.bank.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void scalesToTwoDecimals() {
        Money money = new Money(new BigDecimal("10"));

        assertThat(money.amount()).isEqualTo(new BigDecimal("10.00"));
        assertThat(money.amount().scale()).isEqualTo(2);
    }

    @Test
    void addAndSubtract() {
        Money base = new Money(new BigDecimal("20.00"));
        Money delta = new Money(new BigDecimal("5.50"));

        assertThat(base.add(delta).amount()).isEqualTo(new BigDecimal("25.50"));
        assertThat(base.subtract(delta).amount()).isEqualTo(new BigDecimal("14.50"));
    }

    @Test
    void comparesValues() {
        Money small = new Money(new BigDecimal("10.00"));
        Money large = new Money(new BigDecimal("20.00"));

        assertThat(small.isLessThan(large)).isTrue();
        assertThat(large.isLessThan(small)).isFalse();
    }

    @Test
    void rejectsNullAmount() {
        assertThatThrownBy(() -> new Money(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount cannot be null");
    }
}
