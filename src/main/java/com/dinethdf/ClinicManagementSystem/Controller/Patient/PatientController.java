package com.dinethdf.ClinicManagementSystem.Controller.Patient;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.Model.Patient.PatientDetails;
import com.dinethdf.ClinicManagementSystem.Service.Patient.PatientServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/patient")
public class PatientController {

    @Autowired
    PatientServiceImpl patientServiceImpl;

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    @GetMapping("get-details/staff/{id}")
    public ResponseEntity<ApiResponse<PatientDetails>> getPatients(@PathVariable String id){
        ApiResponse<PatientDetails> response = patientServiceImpl.getPatientFullDetailsById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    @PostMapping("details/staff")
    public ResponseEntity<ApiResponse<PatientDetails>> savePatientDetailsByStaff(@RequestBody PatientDetails patientDetails){

//        PatientDetails patientDetailsCreated =  patientServiceImpl.registerPatientByStaff(patientDetails);
//        ApiResponse<PatientDetails> response = new ApiResponse<>(
//                true,
//                "Patient registered successful",
//                patientDetailsCreated
//        );

        ApiResponse<PatientDetails> response = patientServiceImpl.registerPatientByStaff(patientDetails);

        return ResponseEntity.status(HttpStatus.OK).body(response);

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





}
