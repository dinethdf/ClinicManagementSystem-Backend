package com.dinethdf.ClinicManagementSystem.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;


    public ValidationException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public ValidationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.status = HttpStatus.BAD_REQUEST;
    }

    public String isErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
