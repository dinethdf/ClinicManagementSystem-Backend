package com.dinethdf.ClinicManagementSystem.Model.Lab;

import com.dinethdf.ClinicManagementSystem.Model.AuditWrapper;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "lab_test_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true) // Includes AuditWrapper fields in equals/hashcode
public class LabTestType extends AuditWrapper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "clinic_id", nullable = false)
    private Integer clinicId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Financials
    @Column(name = "test_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal testFee = BigDecimal.ZERO;

    @Column(name = "clinic_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal clinicFee = BigDecimal.ZERO;

    @Column(name = "isActive")
    private Boolean isActive = true;

}