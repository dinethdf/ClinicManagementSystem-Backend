package com.dinethdf.ClinicManagementSystem.Controller.Appointment;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Appointment.AppointmentCreateDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Appointment.AppointmentResponseDTO;
import com.dinethdf.ClinicManagementSystem.Service.Appointment.AppointmentService;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("api/appointments") // New path for appointments
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final MessageSourceUtil messageSourceUtil;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'USER')")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> createAppointment(
             @RequestBody AppointmentCreateDTO createDTO, Principal principal) {

        String createBy = principal.getName();

        // 1. Call the service
        AppointmentResponseDTO newAppointment = appointmentService.createAppointment(createDTO, createBy);

        // 2. Create the standard success response
        ApiResponse<AppointmentResponseDTO> response = ApiResponse.success(
                newAppointment,
                messageSourceUtil.getMessage("appointment.create.success")
        );

        // 3. Return 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}