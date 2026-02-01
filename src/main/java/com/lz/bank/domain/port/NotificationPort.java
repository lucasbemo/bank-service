package com.lz.bank.domain.port;

public interface NotificationPort {
    void notifyTransfer(Long transferId);
}
