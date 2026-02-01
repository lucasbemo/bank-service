package com.lz.bank.adapter.out.scheduler;

import com.lz.bank.domain.port.NotificationPort;
import com.lz.bank.domain.port.OutboxPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxScheduler {
    private static final Logger logger = LoggerFactory.getLogger(OutboxScheduler.class);

    private final OutboxPort outboxPort;
    private final NotificationPort notificationPort;
    private final int batchSize;

    public OutboxScheduler(OutboxPort outboxPort,
                           NotificationPort notificationPort,
                           @Value("${bank.outbox.batch-size:50}") int batchSize) {
        this.outboxPort = outboxPort;
        this.notificationPort = notificationPort;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${bank.outbox.poll-interval-ms:5000}")
    public void dispatch() {
        List<Long> pending = outboxPort.findPendingTransferNotifications(batchSize);
        for (Long transferId : pending) {
            try {
                notificationPort.notifyTransfer(transferId);
                outboxPort.markSent(transferId);
            } catch (Exception ex) {
                logger.warn("Failed to notify transfer {}", transferId, ex);
            }
        }
    }
}
