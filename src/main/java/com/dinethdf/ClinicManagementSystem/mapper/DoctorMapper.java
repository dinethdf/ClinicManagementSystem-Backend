package com.dinethdf.ClinicManagementSystem.mapper;

import com.dinethdf.ClinicManagementSystem.DTO.Doctor.DoctorResponseDto;
import com.dinethdf.ClinicManagementSystem.Model.Doctor.Doctor;
import org.springframework.stereotype.Component;

@Component
public class DoctorMapper {

    public DoctorResponseDto toResponseDTO(Doctor doctor) {
        if (doctor == null) {
            return null;
        }

        DoctorResponseDto dto = new DoctorResponseDto();

        // --- Fields from Doctor entity ---
        dto.setId(doctor.getId());
        dto.setNameWithInitials(doctor.getNameWithInitials());
        dto.setSlmcNumber(doctor.getSlmcNumber());
        dto.setSpecialization(doctor.getSpecialization());
        dto.setQualification(doctor.getQualification());
        dto.setExperienceYears(doctor.getExperienceYears());

        // --- Fields from CommonUserDetails (parent class) ---
        dto.setFirstName(doctor.getFirstName());
        dto.setLastName(doctor.getLastName());
        dto.setEmail(doctor.getEmail());
        dto.setMobileNo(doctor.getMobileNo());
        dto.setNic(doctor.getNic());
        dto.setDob(doctor.getDob());
        dto.setProfileImg(doctor.getProfileImg());

        return dto;
    }
}