package com.dinethdf.ClinicManagementSystem.Model.Prescription;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "prescription_items")
public class PrescriptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    @JsonIgnore
    private Prescription prescription;

    @Column(nullable = false)
    private String medicineName;

    private String dosage;
    private String quantity;
    private String duration;

    @Column(columnDefinition = "TEXT")
    private String instructions;
}