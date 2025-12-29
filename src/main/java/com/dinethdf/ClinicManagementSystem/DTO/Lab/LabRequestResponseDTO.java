package com.dinethdf.ClinicManagementSystem.DTO.Lab;

import com.dinethdf.ClinicManagementSystem.Model.Lab.PatientLabRequest.LabStatus;
import com.dinethdf.ClinicManagementSystem.Model.Lab.PatientLabRequest.PaymentStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LabRequestResponseDTO {

    private Integer id;

    // Patient Info
    private Integer patientId;
    private String patientName;
    private String patientMobile;

    // Test Info
    private Integer labTestTypeId;
    private String testName;
    private String testCode;

    // Origin Info
    private Integer appointmentId;
    private String prescribedBy;   // "Dr. Name" or "Walk-in"

    // Billing Info
    private Integer invoiceId;
    private BigDecimal billedAmount;
    private PaymentStatus paymentStatus;

    // Lab Status & Results
    private LabStatus status;
    private String technicianNotes;
    private String reportUrl;

    // Audit
    private LocalDateTime createdDate;
    private String createdBy;
    private LocalDateTime completedDate;
}