package com.dinethdf.ClinicManagementSystem.DTO;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
// Make the main constructor private so people must use the static methods
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    /**
     * Creates a successful response.
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Creates an error response with no data.
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}