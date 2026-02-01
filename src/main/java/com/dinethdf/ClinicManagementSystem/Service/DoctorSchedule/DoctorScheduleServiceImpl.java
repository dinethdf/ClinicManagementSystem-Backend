package com.dinethdf.ClinicManagementSystem.Service.DoctorSchedule;

import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleCreateDTO;
import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.DoctorShedule.DoctorScheduleUpdateDTO;
import com.dinethdf.ClinicManagementSystem.Model.Doctor.Doctor;
import com.dinethdf.ClinicManagementSystem.Model.DoctorShedule.DoctorSchedule;
import com.dinethdf.ClinicManagementSystem.Model.DoctorShedule.PayoutStatusLookup;
import com.dinethdf.ClinicManagementSystem.Model.DoctorShedule.ScheduleStatusLookup;
import com.dinethdf.ClinicManagementSystem.Repo.Doctor.DoctorRepo;
import com.dinethdf.ClinicManagementSystem.Repo.DoctorSchedule.DoctorScheduleRepo;
import com.dinethdf.ClinicManagementSystem.Repo.DoctorSchedule.PayoutStatusLookupRepo; // Needs to be created
import com.dinethdf.ClinicManagementSystem.Repo.DoctorSchedule.ScheduleStatusLookupRepo; // Needs to be created
import com.dinethdf.ClinicManagementSystem.Service.Appointment.QueueService;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
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

    // --- NEW: Inject Lookup Repositories ---
    private final ScheduleStatusLookupRepo scheduleStatusRepo;
    private final PayoutStatusLookupRepo payoutStatusRepo;

    // 1. Inject the QueueService
    private final QueueService queueService;

    @Override
    @Transactional(readOnly = true)
    public boolean validateScheduleOwnership(Long doctorId, Long scheduleId) {
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

// 1. Check for overlaps
        List<DoctorSchedule> overlappingSchedules = doctorScheduleRepo.findOverlappingSchedules(
                createDTO.getDoctorId(),
                createDTO.getScheduleDate(),
                createDTO.getStartTime(),
                createDTO.getEndTime(),
                TenantContext.getCurrentTenant()
        );

// 2. If overlap exists, grab the details and throw error
        if (!overlappingSchedules.isEmpty()) {
            DoctorSchedule existing = overlappingSchedules.get(0);

            throw new ValidationException(
                    String.format("Doctor already has a schedule on %s from %s to %s. You are trying to book %s - %s.",
                            existing.getScheduleDate(),
                            existing.getStartTime(),
                            existing.getEndTime(),
                            createDTO.getStartTime(),
                            createDTO.getEndTime()
                    ),
                    "DUPLICATE_SCHEDULE_RANGE",
                    HttpStatus.CONFLICT
            );
        }

        // --- NEW: Fetch Default Statuses ---
        ScheduleStatusLookup activeStatus = scheduleStatusRepo.findByCode("ACTIVE")
                .orElseThrow(() -> new ValidationException("System Error: Status 'ACTIVE' not found", "CONFIG_ERROR"));

        PayoutStatusLookup notPaidStatus = payoutStatusRepo.findByCode("NOT_PAID")
                .orElseThrow(() -> new ValidationException("System Error: Payout Status 'NOT_PAID' not found", "CONFIG_ERROR"));


        DoctorSchedule newSchedule = DoctorSchedule.builder()
                .clinicId(TenantContext.getCurrentTenant())
                .doctor(doctor)
                .scheduleDate(createDTO.getScheduleDate())
                .startTime(createDTO.getStartTime())
                .endTime(createDTO.getEndTime())
                .doctorFee(createDTO.getDoctorFee())
                .clinicFee(createDTO.getClinicFee())
                .maxPatientCount(createDTO.getMaxPatientCount())
                .registeredPatientCount(0)
                .lastQueueNumber(0)  // --- NEW: Initialize Queue Number
                .status(activeStatus) // --- NEW: Set Entity
                .isBookingOpen(true)  // --- NEW: Set Boolean
                .payoutStatus(notPaidStatus) // --- NEW: Set Entity
                .isActive(true)
                .isApproved(false)
                .build();

        DoctorSchedule savedSchedule = doctorScheduleRepo.save(newSchedule);

        // 3. Initialize Firebase Queue (Async / Fire-and-Forget)
        try {
            String fullDoctorName = savedSchedule.getDoctor().getFirstName() + " " + savedSchedule.getDoctor().getLastName();
            String timeRange = savedSchedule.getStartTime() + " - " + savedSchedule.getEndTime();
            String specializationName = String.valueOf(savedSchedule.getDoctor().getSpecialization().getName());

            queueService.initializeSchedule(
                    Long.valueOf(savedSchedule.getClinicId()),
                    "Cure Guard",
                    "045*******",
                    savedSchedule.getId(),
                    fullDoctorName,
                    specializationName,
                    savedSchedule.getScheduleDate().toString(),
                    timeRange
            );

            System.out.println("Firebase Schedule Initialized for ID: {}" + savedSchedule.getId());

        } catch (Exception e) {
            System.out.println("Failed to initialize Firebase Schedule: {}" + e.getMessage());
        }

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
                TenantContext.getCurrentTenant(),
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
        findDoctorById(doctorId);

        Integer clinicId = TenantContext.getCurrentTenant();
        Page<DoctorSchedule> schedules;

        if (includeInactive) {
            schedules = doctorScheduleRepo.findByDoctorIdAndClinicId(doctorId, clinicId, pageable);
        } else {
            schedules = doctorScheduleRepo.findActiveByDoctorId(doctorId, clinicId, pageable);
        }

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
                TenantContext.getCurrentTenant()
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
        DoctorSchedule existingSchedule = findScheduleById(id);

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

        List<DoctorSchedule> overlappingSchedules = doctorScheduleRepo.findOverlappingSchedulesForUpdate(
                existingSchedule.getDoctor().getId(),
                existingSchedule.getScheduleDate(),
                updateDTO.getStartTime(),
                updateDTO.getEndTime(),
                id,
                TenantContext.getCurrentTenant()
        );

        if (!overlappingSchedules.isEmpty()) {
            DoctorSchedule conflict = overlappingSchedules.get(0);

            throw new ValidationException(
                    String.format("Cannot update. This overlaps with an existing schedule: %s to %s.",
                            conflict.getStartTime(),
                            conflict.getEndTime()
                    ),
                    "DUPLICATE_SCHEDULE_RANGE",
                    HttpStatus.CONFLICT
            );
        }

        existingSchedule.setStartTime(updateDTO.getStartTime());
        existingSchedule.setEndTime(updateDTO.getEndTime());
        existingSchedule.setDoctorFee(updateDTO.getDoctorFee());
        existingSchedule.setClinicFee(updateDTO.getClinicFee());
        existingSchedule.setMaxPatientCount(updateDTO.getMaxPatientCount());

        DoctorSchedule updatedSchedule = doctorScheduleRepo.save(existingSchedule);
        return mapper.toResponseDTO(updatedSchedule);
    }

    @Override
    public DoctorScheduleResponseDTO deactivateSchedule(Long id) {
        DoctorSchedule schedule = findScheduleById(id);

        // --- NEW: Check String Code instead of Enum ---
        if ("CANCELLED".equals(schedule.getStatus().getCode())) {
            throw new ValidationException(
                    "Schedule is already cancelled",
                    "ALREADY_CANCELLED",
                    HttpStatus.BAD_REQUEST
            );
        }

        if ("COMPLETED".equals(schedule.getStatus().getCode())) {
            throw new ValidationException(
                    "Cannot deactivate a completed schedule",
                    "COMPLETED_SCHEDULE",
                    HttpStatus.BAD_REQUEST
            );
        }

        // --- NEW: Fetch and Set 'CANCELLED' Entity ---
        ScheduleStatusLookup cancelledStatus = scheduleStatusRepo.findByCode("CANCELLED")
                .orElseThrow(() -> new ValidationException("Status 'CANCELLED' not found", "CONFIG_ERROR"));

        schedule.setStatus(cancelledStatus);
        DoctorSchedule savedSchedule = doctorScheduleRepo.save(schedule);
        return mapper.toResponseDTO(savedSchedule);
    }

    @Override
    public DoctorScheduleResponseDTO reactivateSchedule(Long id) {
        DoctorSchedule schedule = findScheduleById(id);

        // --- NEW: Check String Code instead of Enum ---
        if ("ACTIVE".equals(schedule.getStatus().getCode())) {
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

        // --- NEW: Fetch and Set 'ACTIVE' Entity ---
        ScheduleStatusLookup activeStatus = scheduleStatusRepo.findByCode("ACTIVE")
                .orElseThrow(() -> new ValidationException("Status 'ACTIVE' not found", "CONFIG_ERROR"));

        schedule.setStatus(activeStatus);
        DoctorSchedule savedSchedule = doctorScheduleRepo.save(schedule);
        return mapper.toResponseDTO(savedSchedule);
    }

    @Override
    public void hardDeleteSchedule(Long id) {
        DoctorSchedule schedule = findScheduleById(id);
        doctorScheduleRepo.delete(schedule);
    }

    // --- Private Helper Methods ---

    private DoctorSchedule findScheduleById(Long id) {
        DoctorSchedule schedule = doctorScheduleRepo.findById(id)
                .orElseThrow(() -> new ValidationException("Schedule not found with id: " + id, "DOCTOR_SCHEDULE_NOT_FOUND"));

        if (!Objects.equals(schedule.getClinicId(), TenantContext.getCurrentTenant())) {
            throw new ValidationException("Schedule not found", "DOCTOR_SCHEDULE_NOT_FOUND", HttpStatus.NOT_FOUND);
        }
        return schedule;
    }

    private Doctor findDoctorById(Long doctorId) {
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new ValidationException("Doctor not found with id: " + doctorId, "DOCTOR_NOT_FOUND"));

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