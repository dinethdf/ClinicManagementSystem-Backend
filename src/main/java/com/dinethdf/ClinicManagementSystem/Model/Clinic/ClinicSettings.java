package com.dinethdf.ClinicManagementSystem.Model.Clinic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClinicSettings {

    private String currency;            // LKR, USD, etc.
    private String timezone;            // Asia/Colombo
    private String invoicePrefix;       // ML
    private Integer taxRate;             // 12
    private Boolean multiCurrency;       // true / false
    private String language;             // en
    private String secondaryLanguage;    // si

    // Optional: sensible defaults
    public static ClinicSettings defaultSettings() {
        return new ClinicSettings(
                "LKR",
                "Asia/Colombo",
                "INV",
                0,
                false,
                "en",
                null
        );
    }
}
