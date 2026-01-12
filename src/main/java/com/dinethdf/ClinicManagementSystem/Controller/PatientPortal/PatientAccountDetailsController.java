package com.dinethdf.ClinicManagementSystem.Controller.PatientPortal;


import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.Model.Patient.Patient;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.PatientPrincipal;
import com.dinethdf.ClinicManagementSystem.Service.Analytics.PatientAnalysisService;
import com.dinethdf.ClinicManagementSystem.Service.Patient.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/patient-portal/account-settings")
@RequiredArgsConstructor
public class PatientAccountDetailsController {

    private final PatientService patientService;


    @PreAuthorize("hasAnyAuthority('PATIENT')")
    @PutMapping("details/self/{id}")
    public ResponseEntity<ApiResponse<Patient>> updatePatientDetailsBySelf(
            @PathVariable Integer id,
            @RequestBody Patient patient,
            @AuthenticationPrincipal PatientPrincipal patientPrincipal) {

        // 1. Get the ID from the authenticated token
        Long tokenPatientId = patientPrincipal.getPatientDetailsId();

        // 2. Security Check: Compare the requested ID with the Token ID
        // We convert 'id' (Integer) to Long for accurate comparison
        if (!tokenPatientId.equals(Long.valueOf(id))) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: You can only update your own profile."));
        }

        // 3. Proceed if IDs match
        ApiResponse<Patient> response = patientService.updatePatientBySelf(tokenPatientId, patient);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
//

    @GetMapping("fill-details")
    @PreAuthorize("hasAnyAuthority('PATIENT')")
    public ResponseEntity<ApiResponse<Patient>> getPatientDetailsByIdStaff(@AuthenticationPrincipal PatientPrincipal patientPrincipal) {
        Long patientId = patientPrincipal.getPatientDetailsId();

        return ResponseEntity.ok(patientService.getPatientDetailsByIdAll(String.valueOf(patientId)));
    }
}