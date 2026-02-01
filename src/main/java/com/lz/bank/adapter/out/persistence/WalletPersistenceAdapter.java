package com.lz.bank.adapter.out.persistence;

import com.lz.bank.adapter.out.persistence.entity.WalletEntity;
import com.lz.bank.adapter.out.persistence.repository.WalletJpaRepository;
import com.lz.bank.domain.model.Money;
import com.lz.bank.domain.model.Wallet;
import com.lz.bank.domain.port.WalletRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class WalletPersistenceAdapter implements WalletRepositoryPort {
    private final WalletJpaRepository walletJpaRepository;

    public WalletPersistenceAdapter(WalletJpaRepository walletJpaRepository) {
        this.walletJpaRepository = walletJpaRepository;
    }

    @Override
    public Optional<Wallet> findByUserIdForUpdate(Long userId) {
        return walletJpaRepository.findByUserId(userId).map(WalletPersistenceAdapter::toDomain);
    }

    @Override
    public Wallet save(Wallet wallet) {
        WalletEntity entity = toEntity(wallet);
        WalletEntity saved = walletJpaRepository.save(entity);
        return toDomain(saved);
    }

    private static Wallet toDomain(WalletEntity entity) {
        return new Wallet(entity.getUserId(), new Money(entity.getBalance()));
    }

    private static WalletEntity toEntity(Wallet wallet) {
        WalletEntity entity = new WalletEntity();
        entity.setUserId(wallet.userId());
        entity.setBalance(wallet.balance().amount());
        return entity;
    }
}
