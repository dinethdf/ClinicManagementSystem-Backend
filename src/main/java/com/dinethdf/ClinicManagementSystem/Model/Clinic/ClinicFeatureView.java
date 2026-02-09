package com.dinethdf.ClinicManagementSystem.Model.Clinic;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "v_clinic_features") // Maps to your SQL VIEW
@Data
public class ClinicFeatureView {

    @Id // Composite ID simulation (since views don't have real IDs)
    @Column(name = "feature_id")
    private Integer featureId;

    @Column(name = "clinic_id")
    private Integer clinicId;

    @Column(name = "feature_code")
    private String featureCode;

    @Column(name = "feature_name")
    private String featureName;

    private String category;

    @Column(name = "is_enabled")
    private boolean enabled;

    // You can add 'display_order' if needed for sorting
}