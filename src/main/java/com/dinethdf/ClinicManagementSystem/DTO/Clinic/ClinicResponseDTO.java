package com.dinethdf.ClinicManagementSystem.DTO.Clinic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ClinicResponseDTO {
    private Integer id;
    private String name;
    private String subdomain;

    // Contact
    private String address;
    private String city;
    private String mobileNo;
    private String landNo;
    private String email;

    // Branding
    private String logoUrl;
    private String status;         // e.g., "ACTIVE"
    private String actualStatus;   // e.g., "TRIAL_EXPIRED" (Calculated)

    // Plan Info
    private String subscriptionPlan; // Plan Name (e.g., "Pro Plan")
    private LocalDate expiryDate;
    private Integer daysRemaining;   // New field for dashboard

    // Settings (Flattened for easier frontend use, or nested if you prefer)
    private String currency;
    private String timezone;

    @JsonProperty("date_format")
    private String dateFormat;

    @JsonProperty("invoice_prefix")
    private String invoicePrefix;

    @JsonProperty("sms_otp_enabled")
    private boolean smsOtpEnabled;

    @JsonProperty("email_otp_enabled")
    private boolean emailOtpEnabled;
}