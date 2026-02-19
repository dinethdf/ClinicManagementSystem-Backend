package com.dinethdf.ClinicManagementSystem.Controller.Clinic;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;

import com.dinethdf.ClinicManagementSystem.DTO.Clinic.ClinicRegistrationRequest;
import com.dinethdf.ClinicManagementSystem.DTO.Clinic.ClinicResponseDTO;

import com.dinethdf.ClinicManagementSystem.Service.Clinic.ClinicRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/registration")
@RequiredArgsConstructor
public class ClinicRegistrationController {

    private final ClinicRegistrationService registrationService;

    @PostMapping("/register-clinic")
    public ResponseEntity<ApiResponse<ClinicResponseDTO>> registerClinic(@RequestBody ClinicRegistrationRequest request) {

        ClinicResponseDTO result = registrationService.registerNewClinic(request);

        return ResponseEntity.ok(
                ApiResponse.success(result, "Clinic registered successfully. Please login.")
        );
    }
}