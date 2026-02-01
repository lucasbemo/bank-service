package com.lz.bank.testsupport;

import com.lz.bank.application.usecase.dto.CreateTransferCommand;
import com.lz.bank.application.usecase.dto.CreateUserCommand;

import java.math.BigDecimal;

public final class TestCommands {
    private TestCommands() {
    }

    public static CreateUserCommand createUserCommand(String type, BigDecimal initialBalance) {
        return new CreateUserCommand(
                "Example User",
                "00000000000",
                "user@example.com",
                "secret",
                type,
                initialBalance
        );
    }

    public static CreateTransferCommand createTransferCommand(Long payerId, Long payeeId, BigDecimal amount, String key) {
        return new CreateTransferCommand(payerId, payeeId, amount, key);
    }
}
