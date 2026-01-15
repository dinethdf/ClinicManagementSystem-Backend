package com.dinethdf.ClinicManagementSystem.Service.Doctor;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Doctor.DoctorDetailsDto;
import com.dinethdf.ClinicManagementSystem.DTO.Doctor.DoctorRegistrationDto;
import com.dinethdf.ClinicManagementSystem.DTO.Doctor.DoctorResponseDto;
import com.dinethdf.ClinicManagementSystem.DTO.Doctor.DoctorUpdateDto;
import com.dinethdf.ClinicManagementSystem.Model.Doctor.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

public interface DoctorService {

    /**
     * Get all patients (for admin/staff)
     */
    List<Doctor> getAllDoctors();


    DoctorDetailsDto getDoctorDetailsByIdForLoging(Long id, Integer clinicId);

    ApiResponse<DoctorDetailsDto> getDoctorFullDetailsById(String id);

    ApiResponse<DoctorResponseDto> getDoctorDetailsByEmail(String email);

    Page<DoctorResponseDto> getAllDoctorsPaginated(
            String name,
            String mobile,
            String slmc,
            String specialization,
            Pageable pageable
    );

    List<DoctorResponseDto> searchDoctors(String query);

    ApiResponse<Doctor> registerDoctor(Doctor doctor);


    DoctorResponseDto editDoctorDetails(Long doctorId, DoctorUpdateDto requestDto);

    void deleteDoctor(Long doctorId);


    DoctorResponseDto updateDoctorStatus(Long doctorId, String statusStr);
}
