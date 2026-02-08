package com.dinethdf.ClinicManagementSystem.DTO.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;

public interface AppointmentAdminListDTO {
    Long getId();
    Integer getQueuePosition();

    // --- Full Status Info ---
    String getStatus();              // e.g. "BOOKED"
    String getStatusDisplayName();   // e.g. "Booked"
    String getStatusColor();         // e.g. "#28a745"

    String getNote();

    LocalDate getScheduleDate();
    LocalTime getStartTime();

    String getDoctorName();

    Long getPatientId();
    String getPatientNo();
    String getPatientName();
    String getPatientMobile();
}