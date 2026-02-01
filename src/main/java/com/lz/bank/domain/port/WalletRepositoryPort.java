package com.lz.bank.domain.port;

import com.lz.bank.domain.model.Wallet;

import java.util.Optional;

public interface WalletRepositoryPort {
    Optional<Wallet> findByUserIdForUpdate(Long userId);
    Wallet save(Wallet wallet);
}
