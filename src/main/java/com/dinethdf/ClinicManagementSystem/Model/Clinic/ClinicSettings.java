package com.dinethdf.ClinicManagementSystem.Model.Clinic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // Safer for future DB changes
public class ClinicSettings implements Serializable {

    // General
    private String timezone = "Asia/Colombo";
    private String language = "en";
    private String currency = "LKR";

    // Formatting
    @JsonProperty("date_format")
    private String dateFormat = "YYYY-MM-DD";

    // Appointment Configs
    @JsonProperty("appointment_slot_duration")
    private Integer appointmentSlotDuration = 20;

    @JsonProperty("advance_booking_days")
    private Integer advanceBookingDays = 30;

    @JsonProperty("working_hours_start")
    private String workingHoursStart = "08:00";

    @JsonProperty("working_hours_end")
    private String workingHoursEnd = "17:00";

    // Billing
    @JsonProperty("tax_rate")
    private Double taxRate = 0.0;

    @JsonProperty("invoice_prefix")
    private String invoicePrefix = "INV";

    @JsonProperty("sms_otp_enabled")
    private boolean smsOtpEnabled = true;   // Default to TRUE if missing

    @JsonProperty("email_otp_enabled")
    private boolean emailOtpEnabled = true; // Default to TRUE if missing
}