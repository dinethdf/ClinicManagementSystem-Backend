package com.dinethdf.ClinicManagementSystem.Controller.Doctor;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Doctor.DoctorResponseDto;
import com.dinethdf.ClinicManagementSystem.Model.Doctor.Doctor;
import com.dinethdf.ClinicManagementSystem.Service.Doctor.DoctorServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("api/doctor")
public class DoctorController {

    @Autowired
    DoctorServiceImpl doctorServiceImpl;

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Doctor>>> getAllDoctors() {
        // This call is the same, but now the service does the conversion.
        List<Doctor> doctors = doctorServiceImpl.getAllDoctors();

        ApiResponse<List<Doctor>> response = ApiResponse.success(
                doctors,
                "Successfully retrieved all doctors"

        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    @GetMapping("get-details/staff/{id}")
    public ResponseEntity<ApiResponse<DoctorResponseDto>> getPatients(@PathVariable String id){
        ApiResponse<DoctorResponseDto> response = doctorServiceImpl.getDoctorFullDetailsById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @PreAuthorize("hasAuthority('DOCTOR')") // Only a DOCTOR can access this
    @GetMapping("/get-my-details")
    public ResponseEntity<ApiResponse<DoctorResponseDto>> getMyDetails(Principal principal) {

        // 1. Get the email (which is the username) from the token
        String email = principal.getName();

        // 2. Call your new service method with the email
        ApiResponse<DoctorResponseDto> response = doctorServiceImpl.getDoctorDetailsByEmail(email);

        // 3. Return the response
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'USER', 'DOCTOR')")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<DoctorResponseDto>>> searchDoctors(
            // We use 'defaultValue = ""' so it returns an empty list
            // instead of erroring if the 'q' param is missing.
            @RequestParam(name = "q", defaultValue = "") String query) {

        List<DoctorResponseDto> doctors = doctorServiceImpl.searchDoctors(query);

        ApiResponse<List<DoctorResponseDto>> response = ApiResponse.success(
                doctors,
                "messageSourceUtil.getMessage(\"doctor.search.success\")"
        );

        return ResponseEntity.ok(response);
    }

//    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    @PostMapping("register")
    public ResponseEntity<ApiResponse<Doctor>> savePatientDetailsByStaff(@RequestBody Doctor doctor){
        ApiResponse<Doctor> response = doctorServiceImpl.registerDoctor(doctor);
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }





}
