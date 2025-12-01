package com.dinethdf.ClinicManagementSystem.util;

import org.springframework.stereotype.Component;

@Component
public class MobileFormatUtil {

    public String formatToLankaInternational(String mobile) {
        if (mobile == null) return "";

        // Remove spaces and "+" signs
        mobile = mobile.replaceAll("\\s+", "").replace("+", "");

        // Must be digits only
        if (!mobile.matches("\\d+")) {
            throw new IllegalArgumentException("Invalid mobile number: " + mobile);
        }

        // Case 1: Already correct format → 9477XXXXXXX
        if (mobile.startsWith("94") && mobile.length() == 11) {
            return mobile;
        }

        // Case 2: Local format 077XXXXXXX → convert
        if (mobile.startsWith("0") && mobile.length() == 10) {
            return "94" + mobile.substring(1);
        }

        // Case 3: 7XXXXXXXX → convert
        if (mobile.startsWith("7") && mobile.length() == 9) {
            return "94" + mobile;
        }

        throw new IllegalArgumentException("Unsupported mobile number format: " + mobile);
    }
}
