package com.dinethdf.ClinicManagementSystem.Controller.Clinic;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Clinic.ClinicResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Clinic.ClinicUpdateDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Clinic.PlanChangeDTO;
import com.dinethdf.ClinicManagementSystem.Model.Clinic.ClinicSettings;
import com.dinethdf.ClinicManagementSystem.Service.Clinic.ClinicServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clinic/info")
@RequiredArgsConstructor
public class ClinicController {

    private final ClinicServiceImpl clinicService;

    // 1. GET Clinic Details
    @GetMapping("/details")
    public ResponseEntity<ApiResponse<ClinicResponseDTO>> getClinicDetails() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        clinicService.getCurrentClinicDetails(),
                        "Clinic details retrieved successfully"
                )
        );
    }

    // 2. UPDATE Basic Info (Name, Address, Mobile, etc.)
    @PutMapping("/update")
    @PreAuthorize("hasAuthority('ADMIN')") // Only Clinic Admin
    public ResponseEntity<ApiResponse<ClinicResponseDTO>> updateClinicInfo(@RequestBody ClinicUpdateDTO request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        clinicService.updateCurrentClinicDetails(request),
                        "Clinic information updated successfully"
                )
        );
    }

    // 3. UPDATE Settings (Currency, Timezone, Language)
    @PutMapping("/settings")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<ClinicResponseDTO>> updateClinicSettings(@RequestBody ClinicSettings request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        clinicService.updateCurrentClinicSettings(request),
                        "Clinic settings updated successfully"
                )
        );
    }

    // 4. UPDATE Subscription Plan
    @PutMapping("/subscription")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<ClinicResponseDTO>> updateSubscription(@RequestBody PlanChangeDTO request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        clinicService.updateSubscriptionPlan(request),
                        "Subscription plan updated successfully"
                )
        );
    }
}