package com.dinethdf.ClinicManagementSystem.Model.SystemConfig;

import jakarta.persistence.*;

@Entity
@Table(name = "systemConfig")
public class SystemConfig {

    @Id
    @Column(name = "configKey", unique = true, nullable = false)
    private String key;

    @Column(name = "configValue")
    private String value;

    public SystemConfig() {}
    public SystemConfig(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}