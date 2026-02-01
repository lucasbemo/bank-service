package com.lz.bank.application.service;

import com.lz.bank.application.usecase.CreateUserUseCase;
import com.lz.bank.application.usecase.dto.CreateUserCommand;
import com.lz.bank.application.usecase.dto.UserResult;
import com.lz.bank.domain.model.Money;
import com.lz.bank.domain.model.User;
import com.lz.bank.domain.model.UserType;
import com.lz.bank.domain.model.Wallet;
import com.lz.bank.domain.port.UserRepositoryPort;
import com.lz.bank.domain.port.WalletRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateUserService implements CreateUserUseCase {
    private final UserRepositoryPort userRepositoryPort;
    private final WalletRepositoryPort walletRepositoryPort;

    public CreateUserService(UserRepositoryPort userRepositoryPort, WalletRepositoryPort walletRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.walletRepositoryPort = walletRepositoryPort;
    }

    @Override
    @Transactional
    public UserResult execute(CreateUserCommand command) {
        UserType type = UserType.valueOf(command.type().toUpperCase());
        User user = new User(null, command.fullName(), command.document(), command.email(), command.password(), type);
        User saved = userRepositoryPort.save(user);
        Money initialBalance = new Money(command.initialBalance());
        walletRepositoryPort.save(new Wallet(saved.id(), initialBalance));
        return new UserResult(saved.id(), saved.fullName(), saved.email(), saved.type().name());
    }
}
