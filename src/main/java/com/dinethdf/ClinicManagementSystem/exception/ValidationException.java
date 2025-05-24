package com.dinethdf.ClinicManagementSystem.exception;

public class ValidationException extends RuntimeException {

    private final String errorCode;

    public ValidationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String isErrorCode() {
        return errorCode;
    }
}
