package com.lz.bank.adapter.in.web;

import com.lz.bank.application.usecase.CreateTransferUseCase;
import com.lz.bank.application.usecase.dto.CreateTransferCommand;
import com.lz.bank.application.usecase.dto.TransferResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transfers")
@Tag(name = "Transfers")
public class TransferController {
    private final CreateTransferUseCase createTransferUseCase;

    public TransferController(CreateTransferUseCase createTransferUseCase) {
        this.createTransferUseCase = createTransferUseCase;
    }

    @PostMapping
    @Operation(summary = "Create transfer")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Transfer created")
    public ResponseEntity<ApiResponse<TransferResponse>> createTransfer(
            @RequestHeader("Idempotency-Key")
            @Parameter(description = "Idempotency key", required = true, schema = @Schema(example = "e1c9b9e8-1c4b-4a1a-9a7d-8dbf2d23e3d1"))
            String idempotencyKey,
            @Valid @RequestBody CreateTransferRequest request
    ) {
        TransferResult result = createTransferUseCase.execute(
                new CreateTransferCommand(request.payer(), request.payee(), request.value(), idempotencyKey)
        );
        TransferResponse response = new TransferResponse(result.id(), result.status(), result.createdAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
