package com.dinethdf.ClinicManagementSystem.Controller.Clinic;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Clinic.ClinicResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Clinic.ClinicUpdateDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Clinic.PlanChangeDTO;
import com.dinethdf.ClinicManagementSystem.Model.Clinic.ClinicSettings;
import com.dinethdf.ClinicManagementSystem.Service.Clinic.ClinicServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/clinic/info")
@RequiredArgsConstructor
public class ClinicController {

    private final ClinicServiceImpl clinicService;

    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/details")
    public ResponseEntity<ApiResponse<ClinicResponseDTO>> getClinicDetails() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        clinicService.getCurrentClinicDetails(),
                        "Clinic details retrieved successfully"
                )
        );
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<ClinicResponseDTO>> updateClinicInfo(@RequestBody ClinicUpdateDTO request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        clinicService.updateCurrentClinicDetails(request),
                        "Clinic information updated successfully"
                )
        );
    }

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

    @GetMapping("/secrets")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<String>> getSecrets() {
        String decryptedSecrets = clinicService.getCurrentEncryptedSecrets();
        return ResponseEntity.ok(ApiResponse.success(decryptedSecrets, "Secrets fetched successfully"));
    }

    @PutMapping("/secrets")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<String>> updateSecrets(@RequestBody Map<String, String> secretsPayload) {

        clinicService.updateEncryptedSecrets(secretsPayload);

        return ResponseEntity.ok(ApiResponse.success("Secrets securely merged and saved", "SUCCESS"));
    }
}