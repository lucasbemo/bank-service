package com.lz.bank.testsupport;

import com.lz.bank.domain.model.Money;
import com.lz.bank.domain.model.Transfer;
import com.lz.bank.domain.model.TransferStatus;

import java.math.BigDecimal;
import java.time.Instant;

public final class TestTransfers {
    private TestTransfers() {
    }

    public static Transfer transfer(Long id, Long payerId, Long payeeId, BigDecimal amount, String key) {
        return new Transfer(id, payerId, payeeId, new Money(amount), TransferStatus.APPROVED, Instant.now(), key);
    }
}
