package com.dinethdf.ClinicManagementSystem.Model.Prescription;

import com.dinethdf.ClinicManagementSystem.Model.Appointment.Appointment;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "prescriptions", indexes = {
        @Index(name = "idx_prescription_clinic", columnList = "clinic_id")
})
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clinic_id", nullable = false)
    private Integer clinicId; // <--- Multi-Tenant Field

    // Relationship to Appointment
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('ISSUED', 'DISPENSED', 'CANCELLED') DEFAULT 'ISSUED'")
    private PrescriptionStatus status = PrescriptionStatus.ISSUED;

    @Column(columnDefinition = "TEXT")
    private String note;

    // --- Relationships ---
    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PrescriptionItem> items = new ArrayList<>();

    // --- Audit Fields ---
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(nullable = false, updatable = false)
    private String createdBy;

    private LocalDateTime updatedDate;
    private String updatedBy;
    private LocalDateTime approvedDate;
    private String approvedBy;


    // --- Auto-Fill Audit Fields ---
    @PrePersist
    protected void onCreate() {
        if (createdDate == null) createdDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}