package com.dinethdf.ClinicManagementSystem.Service.DoctorSchedule;

import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleCreateDTO;
import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleListDTO;
import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleUpdateDTO;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface DoctorScheduleService {

    /**
     * Creates a new doctor schedule.
     * @param createDTO DTO containing schedule details.
     * @return The created schedule as a Response DTO.
     */
    DoctorScheduleResponseDTO createSchedule(DoctorScheduleCreateDTO createDTO);

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

    Page<DoctorScheduleListDTO> getAllSchedules(String search, LocalDate date, Pageable pageable);

    /**
     * Retrieves all schedules, with an option to include inactive ones.
     * @param includeInactive If true, returns all schedules. If false, returns only active.
     * @return A list of schedule Response DTOs.
     */
    List<DoctorScheduleResponseDTO> getAllSchedules(boolean includeInactive);

    /**
     * Retrieves all schedules for a specific doctor.
     * @param doctorId The ID of the doctor.
     * @param includeInactive If true, returns all schedules for the doctor. If false, only active.
     * @return A list of schedule Response DTOs.
     */
    List<DoctorScheduleResponseDTO> getSchedulesByDoctorId(Long doctorId, boolean includeInactive);

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
}