package com.dinethdf.ClinicManagementSystem.Controller.Doctor;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.Model.Doctor.Doctor;
import com.dinethdf.ClinicManagementSystem.Service.Doctor.DoctorServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/doctor")
public class DoctorController {

    @Autowired
    DoctorServiceImpl doctorServiceImpl;

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    @GetMapping("get-details/staff/{id}")
    public ResponseEntity<ApiResponse<Doctor>> getPatients(@PathVariable String id){
        ApiResponse<Doctor> response = doctorServiceImpl.getDoctorFullDetailsById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

//    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
//    @GetMapping("/search-doctor")
//    public ResponseEntity<List<GuardianResponseDTO>> searchGuardian(
//            @RequestParam(required = false) String nic,
//            @RequestParam(required = false) String fullName) {
//
//        List<GuardianResponseDTO> results = patientServiceImpl.findGuardianByNicAndFullName(nic, fullName);
//        return ResponseEntity.ok(results);
//    }

//    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    @PostMapping("register")
    public ResponseEntity<ApiResponse<Doctor>> savePatientDetailsByStaff(@RequestBody Doctor doctor){
        ApiResponse<Doctor> response = doctorServiceImpl.registerDoctor(doctor);
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }





}
