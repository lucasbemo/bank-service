package com.lz.bank.domain.exception;

public class UserNotFoundException extends DomainException {
    public UserNotFoundException(Long userId) {
        super("USER_NOT_FOUND", "User not found: " + userId);
    }
}
