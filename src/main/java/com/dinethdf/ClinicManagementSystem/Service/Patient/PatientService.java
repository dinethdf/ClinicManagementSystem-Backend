package com.dinethdf.ClinicManagementSystem.Service.Patient;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.ClinicStaff.ClinicStaffResponseDto;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.GuardianResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientListDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientSimpleResponseDTO;
import com.dinethdf.ClinicManagementSystem.Model.Patient.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PatientService {

    /**
     * Get all patients (for admin/staff)
     */
    List<Patient> getAllPatients();

    ApiResponse<Patient> getPatientDetailsByIdAll(String id);

    PatientResponseDTO getPatientByIdForLogin(Long id, Integer clinicId);

    ApiResponse<Patient> getPatientDetailsByIdStaff(String id);

    List<GuardianResponseDTO> findGuardianByNicAndFullName(String nic, String fullName);

    List<PatientResponseDTO> findPatientBySearching(String patientNo, String nic, String fullName, String mobileNo);

    @Transactional(readOnly = true)
    Page<PatientListDTO> getAllPatientsPaginated(String search, Pageable pageable);

    /**
     * Add patient details by staff/admin
     */
    ApiResponse<Patient> registerPatientByStaff(Patient requestDto);
    /**
     * Update patient details by staff/admin
     */
    ApiResponse<Patient> updatePatientByStaff(Integer id, Patient patient);

    ApiResponse<Patient> updatePatientBySelf(Long id, Patient patientFromRequest);

    /**
     * Approve patient (e.g., after staff verification)
     */
    ApiResponse<String> approvePatient(Integer patientId) ;

    ApiResponse<String> togglePatientStatus(Integer patientId);

    ApiResponse<String> deletePatient(Integer patientId);


    List<PatientSimpleResponseDTO> findPatientOnlyByNo(String patientNo);

    Long findExistingPatientIdByNic(String nic);

    PatientResponseDTO findPatientDetailsByNic(String nic);
}
