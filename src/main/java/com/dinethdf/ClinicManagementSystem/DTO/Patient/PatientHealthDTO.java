package com.dinethdf.ClinicManagementSystem.DTO.Patient;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

public class PatientHealthDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllergyDTO {
        private Integer id;
        private String allergyName;
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChronicDiseaseDTO {
        private Integer id;
        private String diseaseName;
        private String notes;
    }
}