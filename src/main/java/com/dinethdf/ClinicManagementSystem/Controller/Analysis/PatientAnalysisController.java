package com.dinethdf.ClinicManagementSystem.Controller.Analysis;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.Service.Analytics.PatientAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/patient-analysis")
@RequiredArgsConstructor
public class PatientAnalysisController {

    private final PatientAnalysisService patientAnalysisService;

    @GetMapping("/{patientId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPatientProfileAnalysis(
            @PathVariable Long patientId) {

        return ResponseEntity.ok(ApiResponse.success(
                patientAnalysisService.getPatientAnalysis(patientId),
                "Patient profile analysis retrieved successfully"
        ));
    }
}