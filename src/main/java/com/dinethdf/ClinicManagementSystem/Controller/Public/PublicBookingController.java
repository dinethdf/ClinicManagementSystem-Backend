package com.dinethdf.ClinicManagementSystem.Controller.Public;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Public.PublicBookingRequestDTO;
import com.dinethdf.ClinicManagementSystem.Service.Patient.PatientServiceImpl;
import com.dinethdf.ClinicManagementSystem.Service.Public.PublicBookingService;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/public")
public class PublicBookingController {

    @Autowired
    private PatientServiceImpl patientService;

    @Autowired
    private PublicBookingService publicBookingService;

    @GetMapping("/check-patient")
    public ResponseEntity<ApiResponse<PatientResponseDTO>> checkPatient(
            @RequestParam(required = false) String nic,
            @RequestParam(required = false) String mobileNo,
            @RequestParam(required = true) String clinicId
    ) {
        try {
            TenantContext.setCurrentTenant(Integer.valueOf(clinicId));
            PatientResponseDTO foundPatient = null;
            if (nic != null && !nic.trim().isEmpty()) {
                foundPatient = patientService.findPatientDetailsByNic(nic);
            }

            else if (mobileNo != null && !mobileNo.trim().isEmpty()) {
                List<PatientResponseDTO> results = patientService.findPatientBySearching(null, null, null, mobileNo);
                if (!results.isEmpty()) {
                    foundPatient = results.get(0); // Take the first match
                }
            }

            // 4. Return Result
            if (foundPatient != null) {
                return ResponseEntity.ok(ApiResponse.success(foundPatient, "Patient Found"));
            }

            // If we reach here, no patient was found -> Return success with NULL data
            return ResponseEntity.ok(ApiResponse.success(null, "Patient not found"));

        } finally {
            // 5. Always clear tenant to prevent data leaks in thread pool
            TenantContext.clear();
        }
    }

    @PostMapping("/book-public")
    public ResponseEntity<ApiResponse<Long>> bookPublicAppointment(@RequestBody PublicBookingRequestDTO request) {

        if (request.getClinicId() == null || request.getClinicId() == 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Clinic ID is missing."));
        }
        try {
            TenantContext.setCurrentTenant(request.getClinicId());
            ApiResponse<Long> response = publicBookingService.processPublicBooking(request);
            return ResponseEntity.ok(response);
        } finally {
            TenantContext.clear();
        }
    }
}