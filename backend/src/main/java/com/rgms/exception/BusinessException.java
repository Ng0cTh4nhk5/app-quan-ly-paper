package com.rgms.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode; // Optional — dùng cho machine-readable error response

    public BusinessException(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = null;
    }

    /**
     * Factory method — tạo lỗi với error code và message (dùng trong FsmService, Guards).
     * errorCode là machine-readable code (e.g. "FSM_TERMINAL_STATE"), message là human-readable.
     * Default status: UNPROCESSABLE_ENTITY (422).
     */
    public BusinessException(String errorCode, String message) {
        this(errorCode, message, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public static BusinessException of(String errorCode, String message) {
        return new BusinessException(errorCode, message, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public static BusinessException forbidden(String errorCode, String message) {
        return new BusinessException(errorCode, message, HttpStatus.FORBIDDEN);
    }

    /**
     * Full constructor.
     */
    public BusinessException(String errorCode, String message, HttpStatus status) {
        super("[" + errorCode + "] " + message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(message, HttpStatus.CONFLICT);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(message, HttpStatus.FORBIDDEN);
    }
}
