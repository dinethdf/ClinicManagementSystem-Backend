package com.dinethdf.ClinicManagementSystem.Service.DoctorSchedule;

import com.dinethdf.ClinicManagementSystem.Model.Appoinment.DoctorSchedule;
import com.dinethdf.ClinicManagementSystem.Model.Doctor.Doctor;
import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleCreateDTO;
import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleUpdateDTO;
import com.dinethdf.ClinicManagementSystem.Repo.Doctor.DoctorRepo;
import com.dinethdf.ClinicManagementSystem.Repo.DoctorSchedule.DoctorScheduleRepo;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.mapper.DoctorScheduleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Automatically injects final fields
@Transactional // Makes all public methods transactional by default
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    private final DoctorScheduleRepo doctorScheduleRepo;
    private final DoctorRepo doctorRepo;
    private final DoctorScheduleMapper mapper; // Use a mapper for DTO conversion

    @Override
    public DoctorScheduleResponseDTO createSchedule(DoctorScheduleCreateDTO createDTO) {
        // 1. Validate DTO input
        validateCreateDTO(createDTO);

        // 2. Find the associated Doctor
        Doctor doctor = findDoctorById(createDTO.getDoctorId());

        // 3. Perform business logic validation
        if (createDTO.getScheduleDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Schedule date cannot be in the past", "PAST_DATE", HttpStatus.BAD_REQUEST);
        }

        boolean exists = doctorScheduleRepo.existsByDoctorIdAndScheduleDateAndScheduleTime(
                createDTO.getDoctorId(), createDTO.getScheduleDate(), createDTO.getScheduleTime());

        if (exists) {
            throw new ValidationException("Doctor already has a schedule at this date and time", "DUPLICATE_SCHEDULE", HttpStatus.CONFLICT);
        }

        // 4. Map DTO to Entity
        DoctorSchedule newSchedule = DoctorSchedule.builder()
                .doctor(doctor)
                .scheduleDate(createDTO.getScheduleDate())
                .scheduleTime(createDTO.getScheduleTime())
                .payment(createDTO.getPayment())
                .maxPatientCount(createDTO.getMaxPatientCount())
                .isActive(true) // Default to active
                .build();

        // 5. Save and return DTO
        DoctorSchedule savedSchedule = doctorScheduleRepo.save(newSchedule);
        return mapper.toResponseDTO(savedSchedule);
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
        // Ensure doctor exists
        findDoctorById(doctorId);

        List<DoctorSchedule> schedules = doctorScheduleRepo.findByDoctorIdAndScheduleDateAfterAndIsActiveTrue(
                doctorId, LocalDate.now().minusDays(1)); // .minusDays(1) to include today

        return schedules.stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DoctorScheduleResponseDTO updateSchedule(Long id, DoctorScheduleUpdateDTO updateDTO) {
        // 1. Find existing schedule
        DoctorSchedule existingSchedule = findScheduleById(id);

        // 2. Validate new data
        if (updateDTO.getScheduleTime() == null) {
            throw new ValidationException("Schedule time is required", "TIME_REQUIRED", HttpStatus.BAD_REQUEST);
        }
        if (updateDTO.getMaxPatientCount() == null || updateDTO.getMaxPatientCount() <= 0) {
            throw new ValidationException("Max patient count must be greater than 0", "INVALID_PATIENT_COUNT", HttpStatus.BAD_REQUEST);
        }
        if (updateDTO.getPayment() == null || updateDTO.getPayment().doubleValue() < 0) {
            throw new ValidationException("Payment must be >= 0", "INVALID_PAYMENT", HttpStatus.BAD_REQUEST);
        }

        // 3. Update fields
        existingSchedule.setScheduleTime(updateDTO.getScheduleTime());
        existingSchedule.setPayment(updateDTO.getPayment());
        existingSchedule.setMaxPatientCount(updateDTO.getMaxPatientCount());

        // 4. Save and return DTO
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
        if (schedule.getScheduleTime() == null) {
            throw new ValidationException("Schedule time is required", "TIME_REQUIRED", HttpStatus.BAD_REQUEST);
        }
        if (schedule.getMaxPatientCount() == null || schedule.getMaxPatientCount() <= 0) {
            throw new ValidationException("Max patient count must be greater than 0", "INVALID_PATIENT_COUNT", HttpStatus.BAD_REQUEST);
        }
        if (schedule.getPayment() == null || schedule.getPayment().doubleValue() < 0) {
            throw new ValidationException("Payment must be >= 0", "INVALID_PAYMENT", HttpStatus.BAD_REQUEST);
        }
    }
}
