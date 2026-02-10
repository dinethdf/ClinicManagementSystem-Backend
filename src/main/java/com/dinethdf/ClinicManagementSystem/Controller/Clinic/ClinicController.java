//package com.dinethdf.ClinicManagementSystem.Controller.Clinic;
//
//import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
//import com.dinethdf.ClinicManagementSystem.DTO.Clinic.ClinicResponseDTO;
//import com.dinethdf.ClinicManagementSystem.DTO.Clinic.ClinicUpdateDTO;
//import com.dinethdf.ClinicManagementSystem.Service.Clinic.ClinicServiceImpl;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/clinic/info")
//@RequiredArgsConstructor
//public class ClinicController {
//
//    private final ClinicServiceImpl clinicService;
//
//    @GetMapping("/details")
//    public ResponseEntity<ApiResponse<ClinicResponseDTO>> getClinicDetails() {
//        return ResponseEntity.ok(clinicService.getCurrentClinicDetails());
//    }
//
//    @PutMapping("/update")
//    @PreAuthorize("hasAuthority('ADMIN')") // Only Admin can update clinic details
//    public ResponseEntity<ApiResponse<ClinicResponseDTO>> updateClinic(@RequestBody ClinicUpdateDTO request) {
//        return ResponseEntity.ok(clinicService.updateClinicDetails(request));
//    }
//}