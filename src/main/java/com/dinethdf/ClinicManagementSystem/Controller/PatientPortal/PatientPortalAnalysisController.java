package com.dinethdf.ClinicManagementSystem.Controller.PatientPortal;


import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.Service.Analytics.PatientAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/patient-portal/overall-analysis")
@RequiredArgsConstructor
public class PatientPortalAnalysisController {

    private final PatientAnalysisService patientAnalysisService;

    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('PATIENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPatientProfileAnalysis() {

        Long patientId = 69L;

        return ResponseEntity.ok(ApiResponse.success(
                patientAnalysisService.getPatientAnalysis(patientId),
                "Patient profile analysis retrieved successfully"
        ));
    }
}