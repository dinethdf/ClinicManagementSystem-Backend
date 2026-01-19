package com.dinethdf.ClinicManagementSystem.Model.Patient;

import com.dinethdf.ClinicManagementSystem.Model.CommonUserDetails; // Assuming you have base class or just use standard fields
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "patient_allergies")
@Data
public class PatientAllergy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "clinic_id", nullable = false)
    private Integer clinicId;

    @Column(name = "patient_id", nullable = false)
    private Integer patientId;

    @Column(name = "allergyName", nullable = false)
    private String allergyName;

    @Column(name = "notes")
    private String notes;
}