package com.dinethdf.ClinicManagementSystem.Controller.Appointment;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Invoice.AppointmentInvoiceViewDTO;
import com.dinethdf.ClinicManagementSystem.Service.Appointment.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/appointments/invoices")
@RequiredArgsConstructor
public class AppointmentInvoiceControllerView {

    private final AppointmentService appointmentService;


    @GetMapping("/{id}/view")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'USER')")
    public ResponseEntity<ApiResponse<List<AppointmentInvoiceViewDTO>>> getAppointmentInvoices(
            @PathVariable Long id) {

        List<AppointmentInvoiceViewDTO> invoices = appointmentService.getInvoicesByAppointmentId(id);

        return ResponseEntity.ok(
                ApiResponse.success(invoices, "Appointment invoices retrieved successfully")
        );
    }
}
