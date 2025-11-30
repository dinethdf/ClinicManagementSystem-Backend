package com.dinethdf.ClinicManagementSystem.Controller.Prescription;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Prescription.PrescriptionRequestDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Prescription.PrescriptionResponseDTO;
import com.dinethdf.ClinicManagementSystem.Service.Prescription.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'DOCTOR')")
    public ResponseEntity<ApiResponse<PrescriptionResponseDTO>> createPrescription(
            @RequestBody PrescriptionRequestDTO request,
            Principal principal) {

        String createdBy = (principal != null) ? principal.getName() : "SYSTEM";
        ApiResponse<PrescriptionResponseDTO> response = prescriptionService.createPrescription(request, createdBy);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'DOCTOR', 'USER')")
    public ResponseEntity<ApiResponse<PrescriptionResponseDTO>> getByAppointment(@PathVariable Long appointmentId) {
        ApiResponse<PrescriptionResponseDTO> response = prescriptionService.getPrescriptionByAppointment(appointmentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'DOCTOR', 'USER')")
    public ResponseEntity<ApiResponse<PrescriptionResponseDTO>> getById(@PathVariable Long id) {
        ApiResponse<PrescriptionResponseDTO> response = prescriptionService.getPrescriptionById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'DOCTOR')")
    public ResponseEntity<ApiResponse<PrescriptionResponseDTO>> updatePrescription(
            @PathVariable Long id,
            @RequestBody PrescriptionRequestDTO request,
            Principal principal) {

        String updatedBy = (principal != null) ? principal.getName() : "SYSTEM";
        ApiResponse<PrescriptionResponseDTO> response = prescriptionService.updatePrescription(id, request, updatedBy);
        return ResponseEntity.ok(response);
    }
}