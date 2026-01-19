package com.dinethdf.ClinicManagementSystem.Controller.Patient;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientHealthDTO;
import com.dinethdf.ClinicManagementSystem.Service.Patient.PatientHealthServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient/health")

public class PatientHealthController {

    private final PatientHealthServiceImpl healthService;

    public PatientHealthController(PatientHealthServiceImpl healthService) {
        this.healthService = healthService;
    }

    // ================= ALLERGIES =================

    @PreAuthorize("hasAnyAuthority('ADMIN', 'DOCTOR', 'STAFF', 'PATIENT')")
    @GetMapping("/{patientId}/allergies")
    public ResponseEntity<ApiResponse<List<PatientHealthDTO.AllergyDTO>>> getAllergies(@PathVariable Integer patientId) {
        return ResponseEntity.ok(ApiResponse.success(healthService.getAllergies(patientId), "Fetched allergies"));
    }

    @PostMapping("/{patientId}/allergies")
    public ResponseEntity<ApiResponse<PatientHealthDTO.AllergyDTO>> addAllergy(
            @PathVariable Integer patientId,
            @RequestBody PatientHealthDTO.AllergyDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(healthService.addAllergy(patientId, dto), "Allergy added"));
    }

    @DeleteMapping("/allergies/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAllergy(@PathVariable Integer id) {
        healthService.deleteAllergy(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Allergy deleted"));
    }

    // ================= DISEASES =================

    @GetMapping("/{patientId}/diseases")
    public ResponseEntity<ApiResponse<List<PatientHealthDTO.ChronicDiseaseDTO>>> getDiseases(@PathVariable Integer patientId) {
        return ResponseEntity.ok(ApiResponse.success(healthService.getDiseases(patientId), "Fetched diseases"));
    }

    @PostMapping("/{patientId}/diseases")
    public ResponseEntity<ApiResponse<PatientHealthDTO.ChronicDiseaseDTO>> addDisease(
            @PathVariable Integer patientId,
            @RequestBody PatientHealthDTO.ChronicDiseaseDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(healthService.addDisease(patientId, dto), "Disease added"));
    }

    @DeleteMapping("/diseases/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDisease(@PathVariable Integer id) {
        healthService.deleteDisease(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Disease deleted"));
    }
}