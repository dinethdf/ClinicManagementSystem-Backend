package com.dinethdf.ClinicManagementSystem.Controller.Doctor;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.Model.Doctor.Specialization;
import com.dinethdf.ClinicManagementSystem.Service.Doctor.SpecializationService;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/specialization")
public class SpecializationController {

    @Autowired
    private SpecializationService specializationService;

    @Autowired
    private MessageSourceUtil messageSourceUtil;

    /**
     * Get All Specializations (or Search by Name query)
     * Usage: GET /api/specialization/list?query=Cardio
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<Specialization>>> getAllSpecializations(
            @RequestParam(required = false) String query) {

        List<Specialization> specializations;

        if (query != null && !query.trim().isEmpty()) {
            specializations = specializationService.searchSpecializations(query);
        } else {
            specializations = specializationService.getAllSpecializations();
        }

        return ResponseEntity.ok(ApiResponse.success(
                specializations,
                messageSourceUtil.getMessage("common.success.retrieve") // Or hardcoded string "Successfully retrieved list"
        ));
    }

    /**
     * Create New Specialization
     */
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Specialization>> createSpecialization(
            @RequestBody Specialization specialization) {

        Specialization created = specializationService.createSpecialization(specialization);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                created,
                messageSourceUtil.getMessage("common.success.create") // "Successfully created"
        ));
    }

    /**
     * Update Existing Specialization
     */
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<Specialization>> updateSpecialization(
            @PathVariable Integer id,
            @RequestBody Specialization specialization) {

        Specialization updated = specializationService.updateSpecialization(id, specialization);

        return ResponseEntity.ok(ApiResponse.success(
                updated,
                messageSourceUtil.getMessage("common.success.update") // "Successfully updated"
        ));
    }

    /**
     * Delete Specialization
     */
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSpecialization(@PathVariable Integer id) {

        specializationService.deleteSpecialization(id);

        return ResponseEntity.ok(ApiResponse.success(
                null,
                messageSourceUtil.getMessage("common.success.delete") // "Successfully deleted"
        ));
    }
}