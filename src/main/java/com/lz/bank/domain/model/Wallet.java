package com.lz.bank.domain.model;

public final class Wallet {
    private final Long userId;
    private final Money balance;

    public Wallet(Long userId, Money balance) {
        this.userId = userId;
        this.balance = balance;
    }

    public Long userId() {
        return userId;
    }

    public Money balance() {
        return balance;
    }

    public Wallet credit(Money amount) {
        return new Wallet(userId, balance.add(amount));
    }

    public Wallet debit(Money amount) {
        return new Wallet(userId, balance.subtract(amount));
    }
}
