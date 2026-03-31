package com.dinethdf.ClinicManagementSystem.Controller.Patient;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.*;
import com.dinethdf.ClinicManagementSystem.Model.Patient.Patient;
import com.dinethdf.ClinicManagementSystem.Service.Patient.PatientServiceImpl;

import com.dinethdf.ClinicManagementSystem.util.BarcodeService;
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

    @Autowired
    private BarcodeService barcodeService;


    @PreAuthorize("hasAnyAuthority('ADMIN', 'DOCTOR', 'STAFF')")
    @GetMapping("details/staff/{id}")
    public ResponseEntity<ApiResponse<Patient>> getPatients(@PathVariable String id){
        ApiResponse<Patient> response = patientServiceImpl.getPatientDetailsByIdStaff(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @PreAuthorize("hasAnyAuthority('ADMIN', 'DOCTOR', 'STAFF')")
    @GetMapping("/by-barcode/{barcode}")
    public ResponseEntity<ApiResponse<Patient>> getPatientByBarcode(@PathVariable String barcode) {

        Long decodedId = barcodeService.decodeId(barcode);

        if (decodedId == 0) {
            // FIX: Removed the second 'null' argument to match your DTO
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid Barcode"));
        }

        ApiResponse<Patient> response = patientServiceImpl.getPatientDetailsByIdStaff(String.valueOf(decodedId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'DOCTOR', 'STAFF')")
    @GetMapping("/barcode/{id}")
    public ResponseEntity<ApiResponse<String>> getPatientBarcode(@PathVariable Long id) {

        String barcode = barcodeService.encodeId(id);
        return ResponseEntity.ok(
                ApiResponse.success(barcode, "Barcode generated successfully")
        );
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
    public ResponseEntity<ApiResponse<Page<PatientListDTO>>> getPatientList(
            @RequestParam(required = false) String patientNo,
            @RequestParam(required = false) String nic,
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String name,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PatientListDTO> patients = patientServiceImpl.getAllPatientsPaginated(
                patientNo,
                nic,
                mobile,
                name,
                pageable
        );
        return ResponseEntity.ok(ApiResponse.success(patients, "Patient list fetched successfully"));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    @PostMapping("details/temp-staff")
    public ResponseEntity<ApiResponse<Patient>> saveTempPatientDetailsByStaff(@RequestBody Patient patient){
        ApiResponse<Patient> response = patientServiceImpl.registerTempPatientByStaff(patient);
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

    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    @PatchMapping("status/{id}")
    public ResponseEntity<ApiResponse<String>> togglePatientStatus(@PathVariable Integer id) {
        ApiResponse<String> response = patientServiceImpl.togglePatientStatus(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    @DeleteMapping("delete/staff/{id}")
    public ResponseEntity<ApiResponse<String>> deletePatientDetailsByStaff(@PathVariable Integer id){
        ApiResponse<String> response = patientServiceImpl.deletePatient(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/create-login/{patientId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<String>> createLogin(@PathVariable Long patientId) {

        patientServiceImpl.createPatientLogin(patientId);

        return ResponseEntity.ok(ApiResponse.success(
                "Login created successfully. Credentials sent to patient's email.",
                "SUCCESS"
        ));
    }


    // 1. Get Login Status
    @GetMapping("/{patientId}/login-status")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<PatientLoginStatusDTO>> getLoginStatus(@PathVariable Long patientId) {
        return ResponseEntity.ok(ApiResponse.success(
                patientServiceImpl.getPatientLoginStatus(patientId),
                "Status retrieved successfully"
        ));
    }

    // 3. Trigger Password Reset
    @PostMapping("/{patientId}/password-reset")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<String>> triggerPasswordReset(@PathVariable Long patientId) {
        patientServiceImpl.triggerPasswordReset(patientId);
        return ResponseEntity.ok(ApiResponse.success(
                "Password reset link has been sent to the patient's email.",
                "SUCCESS"
        ));
    }

    // 4. Toggle Login Status (Enable/Disable)
    @PostMapping("/{patientId}/toggle-login")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<String>> toggleLoginStatus(
            @PathVariable Long patientId,
            @RequestParam String action) { // action = "enable" or "disable"

        patientServiceImpl.togglePatientLoginStatus(patientId, action);
        return ResponseEntity.ok(ApiResponse.success(
                "Patient login has been " + action + "d.",
                "SUCCESS"
        ));
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
