package com.dinethdf.ClinicManagementSystem.Service.ClinicStaff;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.ClinicStaff.ClinicStaffResponseDto;
import com.dinethdf.ClinicManagementSystem.DTO.ClinicStaff.ClinicStaffUpdateDto;
import com.dinethdf.ClinicManagementSystem.Model.ClinicStaff.ClinicStaff;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

public interface ClinicStaffService {

    ClinicStaffResponseDto getStaffById(Long id);

    @Transactional
    ApiResponse<ClinicStaff> registerStaff(ClinicStaff staff);

    @Transactional(readOnly = true)
    Page<ClinicStaffResponseDto> getAllStaffPaginated(int page, int size);

    // --- 2. UPDATE STAFF ---
    @Transactional
    ClinicStaffResponseDto editStaffDetails(Long staffId, ClinicStaffUpdateDto requestDto);
}
