package com.dinethdf.ClinicManagementSystem.Service.Lab;

import com.dinethdf.ClinicManagementSystem.DTO.Lab.LabTestTypeCreateDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Lab.LabTestTypeResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Lab.LabTestTypeUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LabTestTypeService {

    // 1. Create a new Test Type (e.g., "Full Blood Count")
    LabTestTypeResponseDTO createTestType(LabTestTypeCreateDTO createDTO);

    // 2. List All Tests (Admin View - includes Inactive)
    Page<LabTestTypeResponseDTO> getAllTestTypes(String search, Pageable pageable);

    // 3. List Active Tests Only (For Doctor/Reception Dropdowns)
    Page<LabTestTypeResponseDTO> getActiveTestTypes(Pageable pageable);

    // 4. Update Test Details (Name, Price, etc.)
    LabTestTypeResponseDTO updateTestType(Integer id, LabTestTypeUpdateDTO updateDTO);

    // 5. Activate / Deactivate a Test
    LabTestTypeResponseDTO toggleStatus(Integer id);
}