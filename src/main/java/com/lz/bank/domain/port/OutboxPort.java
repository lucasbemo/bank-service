package com.lz.bank.domain.port;

import java.util.List;

public interface OutboxPort {
    void saveTransferNotification(Long transferId);
    List<Long> findPendingTransferNotifications(int batchSize);
    void markSent(Long transferId);
}
