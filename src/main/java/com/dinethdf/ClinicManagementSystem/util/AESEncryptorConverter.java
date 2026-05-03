package com.dinethdf.ClinicManagementSystem.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Converter
public class AESEncryptorConverter implements AttributeConverter<String, String> {

    // 🚨 In production, load this from an Environment Variable!
    // Must be exactly 16, 24, or 32 characters long for AES.
    private static final String AES_ENCRYPTION_KEY = "MySuperSecretKey1234567890123456";
    private static final String AES_ALGORITHM = "AES";

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            SecretKeySpec key = new SecretKeySpec(AES_ENCRYPTION_KEY.getBytes(), AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(attribute.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            SecretKeySpec key = new SecretKeySpec(AES_ENCRYPTION_KEY.getBytes(), AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(Base64.getDecoder().decode(dbData)));
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }
}