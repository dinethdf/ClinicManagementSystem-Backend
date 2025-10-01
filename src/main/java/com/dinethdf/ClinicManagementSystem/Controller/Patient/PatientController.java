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

    @GetMapping("/a/b")
    public String getPatients(){
        System.out.println("Hellos");
        return "Patient";
    }

    @GetMapping("/a/c")
    public String getPatients2(){
        System.out.println("Hello");
        return "Patient";
    }

    @PreAuthorize("permitAll")
    @PostMapping("details/self")
    public ResponseEntity<ApiResponse<PatientDetails>> savePatientDetailsBySelf(@RequestBody PatientDetails patientDetails){
        System.out.println("Patient Details register Self");
        System.out.println("Paatient details");
        System.out.println(patientDetails.toString());
        PatientDetails patientDetailsCreated =  patientServiceImpl.addOrUpdatePatientDetailsByStaff(patientDetails);
        ApiResponse<PatientDetails> response = new ApiResponse<>(
                true,
                "Patient registered successfully by self",
                patientDetailsCreated
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PreAuthorize("hasAnyAuthority('USER')")
    @PostMapping("details/staff")
    public ResponseEntity<ApiResponse<PatientResponseDto>> savePatientDetailsByStaff(){
        System.out.println("Patient Details register Staff");

        PatientResponseDto patientResponseDto = new PatientResponseDto(1L,"asd", "asd", "swaps");

        ApiResponse<PatientResponseDto> response = new ApiResponse<>(
                true,
                "Patient registered successfully By staff",
                patientResponseDto
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping("b/b")
    public String getPatients4(){
        System.out.println("Hello2");
        return "Patient2";
    }


}
