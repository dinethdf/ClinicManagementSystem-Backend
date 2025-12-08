package com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule;

public interface ScheduleAnalyticsDTO {
    Long getTotal();
    Long getCompleted();
    Long getBooked();
    Long getPendingPayment();
    Long getCancelled();
}