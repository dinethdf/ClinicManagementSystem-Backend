package com.dinethdf.ClinicManagementSystem.Model.Clinic;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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

    private String address;
    private String city;
    private String district;

    @Column(name = "mobile_no")
    private String mobileNo;

    @Column(name = "land_no")
    private String landNo;

    private String email;

    @Column(name = "logo_url")
    private String logoUrl;

    private String website;

    @Enumerated(EnumType.STRING)
    private ClinicStatus status = ClinicStatus.ACTIVE;

    @Column(name = "subscription_plan")
    private String subscriptionPlan = "BASIC";

    @Column(name = "max_doctors")
    private Integer maxDoctors = 5;

    @Column(name = "max_patients")
    private Integer maxPatients = 10000;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    public enum ClinicStatus {
        ACTIVE, SUSPENDED, EXPIRED
    }
}