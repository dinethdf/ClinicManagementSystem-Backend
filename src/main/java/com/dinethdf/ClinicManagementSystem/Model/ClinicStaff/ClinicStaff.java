package com.dinethdf.ClinicManagementSystem.Model.ClinicStaff;

import com.dinethdf.ClinicManagementSystem.Model.CommonUserDetails;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserAuth;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "clinic_staff")
@Data
@EqualsAndHashCode(callSuper = true)
public class ClinicStaff extends CommonUserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "auth_id", referencedColumnName = "id")
    private UserAuth userAuth;

    private String nameWithInitials;

    private LocalDateTime joinDate;

    private String designation;

    @Enumerated(EnumType.STRING)
    private Status status; // Enum: ACTIVE, INACTIVE


    @Transient
    private Integer assignedRoleId;
}