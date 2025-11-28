package com.dinethdf.ClinicManagementSystem.mapper;

import com.dinethdf.ClinicManagementSystem.DTO.Doctor.DoctorDetailsDto;
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

    public DoctorDetailsDto toDetailsDto(Doctor doctor) {
        if (doctor == null) {
            return null;
        }

        DoctorDetailsDto dto = new DoctorDetailsDto();

        // --- Basic Info ---
        dto.setId(doctor.getId());
        dto.setFirstName(doctor.getFirstName());
        dto.setLastName(doctor.getLastName());
        dto.setNameWithInitials(doctor.getNameWithInitials());
        dto.setEmail(doctor.getEmail());
        dto.setMobileNo(doctor.getMobileNo());
        dto.setNic(doctor.getNic());
        dto.setDob(doctor.getDob());
        dto.setGender(doctor.getGender());
        dto.setAddress(doctor.getAddress());
        dto.setCity(doctor.getCity());
        dto.setDistrict(doctor.getDistrict());
        dto.setSlmcNumber(doctor.getSlmcNumber());

        // --- Professional Info ---
        dto.setSpecialization(doctor.getSpecialization());
        dto.setQualification(doctor.getQualification());
        dto.setExperienceYears(doctor.getExperienceYears());
        dto.setProfileImg(doctor.getProfileImg());
        dto.setIsApproved(doctor.getIsApproved());
        dto.setAvailabilityStatus(doctor.getAvailabilityStatus().toString());

        // --- System Fields ---
        dto.setCreatedDate(doctor.getCreatedDate());
        dto.setCreatedBy(doctor.getCreatedBy());
        dto.setUpdatedDate(doctor.getUpdatedDate());
        dto.setUpdatedBy(doctor.getUpdatedBy());
        dto.setApprovedDate(doctor.getApprovedDate());
        dto.setApprovedBy(doctor.getApprovedBy());

        return dto;
    }
}