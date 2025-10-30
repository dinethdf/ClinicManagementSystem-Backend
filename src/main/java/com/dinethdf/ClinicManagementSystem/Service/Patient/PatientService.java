package com.dinethdf.ClinicManagementSystem.Service.Patient;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.GuardianResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientResponseDTO;
import com.dinethdf.ClinicManagementSystem.Model.Patient.Patient;

import java.util.List;

public interface PatientService {

    /**
     * Get all patients (for admin/staff)
     */
    List<Patient> getAllPatients();

    ApiResponse<Patient> getPatientDetailsByIdStaff(String id);

    List<GuardianResponseDTO> findGuardianByNicAndFullName(String nic, String fullName);

    List<PatientResponseDTO> findPatientBySearching(String patientNo, String nic, String fullName, String mobileNo);

    /**
     * Add patient details by staff/admin
     */
    ApiResponse<Patient> registerPatientByStaff(Patient requestDto);
    /**
     * Update patient details by staff/admin
     */
    ApiResponse<Patient> updatePatientByStaff(Integer id, Patient patient);
    /**
     * Approve patient (e.g., after staff verification)
     */
    ApiResponse<String> approvePatient(Integer patientId) ;

    /**
     * Delete patient details (admin only)
     */
    ApiResponse<String> deletePatient(Integer patientId);


}
