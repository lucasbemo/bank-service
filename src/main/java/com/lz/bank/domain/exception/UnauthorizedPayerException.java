package com.lz.bank.domain.exception;

public class UnauthorizedPayerException extends DomainException {
    public UnauthorizedPayerException() {
        super("UNAUTHORIZED_PAYER", "Shopkeeper users cannot send transfers");
    }
}
