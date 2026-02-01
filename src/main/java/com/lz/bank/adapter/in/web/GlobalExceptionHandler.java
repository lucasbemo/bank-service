package com.lz.bank.adapter.in.web;

import com.lz.bank.domain.exception.DomainException;
import com.lz.bank.domain.exception.IdempotencyRequiredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomain(DomainException ex) {
        HttpStatus status = switch (ex.code()) {
            case "INSUFFICIENT_BALANCE" -> HttpStatus.UNPROCESSABLE_ENTITY;
            case "UNAUTHORIZED_PAYER", "AUTHORIZATION_DENIED" -> HttpStatus.FORBIDDEN;
            case "USER_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "IDEMPOTENCY_REQUIRED" -> HttpStatus.BAD_REQUEST;
            case "IDEMPOTENCY_CONFLICT" -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(ApiResponse.error(ex.code(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().isEmpty()
                ? "Validation error"
                : ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingHeader(MissingRequestHeaderException ex) {
        if ("Idempotency-Key".equalsIgnoreCase(ex.getHeaderName())) {
            IdempotencyRequiredException exception = new IdempotencyRequiredException();
            return ResponseEntity.badRequest().body(ApiResponse.error(exception.code(), exception.getMessage()));
        }
        return ResponseEntity.badRequest().body(ApiResponse.error("MISSING_HEADER", ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("DATA_INTEGRITY_VIOLATION", "Conflict while saving data"));
    }
}
