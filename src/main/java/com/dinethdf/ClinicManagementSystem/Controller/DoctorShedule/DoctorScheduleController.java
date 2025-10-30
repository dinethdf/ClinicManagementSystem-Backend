package com.dinethdf.ClinicManagementSystem.Controller.DoctorShedule;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.Service.DoctorSchedule.DoctorScheduleService;
import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleCreateDTO;
import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleUpdateDTO;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/schedules") // Specific path for schedules
@RequiredArgsConstructor // Best practice for dependency injection
public class DoctorScheduleController {

    // Inject the Interface, not the implementation
    private final DoctorScheduleService doctorScheduleService;

    // Inject your message utility
    private final MessageSourceUtil messageSourceUtil;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'USER')")
    public ResponseEntity<ApiResponse<DoctorScheduleResponseDTO>> createSchedule(
            @RequestBody DoctorScheduleCreateDTO createDTO) {

        System.out.println("this is run");
        DoctorScheduleResponseDTO newSchedule = doctorScheduleService.createSchedule(createDTO);

        // Use the static helper method
        ApiResponse<DoctorScheduleResponseDTO> response = ApiResponse.success(
                newSchedule,
                messageSourceUtil.getMessage("schedule.create.success")
        );

        // Return 201 Created status
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



    @PostMapping("/batch")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'DOCTOR', 'USER')")
    public ResponseEntity<ApiResponse<List<DoctorScheduleResponseDTO>>> createBatchSchedules(
            @RequestBody List<DoctorScheduleCreateDTO> createDTOs) {

        // 1. Call the new service method
        List<DoctorScheduleResponseDTO> newSchedules = doctorScheduleService.createBatchSchedules(createDTOs);

        // 2. Create the success response
        ApiResponse<List<DoctorScheduleResponseDTO>> response = ApiResponse.success(
                newSchedules,
                messageSourceUtil.getMessage("schedule.batchcreate.success")
        );

        // 3. Return 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    public ResponseEntity<ApiResponse<DoctorScheduleResponseDTO>> getScheduleById(@PathVariable Long id) {

        DoctorScheduleResponseDTO schedule = doctorScheduleService.getScheduleById(id);

        ApiResponse<DoctorScheduleResponseDTO> response = ApiResponse.success(
                schedule,
                messageSourceUtil.getMessage("schedule.get.success")
        );

        return ResponseEntity.ok(response);
    }


    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'USER')")
    public ResponseEntity<ApiResponse<List<DoctorScheduleResponseDTO>>> getAllSchedules(
            @RequestParam(defaultValue = "false") boolean includeInactive) {

        List<DoctorScheduleResponseDTO> schedules = doctorScheduleService.getAllSchedules(includeInactive);

        ApiResponse<List<DoctorScheduleResponseDTO>> response = ApiResponse.success(
                schedules,
                messageSourceUtil.getMessage("schedule.getall.success")
        );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/by-doctor/{doctorId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    public ResponseEntity<ApiResponse<List<DoctorScheduleResponseDTO>>> getSchedulesByDoctor(
            @PathVariable Long doctorId,
            @RequestParam(defaultValue = "false") boolean includeInactive) {

        List<DoctorScheduleResponseDTO> schedules = doctorScheduleService.getSchedulesByDoctorId(doctorId, includeInactive);

        ApiResponse<List<DoctorScheduleResponseDTO>> response = ApiResponse.success(
                schedules,
                messageSourceUtil.getMessage("schedule.getbydoctor.success")
        );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/by-doctor/{doctorId}/upcoming")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'STAFF')")
    public ResponseEntity<ApiResponse<List<DoctorScheduleResponseDTO>>> getUpcomingSchedulesByDoctor(@PathVariable Long doctorId) {

        List<DoctorScheduleResponseDTO> schedules = doctorScheduleService.getUpcomingSchedulesByDoctorId(doctorId);

        ApiResponse<List<DoctorScheduleResponseDTO>> response = ApiResponse.success(
                schedules,
                messageSourceUtil.getMessage("schedule.getupcoming.success")
        );

        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<DoctorScheduleResponseDTO>> updateSchedule(
            @PathVariable Long id,
            @RequestBody DoctorScheduleUpdateDTO updateDTO) {

        DoctorScheduleResponseDTO updatedSchedule = doctorScheduleService.updateSchedule(id, updateDTO);

        ApiResponse<DoctorScheduleResponseDTO> response = ApiResponse.success(
                updatedSchedule,
                messageSourceUtil.getMessage("schedule.update.success")
        );

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<DoctorScheduleResponseDTO>> deactivateSchedule(@PathVariable Long id) {

        DoctorScheduleResponseDTO deactivatedSchedule = doctorScheduleService.deactivateSchedule(id);

        ApiResponse<DoctorScheduleResponseDTO> response = ApiResponse.success(
                deactivatedSchedule,
                messageSourceUtil.getMessage("schedule.deactivate.success")
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Reactivate a schedule.
     * Accessible by ADMIN or STAFF.
     */
    @PostMapping("/{id}/reactivate") // Using POST for an action
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<DoctorScheduleResponseDTO>> reactivateSchedule(@PathVariable Long id) {

        DoctorScheduleResponseDTO reactivatedSchedule = doctorScheduleService.reactivateSchedule(id);

        ApiResponse<DoctorScheduleResponseDTO> response = ApiResponse.success(
                reactivatedSchedule,
                messageSourceUtil.getMessage("schedule.reactivate.success")
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Permanently delete a schedule.
     * Accessible by ADMIN only.
     */
    @DeleteMapping("/{id}/force-delete")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> hardDeleteSchedule(@PathVariable Long id) {

        doctorScheduleService.hardDeleteSchedule(id);

        // We pass 'null' for the data since there is no object to return
        ApiResponse<Object> response = ApiResponse.success(
                null,
                messageSourceUtil.getMessage("schedule.harddelete.success")
        );

        return ResponseEntity.ok(response);
    }
}