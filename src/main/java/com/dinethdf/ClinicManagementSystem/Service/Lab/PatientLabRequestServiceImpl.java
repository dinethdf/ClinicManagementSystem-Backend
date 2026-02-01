package com.dinethdf.ClinicManagementSystem.Service.Lab;

import com.dinethdf.ClinicManagementSystem.DTO.Appointment.AppointmentFullDetailsDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Lab.LabRequestCreateDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Lab.LabRequestResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Lab.LabRequestUpdateDTO;
import com.dinethdf.ClinicManagementSystem.Model.Appointment.Appointment;
import com.dinethdf.ClinicManagementSystem.Model.Doctor.Doctor;
import com.dinethdf.ClinicManagementSystem.Model.Lab.LabTestType;
import com.dinethdf.ClinicManagementSystem.Model.Lab.PatientLabRequest;
import com.dinethdf.ClinicManagementSystem.Model.Lab.PatientLabRequest.LabStatus;
import com.dinethdf.ClinicManagementSystem.Model.Patient.Patient;
import com.dinethdf.ClinicManagementSystem.Repo.Appointment.AppointmentRepo;
import com.dinethdf.ClinicManagementSystem.Repo.Doctor.DoctorRepo;
import com.dinethdf.ClinicManagementSystem.Repo.Lab.LabTestTypeRepo;
import com.dinethdf.ClinicManagementSystem.Repo.Lab.PatientLabRequestRepo;
import com.dinethdf.ClinicManagementSystem.Repo.Patient.PatientDetailsRepo;
import com.dinethdf.ClinicManagementSystem.Service.Appointment.AppointmentService;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PatientLabRequestServiceImpl implements PatientLabRequestService {

    private final PatientLabRequestRepo requestRepo;
    private final LabTestTypeRepo testTypeRepo;
    private final PatientDetailsRepo patientDetailsRepo; // Renamed to match your context
    private final AppointmentRepo appointmentRepo;
    private final DoctorRepo doctorRepo;
    private final AppointmentService appointmentService;
    private final MessageSourceUtil messageSourceUtil;

    @Override
    @Transactional
    public LabRequestResponseDTO createLabRequest(LabRequestCreateDTO createDTO) {

        if (createDTO.getPatientId() == null) {
            throw new ValidationException("Patient ID is required", "PATIENT_REQUIRED", HttpStatus.BAD_REQUEST);
        }
        if (createDTO.getLabTestTypeId() == null) {
            throw new ValidationException("Lab Test Type ID is required", "TEST_TYPE_REQUIRED", HttpStatus.BAD_REQUEST);
        }

        Integer clinicId = TenantContext.getCurrentTenant();

        Patient patient = patientDetailsRepo.findById(Long.valueOf(createDTO.getPatientId()))
                .orElseThrow(() -> new ValidationException(
                        messageSourceUtil.getMessage("patient.not.found"),
                        "PATIENT_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));

        if (!Objects.equals(patient.getClinicId(), clinicId)) {
            throw new ValidationException(
                    messageSourceUtil.getMessage("patient.not.found"),
                    "PATIENT_NOT_FOUND",
                    HttpStatus.NOT_FOUND
            );
        }

        LabTestType testType = testTypeRepo.findById(createDTO.getLabTestTypeId())
                .orElseThrow(() -> new ValidationException("Lab Test not found", "TEST_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (!Objects.equals(testType.getClinicId(), clinicId)) {
            throw new ValidationException("Lab Test not found", "TEST_NOT_FOUND", HttpStatus.NOT_FOUND);
        }
        if (!Boolean.TRUE.equals(testType.getIsActive())) {
            throw new ValidationException("This Lab Test is currently inactive", "TEST_INACTIVE", HttpStatus.BAD_REQUEST);
        }

        // ... inside createLabRequest method ...

        // D. Appointment Validation Logic (STRICT: Doctor Only)
        Appointment appointment = null;

        if (createDTO.getAppointmentId() != null) {

            // 1. STRICT CHECK: Only a DOCTOR can link a request to an appointment
            // Admin, Staff, or Patient are NOT allowed to do this.
            if (!isCurrentUserDoctor()) {
                throw new ValidationException(
                        "Only doctors can prescribe tests linked to an appointment. Staff/Admins must create walk-in requests.",
                        "ACCESS_DENIED",
                        HttpStatus.FORBIDDEN
                );
            }

            // 2. Fetch Details using efficient DTO Service
            AppointmentFullDetailsDTO apptDetails = appointmentService.getAppointmentDetails(Long.valueOf(createDTO.getAppointmentId()));

            // 3. Validate Patient Match
            if (!apptDetails.getPatientId().equals(Long.valueOf(createDTO.getPatientId()))) {
                throw new ValidationException(
                        "The selected appointment does not belong to this patient",
                        "APPOINTMENT_MISMATCH",
                        HttpStatus.CONFLICT
                );
            }

            // 4. Validate Doctor Ownership
            // We already know it's a doctor (Step 1), now we check IF it is the CORRECT doctor.
            String email = SecurityContextHolder.getContext().getAuthentication().getName();

            Doctor currentDoctor = doctorRepo.findByUserAuthEmailAndClinicId(email, clinicId)
                    .orElseThrow(() -> new ValidationException(
                            messageSourceUtil.getMessage("doctor.not.found.for.user"),
                            "DOCTOR_PROFILE_NOT_FOUND",
                            null,
                            HttpStatus.NOT_FOUND
                    ));

            // CRITICAL: Ensure the logged-in doctor owns this specific appointment
            if (!apptDetails.getDoctorId().equals(currentDoctor.getId())) {
                throw new ValidationException(
                        "You can only prescribe tests for your own appointments",
                        "ACCESS_DENIED",
                        HttpStatus.FORBIDDEN
                );
            }

            // 5. Get Entity Reference
            appointment = appointmentRepo.getReferenceById(Long.valueOf(createDTO.getAppointmentId()));
        }


        // --- 5. Build & Save ---
        PatientLabRequest request = new PatientLabRequest();
        request.setClinicId(clinicId);
        request.setPatient(patient);
        request.setLabTestType(testType);
        request.setAppointment(appointment);

        // Snapshot Price
        BigDecimal totalAmount = testType.getTestFee().add(testType.getClinicFee());
        request.setBilledAmount(totalAmount);

        request.setStatus(PatientLabRequest.LabStatus.PENDING);
        request.setPaymentStatus(PatientLabRequest.PaymentStatus.PENDING);

        PatientLabRequest savedRequest = requestRepo.save(request);
        return mapToDTO(savedRequest);
    }

    // --- Standard Read/Update Methods ---

    @Override
    public Page<LabRequestResponseDTO> getAllLabRequests(String search, LabStatus status, Pageable pageable) {
        Integer clinicId = TenantContext.getCurrentTenant();
        return requestRepo.searchRequests(clinicId, search, status, pageable)
                .map(this::mapToDTO);
    }

    @Override
    public Page<LabRequestResponseDTO> getAllLabRequests(String search, Pageable pageable) {
        return getAllLabRequests(search, null, pageable);
    }

    @Override
    public Page<LabRequestResponseDTO> getLabRequestsByPatient(Integer patientId, Pageable pageable) {
        Integer clinicId = TenantContext.getCurrentTenant();
        return requestRepo.findByPatientIdAndClinicId(patientId, clinicId, pageable)
                .map(this::mapToDTO);
    }

    @Override
    public LabRequestResponseDTO getLabRequestById(Integer id) {
        return mapToDTO(findRequestById(id));
    }

    @Override
    @Transactional
    public LabRequestResponseDTO updateLabRequest(Integer id, LabRequestUpdateDTO updateDTO) {
        PatientLabRequest request = findRequestById(id);

        if (updateDTO.getStatus() != null) {
            request.setStatus(updateDTO.getStatus());
            if (updateDTO.getStatus() == PatientLabRequest.LabStatus.COMPLETED) {
                request.setCompletedDate(LocalDateTime.now());
            }
        }
        if (updateDTO.getTechnicianNotes() != null) {
            request.setTechnicianNotes(updateDTO.getTechnicianNotes());
        }
        if (updateDTO.getReportUrl() != null) {
            request.setReportUrl(updateDTO.getReportUrl());
        }

        return mapToDTO(requestRepo.save(request));
    }

    // --- Helpers ---

    private boolean isCurrentUserDoctor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("DOCTOR"));
    }

    private PatientLabRequest findRequestById(Integer id) {
        return requestRepo.findById(id)
                .filter(r -> Objects.equals(r.getClinicId(), TenantContext.getCurrentTenant()))
                .orElseThrow(() -> new ValidationException("Lab Request not found", "REQUEST_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    private LabRequestResponseDTO mapToDTO(PatientLabRequest entity) {
        LabRequestResponseDTO dto = new LabRequestResponseDTO();
        dto.setId(entity.getId());
        dto.setPatientId(Math.toIntExact(entity.getPatient().getId()));
        dto.setPatientName(entity.getPatient().getFirstName() + " " + entity.getPatient().getLastName());
        dto.setPatientMobile(entity.getPatient().getMobileNo());

        dto.setLabTestTypeId(entity.getLabTestType().getId());
        dto.setTestName(entity.getLabTestType().getName());
        dto.setTestCode(entity.getLabTestType().getCode());

        if (entity.getAppointment() != null) {
            dto.setAppointmentId(Math.toIntExact(entity.getAppointment().getId()));
            // Null safety for doctor schedule access
            if(entity.getAppointment().getDoctorSchedule() != null && entity.getAppointment().getDoctorSchedule().getDoctor() != null) {
                String doctorName = entity.getAppointment().getDoctorSchedule().getDoctor().getFirstName() + " " +
                        entity.getAppointment().getDoctorSchedule().getDoctor().getLastName();
                dto.setPrescribedBy("Dr. " + doctorName);
            } else {
                dto.setPrescribedBy("Doctor (Unknown)");
            }
        } else {
            dto.setAppointmentId(null);
            dto.setPrescribedBy("Walk-in / Self Request");
        }

        if (entity.getInvoice() != null) {
            dto.setInvoiceId(Math.toIntExact(entity.getInvoice().getId()));
        }

        dto.setStatus(entity.getStatus());
        dto.setPaymentStatus(entity.getPaymentStatus());
        dto.setBilledAmount(entity.getBilledAmount());

        dto.setTechnicianNotes(entity.getTechnicianNotes());
        dto.setReportUrl(entity.getReportUrl());

        dto.setCreatedDate(entity.getCreatedDate());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCompletedDate(entity.getCompletedDate());

        return dto;
    }

    @Override
    @Transactional
    public void deleteLabRequest(Integer id) {
        Integer clinicId = TenantContext.getCurrentTenant();

        // 1. Fetch Request
        PatientLabRequest request = requestRepo.findById(id)
                .filter(r -> Objects.equals(r.getClinicId(), clinicId))
                .orElseThrow(() -> new ValidationException(
                        "Lab Request not found",
                        "REQUEST_NOT_FOUND",
                        HttpStatus.NOT_FOUND
                ));

        // 2. Safety Check: Status must be PENDING
        if (request.getStatus() != PatientLabRequest.LabStatus.PENDING) {
            throw new ValidationException(
                    "Cannot delete this request because it is " + request.getStatus() + ". Only PENDING requests can be deleted.",
                    "DELETE_NOT_ALLOWED",
                    HttpStatus.BAD_REQUEST
            );
        }

        // 3. Security Check: Doctor Ownership
        // If the user is a DOCTOR, they can ONLY delete requests linked to THEIR OWN appointments.
        if (isCurrentUserDoctor()) {

            // A. Check if the request is even linked to an appointment
            if (request.getAppointment() == null) {
                // If it's a walk-in request (no appointment), a Doctor generally shouldn't be deleting it
                // (unless your business logic allows doctors to manage walk-ins).
                // Assuming strict rules:
                throw new ValidationException(
                        "Doctors can only delete requests linked to their own appointments.",
                        "ACCESS_DENIED",
                        HttpStatus.FORBIDDEN
                );
            }

            // B. Get the Current Logged-in Doctor
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Doctor currentDoctor = doctorRepo.findByUserAuthEmailAndClinicId(email, clinicId)
                    .orElseThrow(() -> new ValidationException(
                            messageSourceUtil.getMessage("doctor.not.found.for.user"),
                            "DOCTOR_PROFILE_NOT_FOUND",
                            HttpStatus.NOT_FOUND
                    ));

            // C. Compare IDs
            // Request -> Appointment -> DoctorSchedule -> Doctor -> ID
            Long appointmentDoctorId = request.getAppointment().getDoctorSchedule().getDoctor().getId();

            if (!appointmentDoctorId.equals(currentDoctor.getId())) {
                throw new ValidationException(
                        "You do not have permission to delete this request. It belongs to another doctor's appointment.",
                        "ACCESS_DENIED",
                        HttpStatus.FORBIDDEN
                );
            }
        }

        // 4. Delete
        requestRepo.delete(request);
    }
}