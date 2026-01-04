package com.dinethdf.ClinicManagementSystem.DTO.Lab;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LabTestTypeResponseDTO {
    private Integer id;
    private String name;
    private String code;
    private String description;

    private BigDecimal testFee;
    private BigDecimal clinicFee;
    private BigDecimal totalPrice; // Calculated (testFee + clinicFee)

    private Boolean isActive;

    private LocalDateTime createdDate;
    private String createdBy;
}