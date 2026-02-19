package com.dinethdf.ClinicManagementSystem.util;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EncryptionUtil {

    @Value("${app.security.obfuscation.key:MySimpleKey}")
    private String key;

    public String encryptClinicPlane(String value) {
        if (value == null) return null;

        // 1. Convert to bytes
        byte[] inputBytes = value.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

        // 2. XOR (Remains as bytes)
        byte[] xored = xorWithKey(inputBytes, keyBytes);

        // 3. Base64 Encode (Safe for URLs)
        return Base64.getEncoder().encodeToString(xored);
    }

    public String decryptClinicPlane(String encryptedValue) {
        if (encryptedValue == null) return null;

        try {
            // 1. Base64 Decode (Back to bytes)
            byte[] decoded = Base64.getDecoder().decode(encryptedValue);
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

            // 2. XOR (Remains as bytes)
            byte[] originalBytes = xorWithKey(decoded, keyBytes);

            // 3. Convert back to String (Safe now because we have the original text bytes)
            return new String(originalBytes, StandardCharsets.UTF_8);

        } catch (IllegalArgumentException e) {
            return null; // Invalid Base64 input
        }
    }

    // Helper: Works purely on bytes to avoid corruption
    private byte[] xorWithKey(byte[] input, byte[] key) {
        byte[] output = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = (byte) (input[i] ^ key[i % key.length]);
        }
        return output;
    }
}