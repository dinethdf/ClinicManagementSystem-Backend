package com.dinethdf.ClinicManagementSystem.DTO.Patient;

public record GuardianResponseDTO(
        Long id,
        String patientNo,
        String firstName,
        String lastName,
        String nic,
        String address,
        String city,
        String district,
        String email,
        String mobileNo,
        String landNo
) {}