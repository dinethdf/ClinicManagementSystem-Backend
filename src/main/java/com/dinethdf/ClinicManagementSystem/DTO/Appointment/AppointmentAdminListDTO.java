package com.dinethdf.ClinicManagementSystem.DTO.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;

public interface AppointmentAdminListDTO {
    Long getId();
    String getAppointmentNumber();
    Integer getQueuePosition();
    String getStatus();
    String getNote();

    LocalDate getScheduleDate();
    LocalTime getStartTime();

    String getDoctorName();

    Long getPatientId();
    String getPatientNo();
    String getPatientName();
    String getPatientMobile();
}