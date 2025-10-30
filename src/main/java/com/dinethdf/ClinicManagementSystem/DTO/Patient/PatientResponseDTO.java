package com.dinethdf.ClinicManagementSystem.DTO.Patient;

import com.dinethdf.ClinicManagementSystem.Model.Patient.Patient;

import java.time.LocalDate;

public record PatientResponseDTO(
        Long id,
        String patientNo,
        String firstName,
        String lastName,
        String nic,
        String address,
        LocalDate dob,
        GuardianResponseDTO guardian,
        String city,
        String district,
        String email,
        String mobileNo,
        String landNo
) {}