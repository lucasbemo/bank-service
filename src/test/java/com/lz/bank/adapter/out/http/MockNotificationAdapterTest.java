package com.lz.bank.adapter.out.http;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class MockNotificationAdapterTest {

    @Test
    void acceptsNotification() {
        MockNotificationAdapter adapter = new MockNotificationAdapter();

        assertThatCode(() -> adapter.notifyTransfer(10L)).doesNotThrowAnyException();
    }
}
