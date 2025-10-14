package com.dinethdf.ClinicManagementSystem.Service.Patient;

import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientRegistrationDto;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientResponseDto;
import com.dinethdf.ClinicManagementSystem.Model.Patient.PatientDetails;

import java.util.List;

public interface PatientService {

    /**
     * Get all patients (for admin/staff)
     */
    List<PatientResponseDto> getAllPatients();

    /**
     * Get patient details by patient ID
     */
    PatientResponseDto getPatientById(Long patientId);

    /**
     * Get patient details by Auth ID (linked to login/user)
     */
    PatientResponseDto getPatientByAuthId(Long authId);

    /**
     * Register patient details (self-service by patient)
     */
    PatientResponseDto registerPatientDetailsSelf(PatientRegistrationDto requestDto);

    /**
     * Add or update patient details by staff/admin
     */
    PatientDetails registerPatientByStaff(PatientDetails requestDto);

    /**
     * Edit patient details (patient or staff, depending on roles)
     */
    PatientResponseDto editPatientDetails(Long patientId, PatientRegistrationDto requestDto);

    /**
     * Approve patient (e.g., after staff verification)
     */
    PatientResponseDto approvePatient(Long patientId);

    /**
     * Delete patient details (admin only)
     */
    void deletePatient(Long patientId);


}
