package com.lz.bank.application.service;

import com.lz.bank.application.usecase.dto.CreateTransferCommand;
import com.lz.bank.application.usecase.dto.TransferResult;
import com.lz.bank.domain.exception.AuthorizationDeniedException;
import com.lz.bank.domain.exception.IdempotencyConflictException;
import com.lz.bank.domain.exception.IdempotencyRequiredException;
import com.lz.bank.domain.exception.InsufficientBalanceException;
import com.lz.bank.domain.exception.UnauthorizedPayerException;
import com.lz.bank.domain.exception.UserNotFoundException;
import com.lz.bank.domain.model.Transfer;
import com.lz.bank.domain.model.TransferStatus;
import com.lz.bank.domain.model.Wallet;
import com.lz.bank.domain.port.AuthorizationPort;
import com.lz.bank.domain.port.OutboxPort;
import com.lz.bank.domain.port.TransferRepositoryPort;
import com.lz.bank.domain.port.UserRepositoryPort;
import com.lz.bank.domain.port.WalletRepositoryPort;
import com.lz.bank.testsupport.TestCommands;
import com.lz.bank.testsupport.TestTransfers;
import com.lz.bank.testsupport.TestUsers;
import com.lz.bank.testsupport.TestWallets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateTransferServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private WalletRepositoryPort walletRepositoryPort;

    @Mock
    private TransferRepositoryPort transferRepositoryPort;

    @Mock
    private AuthorizationPort authorizationPort;

    @Mock
    private OutboxPort outboxPort;

    @InjectMocks
    private CreateTransferService createTransferService;

    @Test
    void rejectsBlankIdempotencyKey() {
        CreateTransferCommand command = TestCommands.createTransferCommand(1L, 2L, new BigDecimal("10.00"), " ");

        assertThatThrownBy(() -> createTransferService.execute(command))
                .isInstanceOf(IdempotencyRequiredException.class);
    }

    @Test
    void returnsExistingTransferWhenSamePayload() {
        CreateTransferCommand command = TestCommands.createTransferCommand(1L, 2L, new BigDecimal("10.00"), "key");
        Transfer existing = TestTransfers.transfer(10L, 1L, 2L, new BigDecimal("10.00"), "key");
        when(transferRepositoryPort.findByIdempotencyKey("key")).thenReturn(Optional.of(existing));

        TransferResult result = createTransferService.execute(command);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.status()).isEqualTo(TransferStatus.APPROVED.name());
        verifyNoInteractions(userRepositoryPort, walletRepositoryPort, authorizationPort, outboxPort);
    }

    @Test
    void throwsConflictWhenSameKeyDifferentPayload() {
        CreateTransferCommand command = TestCommands.createTransferCommand(1L, 2L, new BigDecimal("10.00"), "key");
        Transfer existing = TestTransfers.transfer(10L, 1L, 2L, new BigDecimal("12.00"), "key");
        when(transferRepositoryPort.findByIdempotencyKey("key")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> createTransferService.execute(command))
                .isInstanceOf(IdempotencyConflictException.class);
    }

    @Test
    void throwsUserNotFoundForPayer() {
        CreateTransferCommand command = TestCommands.createTransferCommand(1L, 2L, new BigDecimal("10.00"), "key");
        when(transferRepositoryPort.findByIdempotencyKey("key")).thenReturn(Optional.empty());
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createTransferService.execute(command))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void rejectsShopkeeperPayer() {
        CreateTransferCommand command = TestCommands.createTransferCommand(1L, 2L, new BigDecimal("10.00"), "key");
        when(transferRepositoryPort.findByIdempotencyKey("key")).thenReturn(Optional.empty());
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(TestUsers.shopkeeper(1L)));
        when(userRepositoryPort.findById(2L)).thenReturn(Optional.of(TestUsers.common(2L)));

        assertThatThrownBy(() -> createTransferService.execute(command))
                .isInstanceOf(UnauthorizedPayerException.class);
    }

    @Test
    void rejectsWhenAuthorizationDenied() {
        CreateTransferCommand command = TestCommands.createTransferCommand(1L, 2L, new BigDecimal("10.00"), "key");
        when(transferRepositoryPort.findByIdempotencyKey("key")).thenReturn(Optional.empty());
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(TestUsers.common(1L)));
        when(userRepositoryPort.findById(2L)).thenReturn(Optional.of(TestUsers.common(2L)));
        when(authorizationPort.authorize()).thenReturn(false);

        assertThatThrownBy(() -> createTransferService.execute(command))
                .isInstanceOf(AuthorizationDeniedException.class);
    }

    @Test
    void throwsInsufficientBalance() {
        CreateTransferCommand command = TestCommands.createTransferCommand(1L, 2L, new BigDecimal("10.00"), "key");
        when(transferRepositoryPort.findByIdempotencyKey("key")).thenReturn(Optional.empty());
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(TestUsers.common(1L)));
        when(userRepositoryPort.findById(2L)).thenReturn(Optional.of(TestUsers.common(2L)));
        when(authorizationPort.authorize()).thenReturn(true);
        when(walletRepositoryPort.findByUserIdForUpdate(1L)).thenReturn(Optional.of(TestWallets.wallet(1L, new BigDecimal("5.00"))));
        when(walletRepositoryPort.findByUserIdForUpdate(2L)).thenReturn(Optional.of(TestWallets.wallet(2L, new BigDecimal("20.00"))));

        assertThatThrownBy(() -> createTransferService.execute(command))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    void locksWalletsInDeterministicOrder() {
        CreateTransferCommand command = TestCommands.createTransferCommand(9L, 2L, new BigDecimal("10.00"), "key");
        when(transferRepositoryPort.findByIdempotencyKey("key")).thenReturn(Optional.empty());
        when(userRepositoryPort.findById(9L)).thenReturn(Optional.of(TestUsers.common(9L)));
        when(userRepositoryPort.findById(2L)).thenReturn(Optional.of(TestUsers.common(2L)));
        when(authorizationPort.authorize()).thenReturn(true);
        when(walletRepositoryPort.findByUserIdForUpdate(2L)).thenReturn(Optional.of(TestWallets.wallet(2L, new BigDecimal("50.00"))));
        when(walletRepositoryPort.findByUserIdForUpdate(9L)).thenReturn(Optional.of(TestWallets.wallet(9L, new BigDecimal("50.00"))));
        Transfer savedTransfer = TestTransfers.transfer(99L, 9L, 2L, new BigDecimal("10.00"), "key");
        when(transferRepositoryPort.save(any(Transfer.class))).thenReturn(savedTransfer);

        createTransferService.execute(command);

        InOrder inOrder = inOrder(walletRepositoryPort);
        inOrder.verify(walletRepositoryPort).findByUserIdForUpdate(2L);
        inOrder.verify(walletRepositoryPort).findByUserIdForUpdate(9L);
    }

    @Test
    void createsTransferAndOutboxEvent() {
        CreateTransferCommand command = TestCommands.createTransferCommand(1L, 2L, new BigDecimal("10.00"), "key");
        when(transferRepositoryPort.findByIdempotencyKey("key")).thenReturn(Optional.empty());
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(TestUsers.common(1L)));
        when(userRepositoryPort.findById(2L)).thenReturn(Optional.of(TestUsers.common(2L)));
        when(authorizationPort.authorize()).thenReturn(true);
        when(walletRepositoryPort.findByUserIdForUpdate(1L)).thenReturn(Optional.of(TestWallets.wallet(1L, new BigDecimal("100.00"))));
        when(walletRepositoryPort.findByUserIdForUpdate(2L)).thenReturn(Optional.of(TestWallets.wallet(2L, new BigDecimal("25.00"))));

        Transfer savedTransfer = TestTransfers.transfer(10L, 1L, 2L, new BigDecimal("10.00"), "key");
        when(transferRepositoryPort.save(any(Transfer.class))).thenReturn(savedTransfer);

        TransferResult result = createTransferService.execute(command);

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepositoryPort, org.mockito.Mockito.times(2)).save(walletCaptor.capture());

        assertThat(walletCaptor.getAllValues())
                .anyMatch(wallet -> wallet.userId().equals(1L)
                        && wallet.balance().amount().compareTo(new BigDecimal("90.00")) == 0)
                .anyMatch(wallet -> wallet.userId().equals(2L)
                        && wallet.balance().amount().compareTo(new BigDecimal("35.00")) == 0);

        ArgumentCaptor<Transfer> transferCaptor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepositoryPort).save(transferCaptor.capture());
        Transfer toSave = transferCaptor.getValue();
        assertThat(toSave.payerId()).isEqualTo(1L);
        assertThat(toSave.payeeId()).isEqualTo(2L);
        assertThat(toSave.amount().amount()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(toSave.idempotencyKey()).isEqualTo("key");

        verify(outboxPort).saveTransferNotification(10L);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.status()).isEqualTo(TransferStatus.APPROVED.name());
    }
}
