package com.dinethdf.ClinicManagementSystem.Controller.Lab;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Lab.LabTestTypeCreateDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Lab.LabTestTypeResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Lab.LabTestTypeUpdateDTO;
import com.dinethdf.ClinicManagementSystem.Service.Lab.LabTestTypeService;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/lab/types")
@RequiredArgsConstructor
public class LabTestTypeController {

    private final LabTestTypeService labService;
    private final MessageSourceUtil messageSourceUtil;

    // 1. Create Test (Admin Only)
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<LabTestTypeResponseDTO>> create(
            @RequestBody LabTestTypeCreateDTO dto) {

        LabTestTypeResponseDTO response = labService.createTestType(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        response,
                        messageSourceUtil.getMessage("lab.type.create.success")
                ));
    }

    // 2. List All (Admin View - Includes inactive)
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Page<LabTestTypeResponseDTO>>> getAll(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {

        Page<LabTestTypeResponseDTO> list = labService.getAllTestTypes(search, pageable);

        return ResponseEntity.ok(ApiResponse.success(
                list,
                messageSourceUtil.getMessage("lab.type.list.success")
        ));
    }



    // 4. Update details
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<LabTestTypeResponseDTO>> update(
            @PathVariable Integer id,
            @RequestBody LabTestTypeUpdateDTO dto) {

        LabTestTypeResponseDTO response = labService.updateTestType(id, dto);

        return ResponseEntity.ok(ApiResponse.success(
                response,
                messageSourceUtil.getMessage("lab.type.update.success")
        ));
    }

    // 5. Toggle Active Status
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<LabTestTypeResponseDTO>> toggleStatus(@PathVariable Integer id) {

        LabTestTypeResponseDTO response = labService.toggleStatus(id);

        return ResponseEntity.ok(ApiResponse.success(
                response,
                messageSourceUtil.getMessage("lab.type.toggle.success")
        ));
    }
}