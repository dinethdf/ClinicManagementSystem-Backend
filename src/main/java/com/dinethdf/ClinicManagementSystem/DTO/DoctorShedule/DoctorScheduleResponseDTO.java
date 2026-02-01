package com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class DoctorScheduleResponseDTO {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private String specialization;

    private LocalDate scheduleDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private BigDecimal doctorFee;
    private BigDecimal clinicFee;

    private Integer maxPatientCount;
    private Integer registeredPatientCount;

    // --- UPDATED FIELDS ---

    // 1. Replaced 'ScheduleStatus' Enum with Rich Object
    private StatusDTO status;

    // 2. Replaced 'BookingStatus' Enum with Boolean
    private Boolean isBookingOpen;

    // 3. Replaced 'PayoutStatus' Enum with Rich Object
    private StatusDTO payoutStatus;

    // --- Helper Class for Statuses (Code + Color) ---
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StatusDTO {
        private String code;        // e.g., "ACTIVE"
        private String displayName; // e.g., "Active Schedule"
        private String colorCode;   // e.g., "#28a745"
    }
}