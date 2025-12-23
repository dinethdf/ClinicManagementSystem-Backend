package com.dinethdf.ClinicManagementSystem.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@MappedSuperclass
@EqualsAndHashCode(callSuper = true)
public abstract class CommonUserDetails extends AuditWrapper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "firstName", nullable = false, length = 50)
    private String firstName;

    @Column(name = "lastName", nullable = false, length = 50)
    private String lastName;

    @Column(name = "gender", nullable = false)
    private Integer gender;

    @Column(name = "nic", nullable = true, unique = true, length = 20)
    private String nic;

    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Column(name = "email", nullable = true, unique = true)
    private String email;

    @Column(name = "mobileNo", nullable = true, length = 20)
    private String mobileNo;

    @Column(name = "address", nullable = true, columnDefinition = "TEXT")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "district")
    private String district;

    @Column(name = "profileImg", length = 255)
    private String profileImg;

    @Column(name = "clinic_id", nullable = false)
    private Integer clinicId;
}
