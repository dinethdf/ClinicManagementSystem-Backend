package com.dinethdf.ClinicManagementSystem.Model.SystemConfig;

import jakarta.persistence.*;

@Entity
@Table(
        name = "systemConfig",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"clinic_id", "configKey"}) // Ensures Key is unique per Clinic
        }
)
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Simple ID to handle duplicates across clinics

    @Column(name = "clinic_id", nullable = false)
    private Integer clinicId;

    @Column(name = "configKey", nullable = false)
    private String key;

    @Column(name = "configValue")
    private String value;

    public SystemConfig() {}

    public SystemConfig(Integer clinicId, String key, String value) {
        this.clinicId = clinicId;
        this.key = key;
        this.value = value;
    }

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getClinicId() { return clinicId; }
    public void setClinicId(Integer clinicId) { this.clinicId = clinicId; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}