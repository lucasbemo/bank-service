package com.lz.bank.application.usecase;

import com.lz.bank.application.usecase.dto.CreateTransferCommand;
import com.lz.bank.application.usecase.dto.TransferResult;

public interface CreateTransferUseCase {
    TransferResult execute(CreateTransferCommand command);
}
