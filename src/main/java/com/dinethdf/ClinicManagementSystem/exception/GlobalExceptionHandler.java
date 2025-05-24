package com.dinethdf.ClinicManagementSystem.exception;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ValidationException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", true);
        response.put("code", ex.isErrorCode());
        response.put("message", ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }
}
