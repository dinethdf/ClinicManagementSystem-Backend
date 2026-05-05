package com.dinethdf.ClinicManagementSystem.Controller.UsersAuth;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;

import com.dinethdf.ClinicManagementSystem.DTO.Auth.SystemAccountStatusDTO;
import com.dinethdf.ClinicManagementSystem.Service.UsersAuth.StaffSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
// 🚨 Ensure this mapping is exactly this:
@RequestMapping("/api/staff/{staffId}/security")
@PreAuthorize("hasAuthority('ADMIN')")
public class StaffAdminSecurityController {

    @Autowired private StaffSecurityService securityService;

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<SystemAccountStatusDTO>> getStatus(@PathVariable Long staffId) {
        return ResponseEntity.ok(ApiResponse.success(securityService.getAccountStatus(staffId), "Fetched"));
    }

    @PostMapping("/manual-reset")
    public ResponseEntity<ApiResponse<Boolean>> manualReset(@PathVariable Long staffId, @RequestBody Map<String, String> payload) {
        securityService.manualPasswordReset(staffId, payload.get("password"));
        return ResponseEntity.ok(ApiResponse.success(true, "Password manually reset"));
    }

    @PostMapping("/toggle-suspension")
    public ResponseEntity<ApiResponse<Boolean>> toggleSuspension(@PathVariable Long staffId, @RequestBody Map<String, Boolean> payload) {
        securityService.toggleSuspension(staffId, payload.get("isSuspended"));
        return ResponseEntity.ok(ApiResponse.success(true, "Suspension toggled"));
    }

    @PostMapping("/disable-2fa")
    public ResponseEntity<ApiResponse<Boolean>> disableTwoFactor(@PathVariable Long staffId) {
        securityService.disableTwoFactor(staffId);
        return ResponseEntity.ok(ApiResponse.success(true, "2FA Disabled"));
    }

    @PostMapping("/send-reset-link")
    public ResponseEntity<ApiResponse<Boolean>> sendResetLink(@PathVariable Long staffId) {
        securityService.sendResetLink(staffId);
        return ResponseEntity.ok(ApiResponse.success(true, "Reset link sent"));
    }
}