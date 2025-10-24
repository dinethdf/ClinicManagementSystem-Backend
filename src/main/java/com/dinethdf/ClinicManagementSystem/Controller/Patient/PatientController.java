package com.dinethdf.ClinicManagementSystem.Controller.Patient;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.Model.Patient.Patient;
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
    @GetMapping("details/staff/{id}")
    public ResponseEntity<ApiResponse<Patient>> getPatients(@PathVariable String id){
        ApiResponse<Patient> response = patientServiceImpl.getPatientDetailsByIdStaff(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    @PostMapping("details/staff")
    public ResponseEntity<ApiResponse<Patient>> savePatientDetailsByStaff(@RequestBody Patient patient){
        ApiResponse<Patient> response = patientServiceImpl.registerPatientByStaff(patient);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    @PutMapping("details/staff/{id}")
    public ResponseEntity<ApiResponse<Patient>> updatePatientDetailsByStaff(@PathVariable Integer id, @RequestBody Patient patient){
        ApiResponse<Patient> response = patientServiceImpl.updatePatientByStaff(id, patient);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    @PatchMapping("approve/staff/{id}")
    public ResponseEntity<ApiResponse<String>> updatePatientDetailsByStaff(@PathVariable Integer id){
        ApiResponse<String> response = patientServiceImpl.approvePatient(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    @DeleteMapping("delete/staff/{id}")
    public ResponseEntity<ApiResponse<String>> deletePatientDetailsByStaff(@PathVariable Integer id){
        ApiResponse<String> response = patientServiceImpl.deletePatient(id);
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
