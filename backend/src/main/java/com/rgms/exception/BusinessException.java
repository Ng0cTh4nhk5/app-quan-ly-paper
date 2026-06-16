package com.rgms.exception;

/**
 * Thrown when a business rule is violated (e.g., invalid FSM transition,
 * advance rate exceeded, missing required file before submission).
 * Maps to HTTP 422 Unprocessable Entity.
 */
public class BusinessException extends RuntimeException {

    private final String errorCode;

    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_RULE_VIOLATION";
    }

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
