package com.lz.bank.domain.exception;

public class InsufficientBalanceException extends DomainException {
    public InsufficientBalanceException() {
        super("INSUFFICIENT_BALANCE", "Payer does not have enough balance");
    }
}
