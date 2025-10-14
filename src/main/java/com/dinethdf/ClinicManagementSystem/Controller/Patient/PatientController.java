package com.dinethdf.ClinicManagementSystem.Controller.Patient;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientResponseDto;
import com.dinethdf.ClinicManagementSystem.Model.Patient.PatientDetails;
import com.dinethdf.ClinicManagementSystem.Service.Patient.PatientServiceImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/patient")
public class PatientController {

    @Autowired
    PatientServiceImpl patientServiceImpl;


    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    @PostMapping("details/staff")
    public ResponseEntity<ApiResponse<PatientDetails>> savePatientDetailsByStaff(@RequestBody PatientDetails patientDetails){

        PatientDetails patientDetailsCreated =  patientServiceImpl.registerPatientByStaff(patientDetails);
        ApiResponse<PatientDetails> response = new ApiResponse<>(
                true,
                "Patient registered successfully by self",
                patientDetailsCreated
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }


//    @PreAuthorize("hasAnyAuthority('PATIENT')")
//    @PostMapping("details/self")
//    public ResponseEntity<ApiResponse<PatientDetails>> savePatientDetailsBySelf(@RequestBody PatientDetails patientDetails){
//
//        PatientDetails patientDetailsCreated =  patientServiceImpl.addOrUpdatePatientDetailsByStaff(patientDetails);
//        ApiResponse<PatientDetails> response = new ApiResponse<>(
//                true,
//                "Patient registered successfully by self",
//                patientDetailsCreated
//        );
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//
//    }


    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    @GetMapping("b/b")
    public String getPatients4(){
        System.out.println("Hello2");
        return "Patient2";
    }


}
