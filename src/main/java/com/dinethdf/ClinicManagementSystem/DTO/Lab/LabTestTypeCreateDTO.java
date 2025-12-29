package com.dinethdf.ClinicManagementSystem.DTO.Lab;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LabTestTypeCreateDTO {

    private String name;
    private String code;
    private String description;

    // Validation will be done in Service: check if null or < 0
    private BigDecimal testFee;
    private BigDecimal clinicFee;
}