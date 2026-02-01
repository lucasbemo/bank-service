package com.lz.bank.application.usecase;

import com.lz.bank.application.usecase.dto.CreateUserCommand;
import com.lz.bank.application.usecase.dto.UserResult;

public interface CreateUserUseCase {
    UserResult execute(CreateUserCommand command);
}
