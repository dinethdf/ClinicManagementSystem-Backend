package com.dinethdf.ClinicManagementSystem.Controller.Financial;

import com.dinethdf.ClinicManagementSystem.DTO.Financial.ScheduleFinancialDTO;
import com.dinethdf.ClinicManagementSystem.Service.Financial.FinancialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/financials")
@RequiredArgsConstructor
public class FinancialController {

    private final FinancialService financialService;

    @GetMapping("/schedule/{scheduleId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'USER')")
    public ResponseEntity<List<ScheduleFinancialDTO>> getScheduleFinancials(
            @PathVariable Integer scheduleId
    ) {
        List<ScheduleFinancialDTO> financials = financialService.getFinancialsForSchedule(scheduleId);
        return ResponseEntity.ok(financials);
    }
}