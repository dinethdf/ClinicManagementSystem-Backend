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
    public ResponseEntity<ApiResponse<List<PatientResponseDTO>>> checkPatient(
            @RequestParam(required = true) String query, // The single value (NIC or Mobile)
            @RequestParam(required = true) String clinicId
    ) {
        try {
            TenantContext.setCurrentTenant(Integer.valueOf(clinicId));
        
            List<PatientResponseDTO> patients = patientService.checkPatientExistence(query);

            return ResponseEntity.ok(ApiResponse.success(patients, "Search Complete"));

        } finally {
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