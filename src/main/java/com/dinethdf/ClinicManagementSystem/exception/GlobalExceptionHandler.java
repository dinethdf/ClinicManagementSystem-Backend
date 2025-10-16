package com.dinethdf.ClinicManagementSystem.exception;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ValidationException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("errorCode", ex.getErrorCode());
        response.put("params", ex.getParams() != null ? ex.getParams() : Map.of());
        response.put("defaultMessage", ex.getDefaultMessage());
        response.put("status", ex.getStatus().value());

        return ResponseEntity.status(ex.getStatus()).body(response);
    }
}
