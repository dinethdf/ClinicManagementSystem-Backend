package com.dinethdf.ClinicManagementSystem.Model.Clinic;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity
@Table(name = "clinics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Clinic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String subdomain;

    // Contact Info
    private String address;
    private String city;
    private String district;
    private String state;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "mobile_no")
    private String mobileNo;

    @Column(name = "land_no")
    private String landNo;

    private String email;
    private String website;

    @Column(name = "logo_url")
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    private ClinicStatus status = ClinicStatus.TRIAL;

    // --- NEW: Relationship to Plan ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_plan_id", nullable = false)
    private SubscriptionPlan subscriptionPlan;

    @Column(name = "settings", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private ClinicSettings settings = new ClinicSettings();

    // Limits (Overrides)
    @Column(name = "max_doctors")
    private Integer maxDoctors; // Nullable (if null, use plan default)

    @Column(name = "max_patients")
    private Integer maxPatients;

    // Auditing
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.settings == null) {
            this.settings = new ClinicSettings(); // Default empty settings
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum ClinicStatus {
        TRIAL, ACTIVE, SUSPENDED, EXPIRED, CANCELLED
    }
}