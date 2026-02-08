package com.dinethdf.ClinicManagementSystem.DTO.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;

public interface AppointmentByScheduleListDTO {
    Long getId();
    Integer getQueuePosition();
    String getStatus();
    String getStatusDisplayName();
    String getStatusColor();
    String getNote();
    LocalDate getScheduleDate();
    LocalTime getStartTime();
    Long getPatientId();
    String getPatientNo();
    String getPatientName();
    String getPatientMobile();
}