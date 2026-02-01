package com.lz.bank.adapter.out.scheduler;

import com.lz.bank.domain.port.NotificationPort;
import com.lz.bank.domain.port.OutboxPort;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxSchedulerTest {

    @Test
    void dispatchesNotificationsAndMarksSent() {
        OutboxPort outboxPort = mock(OutboxPort.class);
        NotificationPort notificationPort = mock(NotificationPort.class);
        OutboxScheduler scheduler = new OutboxScheduler(outboxPort, notificationPort, 10);

        when(outboxPort.findPendingTransferNotifications(10)).thenReturn(List.of(1L, 2L));

        scheduler.dispatch();

        verify(notificationPort, times(1)).notifyTransfer(1L);
        verify(notificationPort, times(1)).notifyTransfer(2L);
        verify(outboxPort, times(1)).markSent(1L);
        verify(outboxPort, times(1)).markSent(2L);
    }

    @Test
    void continuesWhenNotificationFails() {
        OutboxPort outboxPort = mock(OutboxPort.class);
        NotificationPort notificationPort = mock(NotificationPort.class);
        OutboxScheduler scheduler = new OutboxScheduler(outboxPort, notificationPort, 10);

        when(outboxPort.findPendingTransferNotifications(10)).thenReturn(List.of(1L, 2L));
        doThrow(new RuntimeException("boom")).when(notificationPort).notifyTransfer(1L);

        scheduler.dispatch();

        verify(notificationPort, times(1)).notifyTransfer(1L);
        verify(notificationPort, times(1)).notifyTransfer(2L);
        verify(outboxPort, times(0)).markSent(1L);
        verify(outboxPort, times(1)).markSent(2L);
    }
}
