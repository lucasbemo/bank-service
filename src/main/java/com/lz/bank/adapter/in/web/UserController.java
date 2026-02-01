package com.lz.bank.adapter.in.web;

import com.lz.bank.application.usecase.CreateUserUseCase;
import com.lz.bank.application.usecase.dto.CreateUserCommand;
import com.lz.bank.application.usecase.dto.UserResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Tag(name = "Users")
public class UserController {
    private final CreateUserUseCase createUserUseCase;

    public UserController(CreateUserUseCase createUserUseCase) {
        this.createUserUseCase = createUserUseCase;
    }

    @PostMapping
    @Operation(summary = "Create user")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResult result = createUserUseCase.execute(new CreateUserCommand(
                request.fullName(),
                request.document(),
                request.email(),
                request.password(),
                request.type(),
                request.initialBalance()
        ));
        UserResponse response = new UserResponse(result.id(), result.fullName(), result.email(), result.type());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
