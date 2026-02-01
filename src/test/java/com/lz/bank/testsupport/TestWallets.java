package com.lz.bank.testsupport;

import com.lz.bank.domain.model.Money;
import com.lz.bank.domain.model.Wallet;

import java.math.BigDecimal;

public final class TestWallets {
    private TestWallets() {
    }

    public static Wallet wallet(Long userId, BigDecimal balance) {
        return new Wallet(userId, new Money(balance));
    }
}
