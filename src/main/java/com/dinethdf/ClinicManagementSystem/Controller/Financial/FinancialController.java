package com.dinethdf.ClinicManagementSystem.Controller.Financial;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Financial.ScheduleFinancialDTO;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserPrincipal;
import com.dinethdf.ClinicManagementSystem.Service.DoctorSchedule.DoctorScheduleService;
import com.dinethdf.ClinicManagementSystem.Service.Financial.FinancialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/financials")
@RequiredArgsConstructor
public class FinancialController {

    private final FinancialService financialService;
    private final DoctorScheduleService doctorScheduleService;

    @GetMapping("/schedule/{scheduleId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'DOCTOR')")
    public ResponseEntity<List<ScheduleFinancialDTO>> getScheduleFinancials(
            @PathVariable Integer scheduleId
    ) {
        List<ScheduleFinancialDTO> financials = financialService.getFinancialsForSchedule(scheduleId);
        return ResponseEntity.ok(financials);
    }

    @GetMapping("/schedule/{scheduleId}/by-doctor")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DOCTOR')")
    public ResponseEntity<List<ScheduleFinancialDTO>> getScheduleFinancialsByDoctor(
            @PathVariable Integer scheduleId,
            Authentication authentication
    ) {
        if (authentication.getPrincipal() instanceof String) {
            throw new AccessDeniedException("User is not authenticated correctly.");
        }
        UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();

        Long doctorId = userDetails.getUserDetailsId();

        boolean isValid = doctorScheduleService.validateScheduleOwnership(doctorId, Long.valueOf(scheduleId));
        if (isValid) {
            List<ScheduleFinancialDTO> financials = financialService.getFinancialsForSchedule(scheduleId);
            return ResponseEntity.ok(financials);
        } else {
            throw new AccessDeniedException("Doctor is not Related this schedule.");
        }

    }
}