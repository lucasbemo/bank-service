package com.lz.bank.adapter.out.persistence;

import com.lz.bank.adapter.out.persistence.entity.WalletEntity;
import com.lz.bank.adapter.out.persistence.repository.WalletJpaRepository;
import com.lz.bank.domain.model.Money;
import com.lz.bank.domain.model.Wallet;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WalletPersistenceAdapterTest {

    @Test
    void mapsEntityToDomainOnFind() {
        WalletJpaRepository repository = mock(WalletJpaRepository.class);
        WalletPersistenceAdapter adapter = new WalletPersistenceAdapter(repository);

        WalletEntity entity = new WalletEntity();
        entity.setUserId(5L);
        entity.setBalance(new BigDecimal("250.00"));

        when(repository.findByUserId(5L)).thenReturn(Optional.of(entity));

        Optional<Wallet> result = adapter.findByUserIdForUpdate(5L);

        assertThat(result).isPresent();
        Wallet wallet = result.get();
        assertThat(wallet.userId()).isEqualTo(5L);
        assertThat(wallet.balance().amount()).isEqualByComparingTo("250.00");
    }

    @Test
    void mapsDomainToEntityOnSave() {
        WalletJpaRepository repository = mock(WalletJpaRepository.class);
        WalletPersistenceAdapter adapter = new WalletPersistenceAdapter(repository);

        Wallet toSave = new Wallet(9L, new Money(new BigDecimal("75.00")));
        WalletEntity saved = new WalletEntity();
        saved.setUserId(9L);
        saved.setBalance(new BigDecimal("75.00"));

        when(repository.save(any(WalletEntity.class))).thenReturn(saved);

        Wallet result = adapter.save(toSave);

        ArgumentCaptor<WalletEntity> captor = ArgumentCaptor.forClass(WalletEntity.class);
        verify(repository).save(captor.capture());
        WalletEntity captured = captor.getValue();
        assertThat(captured.getUserId()).isEqualTo(9L);
        assertThat(captured.getBalance()).isEqualByComparingTo("75.00");

        assertThat(result.userId()).isEqualTo(9L);
        assertThat(result.balance().amount()).isEqualByComparingTo("75.00");
    }
}
