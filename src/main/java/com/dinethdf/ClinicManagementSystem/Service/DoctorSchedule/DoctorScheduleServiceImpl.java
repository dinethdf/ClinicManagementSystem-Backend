package com.dinethdf.ClinicManagementSystem.Service.DoctorSchedule;

import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleCreateDTO;
import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleUpdateDTO;
import com.dinethdf.ClinicManagementSystem.Model.Doctor.Doctor;
import com.dinethdf.ClinicManagementSystem.Model.DoctorShedule.DoctorSchedule;
import com.dinethdf.ClinicManagementSystem.Repo.Doctor.DoctorRepo;
import com.dinethdf.ClinicManagementSystem.Repo.DoctorSchedule.DoctorScheduleRepo;
import com.dinethdf.ClinicManagementSystem.config.TenantContext; // <--- Import Context
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.mapper.DoctorScheduleMapper;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    private final DoctorScheduleRepo doctorScheduleRepo;
    private final DoctorRepo doctorRepo;
    private final DoctorScheduleMapper mapper;
    private final MessageSourceUtil messageSourceUtil;

    @Override
    @Transactional(readOnly = true)
    public boolean validateScheduleOwnership(Long doctorId, Long scheduleId) {
        // Multi-Tenant Check
        return doctorScheduleRepo.existsByIdAndDoctorIdAndClinicId(
                scheduleId,
                doctorId,
                TenantContext.getCurrentTenant()
        );
    }

    @Override
    public DoctorScheduleResponseDTO createSchedule(DoctorScheduleCreateDTO createDTO) {

        validateCreateDTO(createDTO);

        Doctor doctor = findDoctorById(createDTO.getDoctorId());

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (createDTO.getScheduleDate().isBefore(today)) {
            throw new ValidationException("Schedule date cannot be in the past", "PAST_DATE", HttpStatus.BAD_REQUEST);
        }

        if (createDTO.getScheduleDate().equals(today) && createDTO.getStartTime().isBefore(now)) {
            throw new ValidationException(
                    "Start time cannot be in the past for today's schedule",
                    "PAST_TIME",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Multi-Tenant Overlap Check
        boolean exists = doctorScheduleRepo.existsOverlappingSchedule(
                createDTO.getDoctorId(),
                createDTO.getScheduleDate(),
                createDTO.getStartTime(),
                createDTO.getEndTime(),
                TenantContext.getCurrentTenant() // <--- Clinic ID
        );

        if (exists) {
            throw new ValidationException(
                    messageSourceUtil.getMessage(
                            "schedule.error.duplicate.range",
                            new Object[] { createDTO.getScheduleDate(), createDTO.getStartTime(), createDTO.getEndTime() }
                    ),
                    "DUPLICATE_SCHEDULE_RANGE",
                    HttpStatus.CONFLICT
            );
        }

        DoctorSchedule newSchedule = DoctorSchedule.builder()
                .clinicId(TenantContext.getCurrentTenant()) // <--- Set Clinic ID
                .doctor(doctor)
                .scheduleDate(createDTO.getScheduleDate())
                .startTime(createDTO.getStartTime())
                .endTime(createDTO.getEndTime())
                .doctorFee(createDTO.getDoctorFee())
                .clinicFee(createDTO.getClinicFee())
                .maxPatientCount(createDTO.getMaxPatientCount())
                .registeredPatientCount(0)
                //.isActive(true)  <-- optional, can remove if you want to rely on status
                .status(DoctorSchedule.ScheduleStatus.ACTIVE)
                .bookingStatus(DoctorSchedule.BookingStatus.OPEN)
                .payoutStatus(DoctorSchedule.PayoutStatus.NOT_PAID)
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

        // Pass Clinic ID to Repository
        Page<DoctorSchedule> schedulePage = doctorScheduleRepo.findAllSchedules(
                today,
                now,
                doctorName,
                specialization,
                searchDate,
                TenantContext.getCurrentTenant(), // <--- Clinic ID
                pageable
        );

        return schedulePage.map(mapper::toResponseDTO);
    }

    @Override
    @Transactional
    public List<DoctorScheduleResponseDTO> createBatchSchedules(List<DoctorScheduleCreateDTO> createDTOs) {
        return createDTOs.stream()
                .map(this::createSchedule)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorScheduleResponseDTO getScheduleById(Long id) {
        DoctorSchedule schedule = findScheduleById(id);
        return mapper.toResponseDTO(schedule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorScheduleResponseDTO> getAllSchedules(boolean includeInactive) {
        List<DoctorSchedule> schedules;
        Integer clinicId = TenantContext.getCurrentTenant();

        if (includeInactive) {
            // Use Specification to filter by clinicId since we don't have a simple findAllByClinicId method
            Specification<DoctorSchedule> spec = (root, query, cb) -> cb.equal(root.get("clinicId"), clinicId);
            schedules = doctorScheduleRepo.findAll(spec);
        }
        else {
            schedules = doctorScheduleRepo.findAllActive(clinicId);
        }
        return schedules.stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public Page<DoctorScheduleResponseDTO> getSchedulesByDoctorId(Long doctorId, boolean includeInactive, Pageable pageable) {
        // Ensure doctor exists
        findDoctorById(doctorId);

        Integer clinicId = TenantContext.getCurrentTenant();
        Page<DoctorSchedule> schedules;

        if (includeInactive) {
            schedules = doctorScheduleRepo.findByDoctorIdAndClinicId(doctorId, clinicId, pageable);
        } else {
            schedules = doctorScheduleRepo.findActiveByDoctorId(doctorId, clinicId, pageable);
        }

        // Convert Page<Entity> to Page<DTO> efficiently
        return schedules.map(mapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorScheduleResponseDTO> getUpcomingSchedulesByDoctorId(Long doctorId) {

        findDoctorById(doctorId);

        LocalTime now = LocalTime.now().truncatedTo(ChronoUnit.SECONDS);

        List<DoctorSchedule> schedules = doctorScheduleRepo.findUpcomingSchedulesByDoctorId(
                doctorId,
                LocalDate.now(),
                now,
                TenantContext.getCurrentTenant() // <--- Clinic ID
        );

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

        System.out.println("Date : " + today);
        System.out.println("Time : " + now);

        Page<DoctorSchedule> schedulePage = doctorScheduleRepo.findUpcomingSchedules(
                today,
                now,
                doctorName,
                specialization,
                searchDate,
                TenantContext.getCurrentTenant(),
                pageable
        );

        return schedulePage.map(mapper::toResponseDTO);
    }

    @Override
    @Transactional
    public DoctorScheduleResponseDTO updateSchedule(Long id, DoctorScheduleUpdateDTO updateDTO) {
        // 1. Find existing schedule (Multi-tenant safe)
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

        // 3. Multi-Tenant Overlap Check for Update
        boolean exists = doctorScheduleRepo.existsOverlappingScheduleForUpdate(
                existingSchedule.getDoctor().getId(),
                existingSchedule.getScheduleDate(),
                updateDTO.getStartTime(),
                updateDTO.getEndTime(),
                id,
                TenantContext.getCurrentTenant() // <--- Clinic ID
        );

        if (exists) {
            throw new ValidationException(
                    messageSourceUtil.getMessage(
                            "schedule.error.duplicate.range.update",
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

        if (schedule.getStatus() == DoctorSchedule.ScheduleStatus.CANCELLED) {
            throw new ValidationException(
                    "Schedule is already cancelled",
                    "ALREADY_CANCELLED",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (schedule.getStatus() == DoctorSchedule.ScheduleStatus.COMPLETED) {
            throw new ValidationException(
                    "Cannot deactivate a completed schedule",
                    "COMPLETED_SCHEDULE",
                    HttpStatus.BAD_REQUEST
            );
        }

        schedule.setStatus(DoctorSchedule.ScheduleStatus.CANCELLED);
        DoctorSchedule savedSchedule = doctorScheduleRepo.save(schedule);
        return mapper.toResponseDTO(savedSchedule);
    }

    @Override
    public DoctorScheduleResponseDTO reactivateSchedule(Long id) {
        DoctorSchedule schedule = findScheduleById(id);

        if (schedule.getStatus() == DoctorSchedule.ScheduleStatus.ACTIVE) {
            throw new ValidationException(
                    "Schedule is already active",
                    "ALREADY_ACTIVE",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (schedule.getScheduleDate().isBefore(LocalDate.now())) {
            throw new ValidationException(
                    "Cannot reactivate a schedule in the past",
                    "PAST_DATE",
                    HttpStatus.BAD_REQUEST
            );
        }

        schedule.setStatus(DoctorSchedule.ScheduleStatus.ACTIVE);
        DoctorSchedule savedSchedule = doctorScheduleRepo.save(schedule);
        return mapper.toResponseDTO(savedSchedule);
    }

    @Override
    public void hardDeleteSchedule(Long id) {
        DoctorSchedule schedule = findScheduleById(id);
        // Add additional logic check here if appointments exist...
        doctorScheduleRepo.delete(schedule);
    }

    // --- Private Helper Methods ---

    /**
     * Finds a schedule by ID and ensures it belongs to the current tenant.
     */
    private DoctorSchedule findScheduleById(Long id) {
        DoctorSchedule schedule = doctorScheduleRepo.findById(id)
                .orElseThrow(() -> new ValidationException("Schedule not found with id: " + id, "DOCTOR_SCHEDULE_NOT_FOUND"));

        // Tenant Security Check
        if (!Objects.equals(schedule.getClinicId(), TenantContext.getCurrentTenant())) {
            throw new ValidationException("Schedule not found", "DOCTOR_SCHEDULE_NOT_FOUND", HttpStatus.NOT_FOUND);
        }
        return schedule;
    }

    /**
     * Finds a doctor by ID and ensures they belong to the current tenant.
     */
    private Doctor findDoctorById(Long doctorId) {
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new ValidationException("Doctor not found with id: " + doctorId, "DOCTOR_NOT_FOUND"));

        // Tenant Security Check
        if (!Objects.equals(doctor.getClinicId(), TenantContext.getCurrentTenant())) {
            throw new ValidationException("Doctor not found", "DOCTOR_NOT_FOUND", HttpStatus.NOT_FOUND);
        }
        return doctor;
    }

    private void validateCreateDTO(DoctorScheduleCreateDTO schedule) {
        if (schedule.getDoctorId() == null) {
            throw new ValidationException("Doctor is required for schedule", "DOCTOR_REQUIRED", HttpStatus.BAD_REQUEST);
        }
        if (schedule.getScheduleDate() == null) {
            throw new ValidationException("Schedule date is required", "DATE_REQUIRED", HttpStatus.BAD_REQUEST);
        }
        if (schedule.getStartTime() == null) {
            throw new ValidationException("Start time is required", "START_TIME_REQUIRED", HttpStatus.BAD_REQUEST);
        }
        if (schedule.getEndTime() == null) {
            throw new ValidationException("End time is required", "END_TIME_REQUIRED", HttpStatus.BAD_REQUEST);
        }
        if (schedule.getStartTime().isAfter(schedule.getEndTime()) || schedule.getStartTime().equals(schedule.getEndTime())) {
            throw new ValidationException("End time must be after start time", "INVALID_TIME_RANGE", HttpStatus.BAD_REQUEST);
        }
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