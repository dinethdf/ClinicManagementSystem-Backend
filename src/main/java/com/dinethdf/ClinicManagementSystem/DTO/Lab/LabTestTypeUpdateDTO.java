package com.dinethdf.ClinicManagementSystem.DTO.Lab;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LabTestTypeUpdateDTO {
    private String name;
    private String code;
    private String description;
    private BigDecimal testFee;
    private BigDecimal clinicFee;
    private Boolean isActive;
}