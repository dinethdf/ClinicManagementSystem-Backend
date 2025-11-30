package com.dinethdf.ClinicManagementSystem.Controller.Patient;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.GuardianResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientListDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientSimpleResponseDTO;
import com.dinethdf.ClinicManagementSystem.Model.Patient.Patient;
import com.dinethdf.ClinicManagementSystem.Service.Patient.PatientServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/patient")
public class PatientController {

    @Autowired
    PatientServiceImpl patientServiceImpl;

    @PreAuthorize("hasAnyAuthority('ADMIN', 'DOCTOR', 'STAFF')")
    @GetMapping("details/staff/{id}")
    public ResponseEntity<ApiResponse<Patient>> getPatients(@PathVariable String id){
        ApiResponse<Patient> response = patientServiceImpl.getPatientDetailsByIdStaff(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    @GetMapping("/search-guardian")
    public ResponseEntity<List<GuardianResponseDTO>> searchGuardian(
            @RequestParam(required = false) String nic,
            @RequestParam(required = false) String fullName) {

        List<GuardianResponseDTO> results = patientServiceImpl.findGuardianByNicAndFullName(nic, fullName);
        return ResponseEntity.ok(results);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    @GetMapping("/search-patient")
    public ResponseEntity<List<PatientResponseDTO>> searchPatient(
            @RequestParam(required = false) String patientNo,
            @RequestParam(required = false) String nic,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String mobileNo) {

        List<PatientResponseDTO> results = patientServiceImpl.findPatientBySearching(patientNo, nic, mobileNo, fullName);
        return ResponseEntity.ok(results);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'DOCTOR')")
    @GetMapping("/search-by-no")
    public ResponseEntity<ApiResponse<List<PatientSimpleResponseDTO>>> searchPatientByNo(
            @RequestParam(required = true) String patientNo) {

        List<PatientSimpleResponseDTO> results = patientServiceImpl.findPatientOnlyByNo(patientNo);
        ApiResponse<List<PatientSimpleResponseDTO>> response = ApiResponse.success(
                results,
                "messageSourceUtil.getMessage(\"doctor.search.success\")"
        );
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    @GetMapping("/list")
    public ResponseEntity<Page<PatientListDTO>> getAllPatients(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PatientListDTO> patients = patientServiceImpl.getPatientList(search, pageable);
        return ResponseEntity.ok(patients);
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
