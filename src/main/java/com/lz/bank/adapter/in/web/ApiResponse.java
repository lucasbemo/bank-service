package com.lz.bank.adapter.in.web;

import java.time.Instant;

public record ApiResponse<T>(boolean success, Instant timestamp, T data, ApiError error) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, Instant.now(), data, null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, Instant.now(), null, new ApiError(code, message));
    }
}
