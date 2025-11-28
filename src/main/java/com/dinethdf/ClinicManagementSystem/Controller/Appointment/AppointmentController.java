package com.dinethdf.ClinicManagementSystem.Controller.Appointment;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Appointment.*;
import com.dinethdf.ClinicManagementSystem.Service.Appointment.AppointmentService;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final MessageSourceUtil messageSourceUtil;


    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'USER')")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> createAppointment(
             @RequestBody AppointmentCreateDTO createDTO, Principal principal) {

        String createBy = principal.getName();
        AppointmentResponseDTO newAppointment = appointmentService.createAppointment(createDTO, createBy);
        ApiResponse<AppointmentResponseDTO> response = ApiResponse.success(
                newAppointment,
                messageSourceUtil.getMessage("appointment.create.success")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'USER')")
    public ResponseEntity<ApiResponse<List<PatientAppointmentDTO>>> getPatientAppointments(
            @PathVariable Long patientId) {

        List<PatientAppointmentDTO> list = appointmentService.getAppointmentsByPatient(patientId);

        ApiResponse<List<PatientAppointmentDTO>> response = ApiResponse.success(
                list,
                messageSourceUtil.getMessage("appointment.list.success")
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'USER')") // Adjust roles as needed
    public ResponseEntity<ApiResponse<Page<AppointmentAdminListDTO>>> getAllAppointments(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long scheduleId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<AppointmentAdminListDTO> result = appointmentService.getFilteredAppointments(
                patientId, scheduleId, startDate, endDate, pageable
        );

        return ResponseEntity.ok(ApiResponse.success(result, messageSourceUtil.getMessage("appointment.list.success")));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'USER')")
    public ResponseEntity<ApiResponse<AppointmentFullDetailsDTO>> getAppointmentDetails(@PathVariable Long id) {

        AppointmentFullDetailsDTO detail = appointmentService.getAppointmentDetails(id);

        return ResponseEntity.ok(ApiResponse.success(detail, "Appointment details retrieved successfully"));
    }

    @GetMapping("/schedule/{scheduleId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'USER')")
    public ResponseEntity<ApiResponse<Page<AppointmentByScheduleListDTO>>> getAppointmentsByScheduleId(
            @PathVariable Long scheduleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AppointmentByScheduleListDTO> result = appointmentService.getAppointmentsBySchedule(scheduleId, pageable);
        ApiResponse<Page<AppointmentByScheduleListDTO>> response = ApiResponse.success(
                result, messageSourceUtil.getMessage("appointment.list.success")
        );
        return ResponseEntity.ok(response);
    }

}

