package com.dinethdf.ClinicManagementSystem.DTO.Appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.math.BigDecimal;

public interface AppointmentFullDetailsDTO {
    Long getId();
    Integer getQueuePosition();
    String getNote();
    String getStatus();

    // Schedule Info
    Long getScheduleId();
    LocalDate getScheduleDate();
    LocalTime getStartTime();
    LocalTime getEndTime();
    BigDecimal getDoctorFee();
    BigDecimal getClinicFee();

    // Patient Info
    Long getPatientId();
    String getPatientName(); // Full Name
    String getPatientNo();
    String getPatientMobile();
    String getPatientNic();

    // Doctor Info
    String getDoctorName();
    String getSpecialization();

    // System Columns (Requested in Saved Info)
    LocalDateTime getCreatedDate();
    String getCreatedBy();
    LocalDateTime getUpdatedDate();
    String getUpdatedBy();
    LocalDateTime getApprovedDate();
    String getApprovedBy();
}