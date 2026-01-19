package com.dinethdf.ClinicManagementSystem.Model.Patient;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "patient_chronic_diseases")
@Data
public class PatientChronicDisease {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "clinic_id", nullable = false)
    private Integer clinicId;

    @Column(name = "patient_id", nullable = false)
    private Integer patientId;

    @Column(name = "diseaseName", nullable = false)
    private String diseaseName;

    @Column(name = "notes")
    private String notes;
}