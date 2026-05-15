package com.dinethdf.ClinicManagementSystem.util;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class FEValueEncryption {

    private static final long SECRET_OFFSET = 333333333L;
    private static final long SECRET_KEY = 854716209L;

    // AES Configuration for FE/BE String Encryption
    private static final String AES_ENCRYPTION_KEY = "CureGuardFeKey123456789012345678"; // Exactly 32 chars
    private static final String AES_ALGORITHM = "AES";



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

    /**
     * Encrypt a plain string into a secure Base64 AES string for the Frontend
     */
    public String encodeString(String plainText) {
        if (plainText == null || plainText.isEmpty()) return null;
        try {
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(AES_ENCRYPTION_KEY.getBytes(), AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            System.err.println("Error encrypting string for FE: " + e.getMessage());
            return null;
        }
    }

    /**
     * Decrypt a secure Base64 AES string coming from the Frontend back into plain text
     */
    public String decodeString(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) return null;
        try {
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(AES_ENCRYPTION_KEY.getBytes(), AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes);
        } catch (Exception e) {
            System.err.println("Error decrypting string from FE: " + e.getMessage());
            return null;
        }
    }
}