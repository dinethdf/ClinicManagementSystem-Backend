package com.dinethdf.ClinicManagementSystem.util;

import org.springframework.stereotype.Service;

@Service
public class FEValueEncryption {

    private static final long SECRET_OFFSET = 333333333L;
    private static final long SECRET_KEY = 854716209L;


    public String encodeId(Long id) {
        if (id == null) return null;

        // 1. Add Offset & XOR
        long scrambled = (id + SECRET_OFFSET) ^ SECRET_KEY;

        // 2. Pad with zeros to ensure strictly 10 digits
        // %010d means: integer, padded with zeros, width 10
        return String.format("%010d", scrambled);
    }

    /**
     * DECODE: Turns "0898565522" -> 1
     */
    public Long decodeId(String code) {
        try {
            // Parse the string to a number
            long scrambled = Long.parseLong(code);

            // 1. XOR & Subtract Offset
            long original = (scrambled ^ SECRET_KEY) - SECRET_OFFSET;

            return original > 0 ? original : 0L;
        } catch (NumberFormatException e) {
            // Return 0 or throw exception based on preference
            return 0L;
        }
    }
}