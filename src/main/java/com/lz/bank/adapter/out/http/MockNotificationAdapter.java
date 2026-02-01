package com.lz.bank.adapter.out.http;

import com.lz.bank.domain.port.NotificationPort;

public class MockNotificationAdapter implements NotificationPort {
    @Override
    public void notifyTransfer(Long transferId) {
    }
}
