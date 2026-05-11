package com.dinethdf.ClinicManagementSystem.Model.Clinic;

import com.dinethdf.ClinicManagementSystem.Model.AuditWrapper;
import com.dinethdf.ClinicManagementSystem.config.multitenant.TenantSchemaMode;
import com.dinethdf.ClinicManagementSystem.util.AESEncryptorConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "clinics")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Clinic extends AuditWrapper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "clinic_code", nullable = false, unique = true, length = 36)
    private String clinicCode;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String subdomain;

    private String email;

    // --- UPDATED CONTACT FIELDS START ---
    @Column(name = "mobile_no_1", length = 20)
    private String mobileNo1;

    @Column(name = "mobile_no_2", length = 20)
    private String mobileNo2;

    @Column(name = "landline_no", length = 20)
    private String landlineNo;
    // --- UPDATED CONTACT FIELDS END ---

    private String address;
    private String city;
    private String district;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "plan_code", nullable = false)
    private String planCode; // "PLAN_BASIC", "PLAN_PRO", "PLAN_ENTERPRISE"

    @Enumerated(EnumType.STRING)
    private ClinicStatus status = ClinicStatus.TRIAL;

    @Column(name = "registered_at", updatable = false)
    private LocalDateTime registeredAt;

    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;

    @Column(name = "subscription_ends_at")
    private LocalDateTime subscriptionEndsAt;

    @Column(name = "settings", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private ClinicSettings settings;

    @Column(name = "clinic_secret", length = 20)
    private String clinicSecret;

    @Column(name = "is_deleted")
    private boolean deleted = false;

    @Column(name = "encrypted_secrets", columnDefinition = "TEXT")
    @Convert(converter = AESEncryptorConverter.class)
    private String encryptedSecrets;

    /**
     * Controls how this clinic's data is stored:
     *   SHARED → uses clinicmsystem (existing shared DB, table-level isolation via clinic_id)
     *   SCHEMA → uses its own dedicated database: clinic_{clinicCode}
     *
     * Defaults to SHARED — all existing clinics continue working without any change.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tenant_mode", nullable = false, length = 10)
    private TenantSchemaMode tenantMode = TenantSchemaMode.SHARED;

    public String getClinicSecret() { return clinicSecret; }
    public void setClinicSecret(String clinicSecret) { this.clinicSecret = clinicSecret; }

    public String getEncryptedSecrets() { return encryptedSecrets; }
    public void setEncryptedSecrets(String encryptedSecrets) { this.encryptedSecrets = encryptedSecrets; }

    @Override
    @PrePersist
    public void prePersist() {
        super.prePersist();
        this.registeredAt = LocalDateTime.now();
        if (this.settings == null) {
            this.settings = new ClinicSettings();
        }
    }

    public enum ClinicStatus {
        TRIAL, ACTIVE, SUSPENDED, EXPIRED, CANCELLED
    }
}