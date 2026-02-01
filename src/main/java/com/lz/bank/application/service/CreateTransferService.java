package com.lz.bank.application.service;

import com.lz.bank.application.usecase.CreateTransferUseCase;
import com.lz.bank.application.usecase.dto.CreateTransferCommand;
import com.lz.bank.application.usecase.dto.TransferResult;
import com.lz.bank.domain.exception.AuthorizationDeniedException;
import com.lz.bank.domain.exception.IdempotencyConflictException;
import com.lz.bank.domain.exception.IdempotencyRequiredException;
import com.lz.bank.domain.exception.InsufficientBalanceException;
import com.lz.bank.domain.exception.UnauthorizedPayerException;
import com.lz.bank.domain.exception.UserNotFoundException;
import com.lz.bank.domain.model.Money;
import com.lz.bank.domain.model.Transfer;
import com.lz.bank.domain.model.TransferStatus;
import com.lz.bank.domain.model.User;
import com.lz.bank.domain.model.UserType;
import com.lz.bank.domain.model.Wallet;
import com.lz.bank.domain.port.AuthorizationPort;
import com.lz.bank.domain.port.OutboxPort;
import com.lz.bank.domain.port.TransferRepositoryPort;
import com.lz.bank.domain.port.UserRepositoryPort;
import com.lz.bank.domain.port.WalletRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CreateTransferService implements CreateTransferUseCase {
    private final UserRepositoryPort userRepositoryPort;
    private final WalletRepositoryPort walletRepositoryPort;
    private final TransferRepositoryPort transferRepositoryPort;
    private final AuthorizationPort authorizationPort;
    private final OutboxPort outboxPort;

    public CreateTransferService(UserRepositoryPort userRepositoryPort,
                                 WalletRepositoryPort walletRepositoryPort,
                                 TransferRepositoryPort transferRepositoryPort,
                                 AuthorizationPort authorizationPort,
                                 OutboxPort outboxPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.walletRepositoryPort = walletRepositoryPort;
        this.transferRepositoryPort = transferRepositoryPort;
        this.authorizationPort = authorizationPort;
        this.outboxPort = outboxPort;
    }

    @Override
    @Transactional
    public TransferResult execute(CreateTransferCommand command) {
        String idempotencyKey = command.idempotencyKey();
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IdempotencyRequiredException();
        }

        Transfer existing = transferRepositoryPort.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existing != null) {
            boolean sameTransfer = existing.payerId().equals(command.payerId())
                    && existing.payeeId().equals(command.payeeId())
                    && existing.amount().amount().compareTo(command.amount()) == 0;
            if (!sameTransfer) {
                throw new IdempotencyConflictException();
            }
            return new TransferResult(existing.id(), existing.status().name(), existing.createdAt());
        }

        User payer = userRepositoryPort.findById(command.payerId())
                .orElseThrow(() -> new UserNotFoundException(command.payerId()));
        User payee = userRepositoryPort.findById(command.payeeId())
                .orElseThrow(() -> new UserNotFoundException(command.payeeId()));

        if (payer.type() == UserType.SHOPKEEPER) {
            throw new UnauthorizedPayerException();
        }

        if (!authorizationPort.authorize()) {
            throw new AuthorizationDeniedException();
        }

        Money amount = new Money(command.amount());
        Long firstLockId = payer.id() < payee.id() ? payer.id() : payee.id();
        Long secondLockId = payer.id() < payee.id() ? payee.id() : payer.id();

        Wallet firstWallet = walletRepositoryPort.findByUserIdForUpdate(firstLockId)
                .orElseThrow(() -> new UserNotFoundException(firstLockId));
        Wallet secondWallet = walletRepositoryPort.findByUserIdForUpdate(secondLockId)
                .orElseThrow(() -> new UserNotFoundException(secondLockId));

        Wallet payerWallet = firstLockId.equals(payer.id()) ? firstWallet : secondWallet;
        Wallet payeeWallet = firstLockId.equals(payee.id()) ? firstWallet : secondWallet;

        if (payerWallet.balance().isLessThan(amount)) {
            throw new InsufficientBalanceException();
        }

        Wallet updatedPayer = payerWallet.debit(amount);
        Wallet updatedPayee = payeeWallet.credit(amount);

        walletRepositoryPort.save(updatedPayer);
        walletRepositoryPort.save(updatedPayee);

        Transfer transfer = new Transfer(
                null,
                payer.id(),
                payee.id(),
                amount,
                TransferStatus.APPROVED,
                Instant.now(),
                idempotencyKey
        );

        Transfer saved = transferRepositoryPort.save(transfer);
        outboxPort.saveTransferNotification(saved.id());

        return new TransferResult(saved.id(), saved.status().name(), saved.createdAt());
    }
}
