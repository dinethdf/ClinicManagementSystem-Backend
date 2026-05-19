package com.dinethdf.ClinicManagementSystem.Service.DoctorSchedule;

import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleCreateDTO;
import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleListDTO;
import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleUpdateDTO;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface DoctorScheduleService {

    boolean validateScheduleOwnership(Long doctorId, Long scheduleId);

    DoctorScheduleResponseDTO createSchedule(DoctorScheduleCreateDTO createDTO);

    Page<DoctorScheduleResponseDTO> getAllSchedulesList(
            Long doctorId,
            Integer specializationId,
            LocalDate startDate, // New
            LocalDate endDate,   // New
            Pageable pageable);

    /**
     * --- NEW BATCH METHOD SIGNATURE ---
     */
    List<DoctorScheduleResponseDTO> createBatchSchedules(List<DoctorScheduleCreateDTO> createDTOs);

    /**
     * Finds a schedule by its ID.
     * @param id The ID of the schedule.
     * @return The schedule as a Response DTO.
     * @throws ValidationException if schedule is not found.
     */
    DoctorScheduleResponseDTO getScheduleById(Long id);

    List<DoctorScheduleResponseDTO> getAllSchedules(boolean includeInactive);


    Page<DoctorScheduleResponseDTO> getSchedulesByDoctorId(Long doctorId, boolean includeInactive, Pageable pageable);

    /**
     * Retrieves all upcoming (today and future) active schedules for a specific doctor.
     * @param doctorId The ID of the doctor.
     * @return A list of upcoming active schedule DTOs.
     */
    List<DoctorScheduleResponseDTO> getUpcomingSchedulesByDoctorId(Long doctorId);

    Page<DoctorScheduleResponseDTO> getUpcomingSchedules(String doctorName,String specialization,LocalDate searchDate,Pageable pageable);

    /**
     * Updates an existing schedule.
     * @param id The ID of the schedule to update.
     * @param updateDTO DTO containing fields to update.
     * @return The updated schedule as a Response DTO.
     * @throws ValidationException if schedule is not found.
     */
    DoctorScheduleResponseDTO updateSchedule(Long id, DoctorScheduleUpdateDTO updateDTO);

    /**
     * Deactivates a schedule (soft delete).
     * @param id The ID of the schedule to deactivate.
     * @return The deactivated schedule DTO.
     * @throws ValidationException if schedule is not found.
     */
    DoctorScheduleResponseDTO deactivateSchedule(Long id);

    /**
     * Reactivates a previously deactivated schedule.
     * @param id The ID of the schedule to reactivate.
     * @return The reactivated schedule DTO.
     * @throws ValidationException if schedule is not found.
     */
    DoctorScheduleResponseDTO reactivateSchedule(Long id);

    /**
     * Permanently deletes a schedule from the database. Use with caution.
     * @param id The ID of the schedule to delete.
     * @throws ValidationException if schedule is not found.
     */
    void hardDeleteSchedule(Long id);

    /**
     * Closes booking for a schedule early.
     * @param id The ID of the schedule to close booking for.
     * @return The updated schedule DTO.
     */
    DoctorScheduleResponseDTO closeBooking(Long id);

    /**
     * Force syncs the schedule queue to Firebase in case of failure during creation.
     * @param id The ID of the schedule.
     */
    void syncQueueToFirebase(Long id);

    /**
     * Manually completes a schedule session.
     * @param id The ID of the schedule to complete.
     * @return The updated schedule DTO.
     */
    DoctorScheduleResponseDTO completeSchedule(Long id);

    /**
     * Cascades schedule cancellation by cancelling all active appointments.
     * @param id The ID of the schedule to cancel.
     * @param staffId The ID of the staff member performing the cancellation.
     * @param cancelDTO Cancellation details.
     * @return The deactivated schedule DTO.
     */
    DoctorScheduleResponseDTO cancelCascadeSchedule(Long id, Long staffId, com.dinethdf.ClinicManagementSystem.DTO.Appointment.AppointmentCancelDTO cancelDTO);
}