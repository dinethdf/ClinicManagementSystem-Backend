package com.dinethdf.ClinicManagementSystem.Controller.PatientPortal;


import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Appointment.AppointmentFullDetailsDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Appointment.PatientAppointmentDTO;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserPrincipal;
import com.dinethdf.ClinicManagementSystem.Service.Appointment.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/patient-portal/appointments")
@RequiredArgsConstructor
public class PatientPortalAppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping("/my-history")
    @PreAuthorize("hasAnyAuthority('PATIENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<PatientAppointmentDTO>>> getMyAppointments() {

        Long patientId = 69L;

        // 2. Call Service
        List<PatientAppointmentDTO> appointments = appointmentService.getAppointmentsByPatient(patientId);

        return ResponseEntity.ok(ApiResponse.success(
                appointments,
                "My appointment history retrieved successfully"
        ));
    }

    @GetMapping("/details/{id}")
    @PreAuthorize("hasAnyAuthority('PATIENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<AppointmentFullDetailsDTO>> getAppointmentDetails(
            @PathVariable Long id) {

        // 1. Get Patient ID from Token (Secure Way)
//        Long patientId = userPrincipal.getUserDetailsId();

        // --- FOR TESTING ONLY (Uncomment below to hardcode) ---
         Long patientId = 69L;

        // 2. Call Service
        // This ensures the patient can ONLY see their own appointment (checked in query)
        AppointmentFullDetailsDTO details = appointmentService.getAppointmentDetailsByAppoinemtnPatientId(id, patientId);

        return ResponseEntity.ok(ApiResponse.success(
                details,
                "Appointment details retrieved successfully"
        ));
    }

}