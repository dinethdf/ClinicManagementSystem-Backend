package com.dinethdf.ClinicManagementSystem.Controller.Doctor;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.Model.Doctor.DoctorDetails;
import com.dinethdf.ClinicManagementSystem.Service.Doctor.DoctorServiceImpl;
import com.dinethdf.ClinicManagementSystem.Service.Patient.PatientServiceImpl;
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
    public ResponseEntity<ApiResponse<DoctorDetails>> getPatients(@PathVariable String id){
        ApiResponse<DoctorDetails> response = doctorServiceImpl.getDoctorFullDetailsById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

//    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    @PostMapping("register")
    public ResponseEntity<ApiResponse<DoctorDetails>> savePatientDetailsByStaff(@RequestBody DoctorDetails doctorDetails){
        ApiResponse<DoctorDetails> response = doctorServiceImpl.registerDoctor(doctorDetails);

        return ResponseEntity.status(HttpStatus.OK).body(response);

    }





}
