package com.dinethdf.ClinicManagementSystem.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ValidationException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;
    private final Map<String, Object> params;      // Dynamic parameters for i18n
    private final String defaultMessage;           // Fallback English message

    // Constructor with default status BAD_REQUEST
    public ValidationException(String defaultMessage, String errorCode, Map<String, Object> params) {
        super(defaultMessage);
        this.errorCode = errorCode;
        this.status = HttpStatus.BAD_REQUEST;
        this.params = params;
        this.defaultMessage = defaultMessage;
    }
    // Constructor with default status BAD_REQUEST no param
    public ValidationException(String defaultMessage, String errorCode) {
        super(defaultMessage);
        this.errorCode = errorCode;
        this.status = HttpStatus.BAD_REQUEST;
        this.params = null;
        this.defaultMessage = defaultMessage;
    }

    // Constructor with default status BAD_REQUEST no param
    public ValidationException(String defaultMessage, String errorCode, HttpStatus status) {
        super(defaultMessage);
        this.errorCode = errorCode;
        this.status = status;
        this.params = null;
        this.defaultMessage = defaultMessage;
    }

    // Constructor with custom status
    public ValidationException(String defaultMessage, String errorCode, Map<String, Object> params, HttpStatus status) {
        super(defaultMessage);
        this.errorCode = errorCode;
        this.status = status;
        this.params = params;
        this.defaultMessage = defaultMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
