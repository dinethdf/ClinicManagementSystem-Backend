package com.dinethdf.ClinicManagementSystem.Model.Clinic;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "plan_code", unique = true, nullable = false)
    private String planCode; // e.g., "PLAN_BASIC"

    @Column(name = "plan_name", nullable = false)
    private String planName; // e.g., "Basic Plan"

    private String description;

    // --- Limits ---
    @Column(name = "max_doctors")
    private Integer maxDoctors;

    @Column(name = "max_patients")
    private Integer maxPatients;

    @Column(name = "max_storage_mb")
    private Integer maxStorageMb;

    @Column(name = "max_sms_monthly")
    private Integer maxSmsMonthly;

    @Column(name = "max_email_monthly")
    private Integer maxEmailMonthly;

    @Column(name = "trial_days")
    private Integer trialDays;

    // --- Pricing ---
    @Column(name = "monthly_price")
    private BigDecimal monthlyPrice;

    @Column(name = "yearly_price")
    private BigDecimal yearlyPrice;

    private String currency;

    // --- Status ---
    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Column(name = "display_order")
    private Integer displayOrder;

    // --- Auditing ---
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}