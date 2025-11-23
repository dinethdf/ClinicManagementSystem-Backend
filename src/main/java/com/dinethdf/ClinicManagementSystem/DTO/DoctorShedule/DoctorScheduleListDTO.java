package com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
public class DoctorScheduleListDTO {
    private Long id;
    private String doctorName;
    private String specialization;
    private LocalDate scheduleDate;
    private String timeRange; // e.g. "08:00 - 12:00"
    private int registeredCount;
    private int maxCount;
    private boolean isActive;

    public DoctorScheduleListDTO(Long id, String firstName, String lastName, String specialization,
                                 LocalDate scheduleDate, LocalTime startTime, LocalTime endTime,
                                 int registeredCount, int maxCount, boolean isActive) {
        this.id = id;
        this.doctorName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
        this.specialization = specialization;
        this.scheduleDate = scheduleDate;
        this.timeRange = startTime + " - " + endTime;
        this.registeredCount = registeredCount;
        this.maxCount = maxCount;
        this.isActive = isActive;
    }
}