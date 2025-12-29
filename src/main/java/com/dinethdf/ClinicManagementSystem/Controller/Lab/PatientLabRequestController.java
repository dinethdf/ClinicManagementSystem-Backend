package com.dinethdf.ClinicManagementSystem.Controller.Lab;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Lab.LabRequestCreateDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Lab.LabRequestResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Lab.LabRequestUpdateDTO;
import com.dinethdf.ClinicManagementSystem.Model.Lab.PatientLabRequest.LabStatus;
import com.dinethdf.ClinicManagementSystem.Service.Lab.PatientLabRequestService;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/lab/requests")
@RequiredArgsConstructor
public class PatientLabRequestController {

    private final PatientLabRequestService requestService;
    private final MessageSourceUtil messageSourceUtil;

    /**
     * Create a new Lab Request.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'DOCTOR')")
    public ResponseEntity<ApiResponse<LabRequestResponseDTO>> createRequest(
            @RequestBody LabRequestCreateDTO createDTO) {

        LabRequestResponseDTO response = requestService.createLabRequest(createDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        response,
                        messageSourceUtil.getMessage("lab.request.create.success")
                ));
    }

    /**
     * List All Requests (Admin/Technician View).
     * Filterable by Search (Patient Name) and Status.
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Page<LabRequestResponseDTO>>> getAllRequests(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LabStatus status,
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        Page<LabRequestResponseDTO> page = requestService.getAllLabRequests(search, status, pageable);

        return ResponseEntity.ok(ApiResponse.success(
                page,
                messageSourceUtil.getMessage("lab.request.list.success")
        ));
    }

    /**
     * Get details of a single request.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'DOCTOR', 'USER')")
    public ResponseEntity<ApiResponse<LabRequestResponseDTO>> getRequestById(@PathVariable Integer id) {

        LabRequestResponseDTO response = requestService.getLabRequestById(id);

        return ResponseEntity.ok(ApiResponse.success(
                response,
                messageSourceUtil.getMessage("lab.request.get.success")
        ));
    }

    /**
     * Get History for a specific Patient.
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'DOCTOR', 'USER')")
    public ResponseEntity<ApiResponse<Page<LabRequestResponseDTO>>> getPatientHistory(
            @PathVariable Integer patientId,
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        Page<LabRequestResponseDTO> page = requestService.getLabRequestsByPatient(patientId, pageable);

        return ResponseEntity.ok(ApiResponse.success(
                page,
                messageSourceUtil.getMessage("lab.request.history.success")
        ));
    }

    /**
     * Update Request (Add Results / Complete / Cancel).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<LabRequestResponseDTO>> updateRequest(
            @PathVariable Integer id,
            @RequestBody LabRequestUpdateDTO updateDTO) {

        LabRequestResponseDTO response = requestService.updateLabRequest(id, updateDTO);

        return ResponseEntity.ok(ApiResponse.success(
                response,
                messageSourceUtil.getMessage("lab.request.update.success")
        ));
    }
}