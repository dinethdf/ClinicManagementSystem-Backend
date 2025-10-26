package com.dinethdf.ClinicManagementSystem.Service.Doctor;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Doctor.DoctorRegistrationDto;
import com.dinethdf.ClinicManagementSystem.DTO.Doctor.DoctorResponseDto;
import com.dinethdf.ClinicManagementSystem.Model.Doctor.Doctor;


import java.util.List;

public interface DoctorService {

    /**
     * Get all patients (for admin/staff)
     */
    List<DoctorResponseDto> getAllDoctors();

    /**
     * Get patient details by patient ID
     */
    DoctorResponseDto getDoctorById(Long doctorId);

    ApiResponse<DoctorResponseDto> getDoctorFullDetailsById(String id);

    ApiResponse<DoctorResponseDto> getDoctorDetailsByEmail(String email);

    List<DoctorResponseDto> searchDoctors(String query);

    /**
     * Register patient details (self-service by patient)
     */
    DoctorResponseDto registerDoctorDetailsSelf(DoctorRegistrationDto requestDto);

    /**
     * Add or update patient details by staff/admin
     */
    ApiResponse<Doctor> registerDoctor(Doctor doctor);

    /**
     * Edit patient details (patient or staff, depending on roles)
     */
    DoctorResponseDto editDoctorDetails(Long patientId, DoctorRegistrationDto requestDto);

    /**
     * Approve patient (e.g., after staff verification)
     */
    DoctorResponseDto approveDoctor(Long doctorId);

    /**
     * Delete patient details (admin only)
     */
    void deleteDoctor(Long doctorId);


}
