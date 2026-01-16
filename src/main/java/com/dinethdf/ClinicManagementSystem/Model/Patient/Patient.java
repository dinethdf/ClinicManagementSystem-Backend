package com.dinethdf.ClinicManagementSystem.Model.Patient;

import com.dinethdf.ClinicManagementSystem.Model.CommonUserDetails;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Entity
@Table(name = "patient")
@Data
@EqualsAndHashCode(callSuper = true)
public class Patient extends CommonUserDetails {

    @Column(name = "patientNo", nullable = true, unique = true, length = 100)
    private String patientNo;

    @Column(name = "fullName", length = 255)
    private String fullName;

    @Column(name = "ethnicity", nullable = true)
    private Integer ethnicity;

    @Column(name = "maritalStatus", nullable = true)
    private Integer maritalStatus;

    @Column(name = "landNo", nullable = true, length = 20)
    private String landNo;

    @Column(name = "emergencyContactName", nullable = true, length = 100)
    private String emergencyContactName;

    @Column(name = "emergencyContactRelationship", nullable = true, length = 50)
    private String emergencyContactRelationship;

    @Column(name = "emergencyContactMobile", nullable = true)
    private String emergencyContactMobile;

    @Column(name = "emergencyContactEmail")
    private String emergencyContactEmail;

    @Column(name = "bloodGroup", nullable = true, length = 10)
    private String bloodGroup;

    @ManyToOne
    @JoinColumn(name = "guardian_id", referencedColumnName = "id")
    private Patient guardian;

    @Column(name = "relationshipToGuardian")
    private String relationshipToGuardian;

    @Column(name = "height", nullable = true)
    private Double height; // stored in cm

    @Column(name = "weight", nullable = true)
    private Double weight; // stored in kg

    @CreationTimestamp
    @Column(name = "registrationDate", nullable = false, updatable = false)
    private LocalDateTime registrationDate;

    @Column(name = "isActive", nullable = false)
    private Boolean isActive = false;

    @Column(name = "insurance_id")
    private Integer insuranceId;

    // --- Helper methods ---
    public int getAge() {
        if (getDob() == null) return 0;
        return Period.between(getDob(), LocalDate.now()).getYears();
    }

    @Transient
    public boolean isDependent() {
        return this.guardian != null && this.guardian.getId() != null;
    }

    @Transient
    public boolean isGuardianRequired() {
        return getAge() < 18;
    }

    // ✅ FIX: Automatically convert empty strings to NULL before saving
    @PrePersist
    @PreUpdate
    private void sanitizeData() {
        if (this.bloodGroup != null && this.bloodGroup.trim().isEmpty()) {
            this.bloodGroup = null;
        }
    }
}