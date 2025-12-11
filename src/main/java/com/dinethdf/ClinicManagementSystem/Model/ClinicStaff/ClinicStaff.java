package com.dinethdf.ClinicManagementSystem.Model.ClinicStaff;

import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserAuth;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "clinic_staff")
@Data
public class ClinicStaff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "auth_id", referencedColumnName = "id")
    private UserAuth userAuth;

    private String firstName;
    private String lastName;
    private String nameWithInitials;
    private Integer gender;
    private LocalDate dob;
    private String nic;
    private String email;
    private String mobileNo;
    private String address;
    private String city;
    private String district;
    private LocalDateTime joinDate;
    private String profileImg;
    private String designation;

    @Enumerated(EnumType.STRING)
    private Status status; // Enum: ACTIVE, INACTIVE

    // --- System Columns ---
    private LocalDateTime createdDate;
    private String createdBy;
    private LocalDateTime updatedDate;
    private String updatedBy;

    // --- Dynamic Role Handling ---
    // This field is used only for Registration input. It is not stored in this table.
    @Transient
    private Integer assignedRoleId;
}