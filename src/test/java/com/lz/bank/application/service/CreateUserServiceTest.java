package com.lz.bank.application.service;

import com.lz.bank.application.usecase.dto.CreateUserCommand;
import com.lz.bank.application.usecase.dto.UserResult;
import com.lz.bank.domain.model.User;
import com.lz.bank.domain.model.Wallet;
import com.lz.bank.domain.port.UserRepositoryPort;
import com.lz.bank.domain.port.WalletRepositoryPort;
import com.lz.bank.testsupport.TestCommands;
import com.lz.bank.testsupport.TestUsers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateUserServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private WalletRepositoryPort walletRepositoryPort;

    @InjectMocks
    private CreateUserService createUserService;

    @Test
    void createsUserAndWallet() {
        CreateUserCommand command = TestCommands.createUserCommand("common", new BigDecimal("1000.00"));
        User savedUser = TestUsers.common(1L);
        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);

        UserResult result = createUserService.execute(command);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepositoryPort).save(userCaptor.capture());
        assertThat(userCaptor.getValue().type().name()).isEqualTo("COMMON");

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepositoryPort).save(walletCaptor.capture());
        Wallet wallet = walletCaptor.getValue();
        assertThat(wallet.userId()).isEqualTo(1L);
        assertThat(wallet.balance().amount()).isEqualByComparingTo(new BigDecimal("1000.00"));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.fullName()).isEqualTo(savedUser.fullName());
        assertThat(result.email()).isEqualTo(savedUser.email());
        assertThat(result.type()).isEqualTo("COMMON");
    }
}
