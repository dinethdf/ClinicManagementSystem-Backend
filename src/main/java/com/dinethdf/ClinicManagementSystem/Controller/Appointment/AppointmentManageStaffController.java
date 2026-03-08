package com.dinethdf.ClinicManagementSystem.Controller.Appointment;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Appointment.StaffAppointmentManageDTO;
import com.dinethdf.ClinicManagementSystem.Service.Appointment.AppointmentManageStaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointments/manage")
@RequiredArgsConstructor
public class AppointmentManageStaffController {

    private final AppointmentManageStaffService appointmentManageStaffService;

    // 1. Revert to Waiting Queue
    @PostMapping("/booked")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<String>> markAsBooked(@RequestBody StaffAppointmentManageDTO request) {
        return ResponseEntity.ok(appointmentManageStaffService.markAsBooked(request));
    }

    // 2. Call Patient to Room
    @PostMapping("/ongoing")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<String>> markAsOngoing(@RequestBody StaffAppointmentManageDTO request) {
        return ResponseEntity.ok(appointmentManageStaffService.markAsOngoing(request));
    }

    // 3. Finish Appointment
    @PostMapping("/completed")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<String>> markAsCompleted(@RequestBody StaffAppointmentManageDTO request) {
        return ResponseEntity.ok(appointmentManageStaffService.markAsCompleted(request));
    }
}