package com.dinethdf.ClinicManagementSystem.Model.Clinic;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity
@Immutable // Read-Only
@Table(name = "v_clinic_data") // Maps to your SQL VIEW
@Data
public class ClinicView {

    @Id
    @Column(name = "clinic_id")
    private Integer id;

    @Column(name = "clinic_name")
    private String name;

    private String subdomain;
    private String email;

    @Column(name = "mobile_no")
    private String mobileNo;

    @Column(name = "logo_url")
    private String logoUrl;

    private String status;

    // JSON Settings (Read directly from View)
    @Column(name = "settings", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private ClinicSettings settings;

    // Plan Info (Joined in the View)
    @Column(name = "plan_name")
    private String planName;

    // Effective Limits (Calculated in the View)
    @Column(name = "max_doctors")
    private Integer maxDoctors;

    @Column(name = "max_sms_monthly")
    private Integer maxSmsMonthly;

    // Calculated Status
    @Column(name = "days_remaining")
    private Integer daysRemaining;

    @Column(name = "actual_status")
    private String actualStatus;
}