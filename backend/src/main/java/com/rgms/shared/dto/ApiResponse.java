package com.rgms.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * ApiResponse — Chuẩn hóa envelope response cho toàn bộ REST API của RGMS.
 *
 * Cấu trúc:
 * {
 *   "success": true,
 *   "data": { ... },
 *   "message": null,
 *   "timestamp": "2025-01-01T00:00:00"
 * }
 *
 * Dùng ApiResponse.ok(data) cho success, ApiResponse.error(msg) cho lỗi.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;
    private final LocalDateTime timestamp;

    private ApiResponse(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Tạo response thành công với data.
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /**
     * Tạo response thành công không có data.
     */
    public static <Void> ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null);
    }

    /**
     * Tạo response lỗi với message (dùng trong GlobalExceptionHandler).
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message);
    }
}
