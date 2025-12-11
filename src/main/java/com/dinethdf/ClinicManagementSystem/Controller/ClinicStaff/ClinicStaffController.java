package com.dinethdf.ClinicManagementSystem.Controller.ClinicStaff;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.ClinicStaff.ClinicStaffResponseDto;
import com.dinethdf.ClinicManagementSystem.DTO.ClinicStaff.ClinicStaffUpdateDto;
import com.dinethdf.ClinicManagementSystem.Model.ClinicStaff.ClinicStaff;
import com.dinethdf.ClinicManagementSystem.Service.ClinicStaff.ClinicStaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff")
public class ClinicStaffController {

    @Autowired
    private ClinicStaffService clinicStaffService;

    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClinicStaffResponseDto>> getStaffById(@PathVariable Long id) {

        ClinicStaffResponseDto staffDto = clinicStaffService.getStaffById(id);

        return ResponseEntity.ok(ApiResponse.success(
                staffDto,
                "Staff details retrieved successfully"
        ));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')") // Usually only Admins register staff
    @PostMapping("register")
    public ResponseEntity<ApiResponse<ClinicStaff>> saveStaffDetails(@RequestBody ClinicStaff staff) {
        ApiResponse<ClinicStaff> response = clinicStaffService.registerStaff(staff);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Page<ClinicStaffResponseDto>>> getAllStaff(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ClinicStaffResponseDto> staffPage = clinicStaffService.getAllStaffPaginated(page, size);

        ApiResponse<Page<ClinicStaffResponseDto>> response = ApiResponse.success(
                staffPage,
                "Successfully retrieved staff list"
        );

        return ResponseEntity.ok(response);
    }

    // --- UPDATE ENDPOINT ---
    @PreAuthorize("hasAnyAuthority('ADMIN')") // Usually only Admin updates staff
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<ClinicStaffResponseDto>> updateStaff(
            @PathVariable Long id,
            @RequestBody ClinicStaffUpdateDto updateDto) {

        ClinicStaffResponseDto result = clinicStaffService.editStaffDetails(id, updateDto);
        return ResponseEntity.ok(ApiResponse.success(result, "Staff updated successfully"));
    }

}