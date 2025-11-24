package com.dinethdf.ClinicManagementSystem.Controller.Doctor;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Doctor.DoctorResponseDto;
import com.dinethdf.ClinicManagementSystem.Model.Doctor.Doctor;
import com.dinethdf.ClinicManagementSystem.Service.Doctor.DoctorServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    @GetMapping("/doctorlist")
    public ResponseEntity<ApiResponse<Page<DoctorResponseDto>>> getAllDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        // Call the new service method
        Page<DoctorResponseDto> doctorsPage = doctorServiceImpl.getAllDoctorsPaginated(page, size);

        ApiResponse<Page<DoctorResponseDto>> response = ApiResponse.success(
                doctorsPage,
                "Successfully retrieved doctors list"
        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Doctor>>> getAllDoctors() {
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


    @PreAuthorize("hasAuthority('DOCTOR')")
    @GetMapping("/get-my-details")
    public ResponseEntity<ApiResponse<DoctorResponseDto>> getMyDetails(Principal principal) {

        String email = principal.getName();

        ApiResponse<DoctorResponseDto> response = doctorServiceImpl.getDoctorDetailsByEmail(email);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'USER', 'DOCTOR')")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<DoctorResponseDto>>> searchDoctors(
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
    public ResponseEntity<ApiResponse<Doctor>> saveDoctorDetailsByStaff(@RequestBody Doctor doctor){
        ApiResponse<Doctor> response = doctorServiceImpl.registerDoctor(doctor);
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }





}
