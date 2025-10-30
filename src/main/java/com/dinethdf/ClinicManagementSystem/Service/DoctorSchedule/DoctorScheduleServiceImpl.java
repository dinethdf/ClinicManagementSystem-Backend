package com.dinethdf.ClinicManagementSystem.Service.DoctorSchedule;

import com.dinethdf.ClinicManagementSystem.Model.DoctorShedule.DoctorSchedule;
import com.dinethdf.ClinicManagementSystem.Model.Doctor.Doctor;
import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleCreateDTO;
import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleUpdateDTO;
import com.dinethdf.ClinicManagementSystem.Repo.Doctor.DoctorRepo;
import com.dinethdf.ClinicManagementSystem.Repo.DoctorSchedule.DoctorScheduleRepo;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.mapper.DoctorScheduleMapper;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Automatically injects final fields
@Transactional // Makes all public methods transactional by default
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    private final DoctorScheduleRepo doctorScheduleRepo;
    private final DoctorRepo doctorRepo;
    private final DoctorScheduleMapper mapper; // Use a mapper for DTO conversion
    private final MessageSourceUtil messageSourceUtil;

    @Override
    public DoctorScheduleResponseDTO createSchedule(DoctorScheduleCreateDTO createDTO) {
        // 1. Validate DTO input
        validateCreateDTO(createDTO); // This helper is also updated (see below)

        // 2. Find the associated Doctor
        Doctor doctor = findDoctorById(createDTO.getDoctorId());

        // 3. Perform business logic validation (Date)
        if (createDTO.getScheduleDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Schedule date cannot be in the past", "PAST_DATE", HttpStatus.BAD_REQUEST);
        }

        // 4. Perform business logic validation (Time Overlap)
        // We now call our new repository method
        boolean exists = doctorScheduleRepo.existsOverlappingSchedule(
                createDTO.getDoctorId(),
                createDTO.getScheduleDate(),
                createDTO.getStartTime(),
                createDTO.getEndTime()
        );

        if (exists) {
            // Throw an updated error message
            throw new ValidationException(
                    messageSourceUtil.getMessage(
                            "schedule.error.duplicate.range", // Use a new message key
                            new Object[] { createDTO.getScheduleDate(), createDTO.getStartTime(), createDTO.getEndTime() }
                    ),
                    "DUPLICATE_SCHEDULE_RANGE",
                    HttpStatus.CONFLICT
            );
        }

        // 5. Map DTO to Entity
        // We now set startTime and endTime
        DoctorSchedule newSchedule = DoctorSchedule.builder()
                .doctor(doctor)
                .scheduleDate(createDTO.getScheduleDate())
                .startTime(createDTO.getStartTime()) // Updated
                .endTime(createDTO.getEndTime())     // Updated
                .payment(createDTO.getPayment())
                .maxPatientCount(createDTO.getMaxPatientCount())
                .registeredPatientCount(0)
                .isActive(true)
                .build();

        // 6. Save and return DTO
        DoctorSchedule savedSchedule = doctorScheduleRepo.save(newSchedule);
        return mapper.toResponseDTO(savedSchedule);
    }

    /**
     * --- NEW BATCH METHOD IMPLEMENTATION ---
     */
    @Override
    @Transactional // This is explicit, but inherits from the class anyway
    public List<DoctorScheduleResponseDTO> createBatchSchedules(List<DoctorScheduleCreateDTO> createDTOs) {

        // This re-uses all your validation for every item. and call the 'createSchedule' method for each one.
        return createDTOs.stream()
                .map(this::createSchedule)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true) // Mark as read-only for performance
    public DoctorScheduleResponseDTO getScheduleById(Long id) {
        DoctorSchedule schedule = findScheduleById(id);
        return mapper.toResponseDTO(schedule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorScheduleResponseDTO> getAllSchedules(boolean includeInactive) {
        List<DoctorSchedule> schedules;
        if (includeInactive) {
            schedules = doctorScheduleRepo.findAll();
        } else {
            schedules = doctorScheduleRepo.findByIsActiveTrue();
        }
        return schedules.stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorScheduleResponseDTO> getSchedulesByDoctorId(Long doctorId, boolean includeInactive) {
        // Ensure doctor exists first
        findDoctorById(doctorId);

        List<DoctorSchedule> schedules;
        if (includeInactive) {
            schedules = doctorScheduleRepo.findByDoctorId(doctorId);
        } else {
            schedules = doctorScheduleRepo.findByDoctorIdAndIsActiveTrue(doctorId);
        }
        return schedules.stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorScheduleResponseDTO> getUpcomingSchedulesByDoctorId(Long doctorId) {

        findDoctorById(doctorId);

        LocalTime now = LocalTime.now().truncatedTo(ChronoUnit.SECONDS);
        List<DoctorSchedule> schedules = doctorScheduleRepo.findUpcomingSchedules(
                doctorId, LocalDate.now(), now); // .minusDays(1) to include today

        return schedules.stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional // Make sure your method is transactional
    public DoctorScheduleResponseDTO updateSchedule(Long id, DoctorScheduleUpdateDTO updateDTO) {
        // 1. Find existing schedule
        DoctorSchedule existingSchedule = findScheduleById(id);

        // 2. Validate new DTO data
        if (updateDTO.getStartTime() == null) {
            throw new ValidationException("Start time is required", "START_TIME_REQUIRED", HttpStatus.BAD_REQUEST);
        }
        if (updateDTO.getEndTime() == null) {
            throw new ValidationException("End time is required", "END_TIME_REQUIRED", HttpStatus.BAD_REQUEST);
        }
        if (updateDTO.getStartTime().isAfter(updateDTO.getEndTime()) || updateDTO.getStartTime().equals(updateDTO.getEndTime())) {
            throw new ValidationException("End time must be after start time", "INVALID_TIME_RANGE", HttpStatus.BAD_REQUEST);
        }
        if (updateDTO.getMaxPatientCount() == null || updateDTO.getMaxPatientCount() <= 0) {
            throw new ValidationException("Max patient count must be greater than 0", "INVALID_PATIENT_COUNT", HttpStatus.BAD_REQUEST);
        }
        if (updateDTO.getPayment() == null || updateDTO.getPayment().doubleValue() < 0) {
            throw new ValidationException("Payment must be >= 0", "INVALID_PAYMENT", HttpStatus.BAD_REQUEST);
        }

        // 3. NEW: Check for overlapping time conflicts with *other* schedules
        // We use the date and doctor from the *existing* schedule
        boolean exists = doctorScheduleRepo.existsOverlappingScheduleForUpdate(
                existingSchedule.getDoctor().getId(),
                existingSchedule.getScheduleDate(),
                updateDTO.getStartTime(),
                updateDTO.getEndTime(),
                id // The ID of the schedule we are updating
        );

        if (exists) {
            throw new ValidationException(
                    messageSourceUtil.getMessage(
                            "schedule.error.duplicate.range.update", // A new message key
                            new Object[] { existingSchedule.getScheduleDate(), updateDTO.getStartTime(), updateDTO.getEndTime() }
                    ),
                    "DUPLICATE_SCHEDULE_RANGE",
                    HttpStatus.CONFLICT
            );
        }

        // 4. Update fields
        existingSchedule.setStartTime(updateDTO.getStartTime());
        existingSchedule.setEndTime(updateDTO.getEndTime());
        existingSchedule.setPayment(updateDTO.getPayment());
        existingSchedule.setMaxPatientCount(updateDTO.getMaxPatientCount());

        // 5. Save and return DTO
        DoctorSchedule updatedSchedule = doctorScheduleRepo.save(existingSchedule);
        return mapper.toResponseDTO(updatedSchedule);
    }

    @Override
    public DoctorScheduleResponseDTO deactivateSchedule(Long id) {
        DoctorSchedule schedule = findScheduleById(id);

        if (!schedule.getIsActive()) {
            throw new ValidationException("Schedule is already inactive", "ALREADY_INACTIVE", HttpStatus.BAD_REQUEST);
        }

        schedule.setIsActive(false);
        DoctorSchedule savedSchedule = doctorScheduleRepo.save(schedule);
        return mapper.toResponseDTO(savedSchedule);
    }

    @Override
    public DoctorScheduleResponseDTO reactivateSchedule(Long id) {
        DoctorSchedule schedule = findScheduleById(id);

        if (schedule.getIsActive()) {
            throw new ValidationException("Schedule is already active", "ALREADY_ACTIVE", HttpStatus.BAD_REQUEST);
        }

        // Add logic here if reactivating a past schedule is not allowed
        if (schedule.getScheduleDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Cannot reactivate a schedule in the past", "PAST_DATE", HttpStatus.BAD_REQUEST);
        }

        schedule.setIsActive(true);
        DoctorSchedule savedSchedule = doctorScheduleRepo.save(schedule);
        return mapper.toResponseDTO(savedSchedule);
    }

    @Override
    public void hardDeleteSchedule(Long id) {
        // Check if it exists first
        DoctorSchedule schedule = findScheduleById(id);

        // Add business logic here. E.g., Don't allow delete if
        // appointments are already booked for this schedule.
        // if (appointmentRepo.existsByScheduleId(id)) {
        //    throw new ValidationException("Cannot delete schedule with active appointments", "SCHEDULE_IN_USE", HttpStatus.CONFLICT);
        // }

        doctorScheduleRepo.delete(schedule);
    }

    // --- Private Helper Methods ---

    /**
     * Finds a schedule by ID or throws ResourceNotFoundException.
     */
    private DoctorSchedule findScheduleById(Long id) {
        return doctorScheduleRepo.findById(id)
                .orElseThrow(() -> new ValidationException("  not found with id: " + id, "DOCTOR_SCHEDULE_NOT_FOUND"));
    }

    /**
     * Finds a doctor by ID or throws ResourceNotFoundException.
     */
    private Doctor findDoctorById(Long doctorId) {
        return doctorRepo.findById(doctorId)
                .orElseThrow(() -> new ValidationException("Doctor not found with id: " + doctorId, "DOCTOR_NOT_FOUND"));
    }

    /**
     * Validates the input DTO for creating a new schedule.
     */
    private void validateCreateDTO(DoctorScheduleCreateDTO schedule) {
        if (schedule.getDoctorId() == null) {
            throw new ValidationException("Doctor is required for schedule", "DOCTOR_REQUIRED", HttpStatus.BAD_REQUEST);
        }
        if (schedule.getScheduleDate() == null) {
            throw new ValidationException("Schedule date is required", "DATE_REQUIRED", HttpStatus.BAD_REQUEST);
        }

        // --- Updated Time Checks ---
        if (schedule.getStartTime() == null) {
            throw new ValidationException("Start time is required", "START_TIME_REQUIRED", HttpStatus.BAD_REQUEST);
        }
        if (schedule.getEndTime() == null) {
            throw new ValidationException("End time is required", "END_TIME_REQUIRED", HttpStatus.BAD_REQUEST);
        }
        if (schedule.getStartTime().isAfter(schedule.getEndTime()) || schedule.getStartTime().equals(schedule.getEndTime())) {
            throw new ValidationException("End time must be after start time", "INVALID_TIME_RANGE", HttpStatus.BAD_REQUEST);
        }
        // --- End Updated Time Checks ---

        if (schedule.getMaxPatientCount() == null || schedule.getMaxPatientCount() <= 0) {
            throw new ValidationException("Max patient count must be greater than 0", "INVALID_PATIENT_COUNT", HttpStatus.BAD_REQUEST);
        }
        if (schedule.getPayment() == null || schedule.getPayment().doubleValue() < 0) {
            throw new ValidationException("Payment must be >= 0", "INVALID_PAYMENT", HttpStatus.BAD_REQUEST);
        }
    }
}
