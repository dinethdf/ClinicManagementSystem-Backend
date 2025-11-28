package com.dinethdf.ClinicManagementSystem.Service.DoctorSchedule;

import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleListDTO;
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
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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

        validateCreateDTO(createDTO);

        Doctor doctor = findDoctorById(createDTO.getDoctorId());

        if (createDTO.getScheduleDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Schedule date cannot be in the past", "PAST_DATE", HttpStatus.BAD_REQUEST);
        }

        boolean exists = doctorScheduleRepo.existsOverlappingSchedule(
                createDTO.getDoctorId(),
                createDTO.getScheduleDate(),
                createDTO.getStartTime(),
                createDTO.getEndTime()
        );

        if (exists) {
            throw new ValidationException(
                    messageSourceUtil.getMessage(
                            "schedule.error.duplicate.range", // Use a new message key
                            new Object[] { createDTO.getScheduleDate(), createDTO.getStartTime(), createDTO.getEndTime() }
                    ),
                    "DUPLICATE_SCHEDULE_RANGE",
                    HttpStatus.CONFLICT
            );
        }

        DoctorSchedule newSchedule = DoctorSchedule.builder()
                .doctor(doctor)
                .scheduleDate(createDTO.getScheduleDate())
                .startTime(createDTO.getStartTime())
                .endTime(createDTO.getEndTime())
                .doctorFee(createDTO.getDoctorFee())
                .clinicFee(createDTO.getClinicFee())
                .maxPatientCount(createDTO.getMaxPatientCount())
                .registeredPatientCount(0)
                .isActive(true)
                .build();


        DoctorSchedule savedSchedule = doctorScheduleRepo.save(newSchedule);
        return mapper.toResponseDTO(savedSchedule);
    }

    @Override
    public Page<DoctorScheduleResponseDTO> getAllSchedulesList(
            String doctorName,
            String specialization,
            LocalDate searchDate,
            Pageable pageable) {

        LocalTime now = LocalTime.now().truncatedTo(ChronoUnit.SECONDS);
        LocalDate today = LocalDate.now();

        Page<DoctorSchedule> schedulePage = doctorScheduleRepo.findAllSchedules(
                today,
                now,
                doctorName,
                specialization,
                searchDate,
                pageable
        );

        // Map Entity Page to DTO Page
        return schedulePage.map(mapper::toResponseDTO);
    }

    private DoctorScheduleListDTO mapToDTO(DoctorSchedule s) {
        String fName = (s.getDoctor() != null) ? s.getDoctor().getFirstName() : "Unknown";
        String lName = (s.getDoctor() != null) ? s.getDoctor().getLastName() : "";
        String specName = (s.getDoctor() != null && s.getDoctor().getSpecialization() != null)
                ? s.getDoctor().getSpecialization().getName()
                : "General";

        return new DoctorScheduleListDTO(
                s.getId(),
                fName,
                lName,
                specName,
                s.getScheduleDate(),
                s.getStartTime(),
                s.getEndTime(),
                s.getRegisteredPatientCount(),
                s.getMaxPatientCount(),
                Boolean.TRUE.equals(s.getIsActive())
        );

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
        List<DoctorSchedule> schedules = doctorScheduleRepo.findUpcomingSchedulesByDoctorId(
                doctorId, LocalDate.now(), now); // .minusDays(1) to include today

        return schedules.stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorScheduleResponseDTO> getUpcomingSchedules(
                String doctorName,
                String specialization,
                LocalDate searchDate,
                Pageable pageable) {

            LocalTime now = LocalTime.now().truncatedTo(ChronoUnit.SECONDS);
            LocalDate today = LocalDate.now();

            // Fetch paginated data
            Page<DoctorSchedule> schedulePage = doctorScheduleRepo.findUpcomingSchedules(
                    today,
                    now,
                    doctorName,
                    specialization,
                    searchDate,
                    pageable
            );

            // Map Entity Page to DTO Page
            return schedulePage.map(mapper::toResponseDTO);
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
        if (updateDTO.getDoctorFee() == null || updateDTO.getDoctorFee().doubleValue() < 0) {
            throw new ValidationException("Doctor Fee must be > 0", "INVALID_DOCTOR_FEE", HttpStatus.BAD_REQUEST);
        }
        if (updateDTO.getClinicFee() == null || updateDTO.getClinicFee().doubleValue() < 0) {
            throw new ValidationException("Clinic Fee must be > 0", "INVALID_CLINIC_FEE", HttpStatus.BAD_REQUEST);
        }


        boolean exists = doctorScheduleRepo.existsOverlappingScheduleForUpdate(
                existingSchedule.getDoctor().getId(),
                existingSchedule.getScheduleDate(),
                updateDTO.getStartTime(),
                updateDTO.getEndTime(),
                id
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
        existingSchedule.setDoctorFee(updateDTO.getDoctorFee());
        existingSchedule.setClinicFee(updateDTO.getClinicFee());
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
        if (schedule.getDoctorFee() == null || schedule.getDoctorFee().doubleValue() < 0) {
            throw new ValidationException("Doctor Fee must be > 0", "INVALID_DOCTOR_FEE", HttpStatus.BAD_REQUEST);
        }
        if (schedule.getClinicFee() == null || schedule.getClinicFee().doubleValue() < 0) {
            throw new ValidationException("Clinic Fee must be > 0", "INVALID_CLINIC_FEE", HttpStatus.BAD_REQUEST);
        }
    }
}
