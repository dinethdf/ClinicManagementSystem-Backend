package com.dinethdf.ClinicManagementSystem.Controller.Clinic;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.Model.Clinic.Clinic;
import com.dinethdf.ClinicManagementSystem.Service.Clinic.ClinicServiceImpl;
import com.dinethdf.ClinicManagementSystem.DTO.LoginRequest;
import com.dinethdf.ClinicManagementSystem.Model.AuthModels.JwtAuthenticationResponse;
import com.dinethdf.ClinicManagementSystem.Service.UsersAuth.MasterAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Super Admin Controller for managing clinics at a system level.
 * Used for "Private Clinic Registration" and manual multi-tenant configuration.
 */
@RestController
@RequestMapping("/api/admin/clinics")
@RequiredArgsConstructor
public class SuperAdminClinicController {

    private final ClinicServiceImpl clinicService;
    private final MasterAdminService masterAdminService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> systemLogin(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(masterAdminService.systemLogin(request), "Master login successful"));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('SUPER-ADMIN')")
    public ResponseEntity<ApiResponse<Clinic>> createPrivateClinic(@RequestBody Clinic clinic) {
        Clinic created = clinicService.createClinic(clinic);
        return ResponseEntity.ok(ApiResponse.success(created, "Private clinic created successfully"));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('SUPER-ADMIN')")
    public ResponseEntity<ApiResponse<List<Clinic>>> getAllClinics() {
        return ResponseEntity.ok(ApiResponse.success(clinicService.getAllClinics(), "All clinics fetched"));
    }

    @PutMapping("/{code}")
    @PreAuthorize("hasAuthority('SUPER-ADMIN')")
    public ResponseEntity<ApiResponse<Clinic>> updateClinic(
            @PathVariable String code, 
            @RequestBody Clinic details) {
        return ResponseEntity.ok(ApiResponse.success(clinicService.updateClinic(code, details), "Clinic updated"));
    }
}
