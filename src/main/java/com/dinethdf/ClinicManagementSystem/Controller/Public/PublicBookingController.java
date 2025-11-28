package com.dinethdf.ClinicManagementSystem.Controller.Public;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Public.PublicBookingRequestDTO;
import com.dinethdf.ClinicManagementSystem.Service.Patient.PatientServiceImpl;
import com.dinethdf.ClinicManagementSystem.Service.Public.PublicBookingService;
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

    // 1. Search Patient (Step 2 of Wizard)
    @GetMapping("/check-patient")
    public ResponseEntity<ApiResponse<PatientResponseDTO>> checkPatient(
            @RequestParam(required = false) String nic,
            @RequestParam(required = false) String mobileNo
    ) {
        try {
            // We reuse your existing search logic
            List<PatientResponseDTO> results = patientService.findPatientBySearching(null, nic, null, mobileNo);

            if (!results.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success(results.get(0), "Patient Found"));
            }
        } catch (Exception e) {
            // If validation fails (not found), we return NULL data so frontend knows to show Register Form
        }

        return ResponseEntity.ok(ApiResponse.success(null, "Patient not found"));
    }

    @PostMapping("/book-public")
    public ResponseEntity<ApiResponse<Long>> bookPublicAppointment(@RequestBody PublicBookingRequestDTO request) {
        ApiResponse<Long> response = publicBookingService.processPublicBooking(request);
        return ResponseEntity.ok(response);
    }
}