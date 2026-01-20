package com.dinethdf.ClinicManagementSystem.Controller.Doctor;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Doctor.DoctorDetailsDto;
import com.dinethdf.ClinicManagementSystem.DTO.Doctor.DoctorResponseDto;
import com.dinethdf.ClinicManagementSystem.DTO.Doctor.DoctorUpdateDto;
import com.dinethdf.ClinicManagementSystem.Model.Doctor.Doctor;
import com.dinethdf.ClinicManagementSystem.Service.Doctor.DoctorServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Page<DoctorResponseDto>>> getAllDoctors(
            // Specific Filters
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String slmc,
            @RequestParam(required = false) String specialization,

            // Dynamic Pagination & Sorting (Default sort by ID desc)
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        Page<DoctorResponseDto> doctorsPage = doctorServiceImpl.getAllDoctorsPaginated(
                name,
                mobile,
                slmc,
                specialization,
                pageable
        );

        return ResponseEntity.ok(ApiResponse.success(
                doctorsPage,
                "Successfully retrieved doctors list"
        ));
    }

//    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
//    @GetMapping
//    public ResponseEntity<ApiResponse<List<Doctor>>> getAllDoctors() {
//        List<Doctor> doctors = doctorServiceImpl.getAllDoctors();
//
//        ApiResponse<List<Doctor>> response = ApiResponse.success(
//                doctors,
//                "Successfully retrieved all doctors"
//        );
//        return ResponseEntity.ok(response);
//    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    @GetMapping("get-details/staff/{id}")
    public ResponseEntity<ApiResponse<DoctorDetailsDto>> getPatients(@PathVariable String id){
        ApiResponse<DoctorDetailsDto> response = doctorServiceImpl.getDoctorFullDetailsById(id);
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

    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'USER', 'DOCTOR')")
    @PostMapping("register")
    public ResponseEntity<ApiResponse<Doctor>> saveDoctorDetailsByStaff(@RequestBody Doctor doctor){
        ApiResponse<Doctor> response = doctorServiceImpl.registerDoctor(doctor);
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }
    // Update Doctor Details
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'USER', 'DOCTOR')")
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<DoctorResponseDto>> updateDoctor(
            @PathVariable Long id,
            @RequestBody DoctorUpdateDto updateDto) {

        DoctorResponseDto result = doctorServiceImpl.editDoctorDetails(id, updateDto);
        return ResponseEntity.ok(ApiResponse.success(result, "Doctor updated successfully"));
    }

    // Change Availability Status
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'USER', 'DOCTOR')")
    @PatchMapping("/status/{id}")
    public ResponseEntity<ApiResponse<DoctorResponseDto>> changeStatus(
            @PathVariable Long id,
            @RequestParam String status) { // e.g. ?status=ON_LEAVE

        DoctorResponseDto result = doctorServiceImpl.updateDoctorStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(result, "Status updated successfully"));
    }

    // Approve Doctor
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'USER', 'DOCTOR')")
    @PatchMapping("/approve/{id}")
    public ResponseEntity<ApiResponse<DoctorResponseDto>> approveDoctor(@PathVariable Long id) {
        DoctorResponseDto result = doctorServiceImpl.approveDoctor(id);
        return ResponseEntity.ok(ApiResponse.success(result, "Doctor approved successfully"));
    }

    // Delete Doctor
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDoctor(@PathVariable Long id) {
        doctorServiceImpl.deleteDoctor(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Doctor deleted successfully"));
    }

}
