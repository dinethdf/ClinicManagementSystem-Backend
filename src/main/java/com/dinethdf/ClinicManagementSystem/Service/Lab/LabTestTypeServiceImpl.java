package com.dinethdf.ClinicManagementSystem.Service.Lab;

import com.dinethdf.ClinicManagementSystem.DTO.Lab.LabTestTypeCreateDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Lab.LabTestTypeResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Lab.LabTestTypeUpdateDTO;
import com.dinethdf.ClinicManagementSystem.Model.Lab.LabTestType;
import com.dinethdf.ClinicManagementSystem.Repo.Lab.LabTestTypeRepo;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LabTestTypeServiceImpl implements LabTestTypeService {

    private final LabTestTypeRepo labRepo;
    private final MessageSourceUtil messageSourceUtil;

    @Override
    @Transactional
    public LabTestTypeResponseDTO createTestType(LabTestTypeCreateDTO createDTO) {
        Integer clinicId = TenantContext.getCurrentTenant();

        // 1. Basic Validation
        if (createDTO.getName() == null || createDTO.getName().trim().isEmpty()) {
            throw new ValidationException("Test name is required", "NAME_REQUIRED", HttpStatus.BAD_REQUEST);
        }
        if (createDTO.getTestFee() == null || createDTO.getTestFee().doubleValue() < 0) {
            throw new ValidationException("Test fee cannot be negative", "INVALID_TEST_FEE", HttpStatus.BAD_REQUEST);
        }
        if (createDTO.getClinicFee() == null || createDTO.getClinicFee().doubleValue() < 0) {
            throw new ValidationException("Clinic fee cannot be negative", "INVALID_CLINIC_FEE", HttpStatus.BAD_REQUEST);
        }

        // 2. Duplicate Name Check
        if (labRepo.existsByNameAndClinicId(createDTO.getName(), clinicId)) {
            throw new ValidationException(
                    "Test with this name already exists",
                    "DUPLICATE_TEST_NAME",
                    HttpStatus.CONFLICT
            );
        }

        // --- 3. NEW: Duplicate Code Check ---
        if (createDTO.getCode() != null && !createDTO.getCode().isEmpty()) {
            if (labRepo.existsByCodeAndClinicId(createDTO.getCode(), clinicId)) {
                throw new ValidationException(
                        "Test with this code already exists: " + createDTO.getCode(),
                        "DUPLICATE_TEST_CODE",
                        HttpStatus.CONFLICT
                );
            }
        }

        // 4. Create Entity
        LabTestType entity = new LabTestType();
        entity.setClinicId(clinicId);
        entity.setName(createDTO.getName());
        entity.setCode(createDTO.getCode());
        entity.setDescription(createDTO.getDescription());
        entity.setTestFee(createDTO.getTestFee());
        entity.setClinicFee(createDTO.getClinicFee());
        entity.setIsActive(true);

        // 5. Save & Return
        LabTestType saved = labRepo.save(entity);
        return mapToDTO(saved);
    }

    @Override
    public Page<LabTestTypeResponseDTO> getAllTestTypes(String search, Pageable pageable) {
        return labRepo.searchAll(search, TenantContext.getCurrentTenant(), pageable)
                .map(this::mapToDTO);
    }

    @Override
    public Page<LabTestTypeResponseDTO> getActiveTestTypes(Pageable pageable) {
        return labRepo.findAllActive(TenantContext.getCurrentTenant(), pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional
    public LabTestTypeResponseDTO updateTestType(Integer id, LabTestTypeUpdateDTO updateDTO) {
        Integer clinicId = TenantContext.getCurrentTenant();
        LabTestType existing = findById(id);

        // 1. Check duplicate NAME (if changing)
        if (updateDTO.getName() != null &&
                !existing.getName().equalsIgnoreCase(updateDTO.getName()) &&
                labRepo.existsByNameAndClinicIdAndIdNot(updateDTO.getName(), clinicId, id)) {
            throw new ValidationException("Test name already exists", "DUPLICATE_NAME", HttpStatus.CONFLICT);
        }

        // --- 2. NEW: Check duplicate CODE (if changing) ---
        if (updateDTO.getCode() != null &&
                !Objects.equals(existing.getCode(), updateDTO.getCode()) &&
                labRepo.existsByCodeAndClinicIdAndIdNot(updateDTO.getCode(), clinicId, id)) {
            throw new ValidationException(
                    "Test code already exists: " + updateDTO.getCode(),
                    "DUPLICATE_CODE",
                    HttpStatus.CONFLICT
            );
        }

        // 3. Update fields
        if (updateDTO.getName() != null) existing.setName(updateDTO.getName());
        if (updateDTO.getCode() != null) existing.setCode(updateDTO.getCode());
        if (updateDTO.getDescription() != null) existing.setDescription(updateDTO.getDescription());
        if (updateDTO.getTestFee() != null) existing.setTestFee(updateDTO.getTestFee());
        if (updateDTO.getClinicFee() != null) existing.setClinicFee(updateDTO.getClinicFee());
        if (updateDTO.getIsActive() != null) existing.setIsActive(updateDTO.getIsActive());

        LabTestType updated = labRepo.save(existing);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public LabTestTypeResponseDTO toggleStatus(Integer id) {
        LabTestType existing = findById(id);
        existing.setIsActive(!Boolean.TRUE.equals(existing.getIsActive()));
        return mapToDTO(labRepo.save(existing));
    }

    // --- Helpers ---

    private LabTestType findById(Integer id) {
        return labRepo.findById(id)
                .filter(t -> Objects.equals(t.getClinicId(), TenantContext.getCurrentTenant()))
                .orElseThrow(() -> new ValidationException("Lab Test not found", "NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    private LabTestTypeResponseDTO mapToDTO(LabTestType entity) {
        LabTestTypeResponseDTO dto = new LabTestTypeResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCode(entity.getCode());
        dto.setDescription(entity.getDescription());

        dto.setTestFee(entity.getTestFee());
        dto.setClinicFee(entity.getClinicFee());

        // Calculate Total
        BigDecimal total = (entity.getTestFee() != null ? entity.getTestFee() : BigDecimal.ZERO)
                .add(entity.getClinicFee() != null ? entity.getClinicFee() : BigDecimal.ZERO);
        dto.setTotalPrice(total);

        dto.setIsActive(entity.getIsActive());

        dto.setCreatedDate(entity.getCreatedDate());
        dto.setCreatedBy(entity.getCreatedBy());
        return dto;
    }
}