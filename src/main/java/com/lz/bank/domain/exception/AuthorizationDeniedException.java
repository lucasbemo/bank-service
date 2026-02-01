package com.lz.bank.domain.exception;

public class AuthorizationDeniedException extends DomainException {
    public AuthorizationDeniedException() {
        super("AUTHORIZATION_DENIED", "Authorization service denied the transfer");
    }
}
