package com.dinethdf.ClinicManagementSystem.DTO.Appointment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public interface PatientAppointmentDTO {

    // Appointment Details
    Long getAppointmentId();
    Integer getQueuePosition(); // Important for the patient to know their turn
    String getStatus(); // e.g., Booked, Completed

    // Schedule Details
    LocalDate getScheduleDate();
    LocalTime getStartTime();

    // Doctor Details
    String getDoctorFirstName();
    String getDoctorLastName();
    String getDoctorFullName(); // We can concat this in SQL
    String getSpecialization(); // Helpful context

    // Financial Details (Optional but usually needed)
    BigDecimal getDoctorFee();
    BigDecimal getClinicFee();
    BigDecimal getTotalFee(); // Calculated in SQL
}