package com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduleCancelResponseDTO {
    private boolean success;
    private String message;
    private int totalAffected;
    private List<AffectedPatientDTO> affectedPatients;
}
