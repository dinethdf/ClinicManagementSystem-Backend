package com.dinethdf.ClinicManagementSystem.DTO.Appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.math.BigDecimal;

public interface AppointmentFullDetailsDTO {
    Long getId();
    Integer getQueuePosition();
    String getNote();

    // --- UPDATED STATUS INFO ---
    String getStatus();              // Returns Code (e.g., "BOOKED")
    String getStatusDisplayName();   // Returns Name (e.g., "Booked")
    String getStatusColor();         // Returns Color (e.g., "#007bff")

    // Schedule Info
    Long getScheduleId();
    LocalDate getScheduleDate();
    LocalTime getStartTime();
    LocalTime getEndTime();
    BigDecimal getDoctorFee();
    BigDecimal getClinicFee();

    // Patient Info
    Long getPatientId();
    String getPatientName();
    String getPatientNo();
    String getPatientMobile();
    String getPatientNic();

    // Doctor Info
    Long getDoctorId();
    String getDoctorName();
    String getSpecialization();

    // --- UPDATED INVOICE INFO ---
    BigDecimal getInvoiceTotalDiscount();
    BigDecimal getInvoiceFinalAmount();
    BigDecimal getAmountPaid();
    BigDecimal getBalanceRemaining();

    String getInvoiceStatus();            // Returns Code (e.g., "PAID")
    String getInvoiceStatusDisplayName(); // Returns Name (e.g., "Paid")
    String getInvoiceStatusColor();       // Returns Color (e.g., "#28a745")

    // System Columns
    LocalDateTime getCreatedDate();
    String getCreatedBy();
    LocalDateTime getUpdatedDate();
    String getUpdatedBy();
    LocalDateTime getApprovedDate();
    String getApprovedBy();
}