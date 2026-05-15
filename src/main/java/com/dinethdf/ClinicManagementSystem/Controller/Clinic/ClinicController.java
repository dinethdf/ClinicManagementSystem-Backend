package com.dinethdf.ClinicManagementSystem.Controller.Clinic;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Clinic.ClinicResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Clinic.ClinicUpdateDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Clinic.PlanChangeDTO;
import com.dinethdf.ClinicManagementSystem.Model.Clinic.ClinicSettings;
import com.dinethdf.ClinicManagementSystem.Service.Clinic.ClinicServiceImpl;
import com.dinethdf.ClinicManagementSystem.util.FEValueEncryption;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
    private final FEValueEncryption feValueEncryption;

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
        
        // Encrypt the JSON string for the frontend
        String feEncryptedSecrets = feValueEncryption.encodeString(decryptedSecrets);
        
        return ResponseEntity.ok(ApiResponse.success(feEncryptedSecrets, "Secrets fetched successfully"));
    }

    @PutMapping("/secrets")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<String>> updateSecrets(@RequestBody Map<String, String> request) {

        // The FE sends: { "data": "ENCRYPTED_BASE64_STRING" }
        String encryptedPayload = request.get("data");
        if (encryptedPayload == null || encryptedPayload.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Missing encrypted data payload"));
        }

        try {
            // 1. Decrypt the Base64 string from the frontend back to plain JSON
            String decryptedJson = feValueEncryption.decodeString(encryptedPayload);
            if (decryptedJson == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Failed to decrypt data payload"));
            }

            // 2. Parse the JSON back into a Map
            Map<String, String> secretsPayload = objectMapper.readValue(decryptedJson, new TypeReference<Map<String, String>>() {});
            
            // 3. Save it securely to the database (which encrypts it again with DB keys)
            clinicService.updateEncryptedSecrets(secretsPayload);

            return ResponseEntity.ok(ApiResponse.success("Secrets securely merged and saved", "SUCCESS"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid secure payload structure"));
        }
    }
}