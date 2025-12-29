package com.dinethdf.ClinicManagementSystem.Service.Lab;

import com.dinethdf.ClinicManagementSystem.DTO.Lab.LabRequestCreateDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Lab.LabRequestResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Lab.LabRequestUpdateDTO;
import com.dinethdf.ClinicManagementSystem.Model.Appointment.Appointment;
import com.dinethdf.ClinicManagementSystem.Model.Lab.LabTestType;
import com.dinethdf.ClinicManagementSystem.Model.Lab.PatientLabRequest;
import com.dinethdf.ClinicManagementSystem.Model.Lab.PatientLabRequest.LabStatus;
import com.dinethdf.ClinicManagementSystem.Model.Patient.Patient;
import com.dinethdf.ClinicManagementSystem.Repo.Appointment.AppointmentRepo;
import com.dinethdf.ClinicManagementSystem.Repo.Lab.LabTestTypeRepo; // <--- ADDED THIS IMPORT
import com.dinethdf.ClinicManagementSystem.Repo.Lab.PatientLabRequestRepo;
import com.dinethdf.ClinicManagementSystem.Repo.Patient.PatientDetailsRepo;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
    private final PatientDetailsRepo patientRepo;
    private final AppointmentRepo appointmentRepo;
    private final MessageSourceUtil messageSourceUtil;

    @Override
    @Transactional
    public LabRequestResponseDTO createLabRequest(LabRequestCreateDTO createDTO) {

        // --- 1. Manual Validation ---
        if (createDTO.getPatientId() == null) {
            throw new ValidationException("Patient ID is required", "PATIENT_REQUIRED", HttpStatus.BAD_REQUEST);
        }
        if (createDTO.getLabTestTypeId() == null) {
            throw new ValidationException("Lab Test Type ID is required", "TEST_TYPE_REQUIRED", HttpStatus.BAD_REQUEST);
        }

        Integer clinicId = TenantContext.getCurrentTenant();

        // --- 2. Fetch & Validate Entities ---

        // A. Validate Patient
        Patient patient = patientRepo.findById(Long.valueOf(createDTO.getPatientId()))
                .filter(p -> Objects.equals(p.getClinicId(), clinicId))
                .orElseThrow(() -> new ValidationException("Patient not found", "PATIENT_NOT_FOUND", HttpStatus.NOT_FOUND));

        // B. Validate Test Type (Must be Active)
        // This will now work because testTypeRepo is of type LabTestTypeRepo
        LabTestType testType = testTypeRepo.findById(createDTO.getLabTestTypeId())
                .filter(t -> Objects.equals(t.getClinicId(), clinicId))
                .orElseThrow(() -> new ValidationException("Lab Test not found", "TEST_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (!Boolean.TRUE.equals(testType.getIsActive())) {
            throw new ValidationException("This Lab Test is currently inactive", "TEST_INACTIVE", HttpStatus.BAD_REQUEST);
        }

        // C. Validate Appointment (If provided)
        Appointment appointment = null;
        if (createDTO.getAppointmentId() != null) {
            appointment = appointmentRepo.findById(Long.valueOf(createDTO.getAppointmentId()))
                    .filter(a -> Objects.equals(a.getClinicId(), clinicId))
                    .orElseThrow(() -> new ValidationException("Appointment not found", "APPOINTMENT_NOT_FOUND", HttpStatus.NOT_FOUND));

            // Optional: Ensure appointment belongs to this patient
            if (!Objects.equals(appointment.getPatient().getId(), patient.getId())) {
                throw new ValidationException("Appointment does not belong to this patient", "INVALID_APPOINTMENT", HttpStatus.CONFLICT);
            }
        }

        // --- 3. Build Entity ---
        PatientLabRequest request = new PatientLabRequest();
        request.setClinicId(clinicId);
        request.setPatient(patient);
        request.setLabTestType(testType);
        request.setAppointment(appointment); // Null if walk-in

        // Set Financial Snapshot (Locking the price at time of booking)
        BigDecimal totalAmount = testType.getTestFee().add(testType.getClinicFee());
        request.setBilledAmount(totalAmount);

        // Default Statuses
        request.setStatus(PatientLabRequest.LabStatus.PENDING);
        request.setPaymentStatus(PatientLabRequest.PaymentStatus.PENDING);

        // CreatedBy handled by AuditWrapper

        // --- 4. Save & Return ---
        PatientLabRequest savedRequest = requestRepo.save(request);
        return mapToDTO(savedRequest);
    }


    @Override
    public Page<LabRequestResponseDTO> getAllLabRequests(String search, LabStatus status, Pageable pageable) {
        Integer clinicId = TenantContext.getCurrentTenant();

        // Use the custom search query from the Repo
        return requestRepo.searchRequests(clinicId, search, status, pageable)
                .map(this::mapToDTO);
    }

    // Overload in case you haven't updated the interface yet (Optional fallback)
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

        // --- Update Logic ---

        // 1. Status Update
        if (updateDTO.getStatus() != null) {
            request.setStatus(updateDTO.getStatus());

            // Auto-set completed date if status is COMPLETED
            if (updateDTO.getStatus() == PatientLabRequest.LabStatus.COMPLETED) {
                request.setCompletedDate(LocalDateTime.now());
            }
        }

        // 2. Results Entry
        if (updateDTO.getTechnicianNotes() != null) {
            request.setTechnicianNotes(updateDTO.getTechnicianNotes());
        }
        if (updateDTO.getReportUrl() != null) {
            request.setReportUrl(updateDTO.getReportUrl());
        }

        PatientLabRequest updated = requestRepo.save(request);
        return mapToDTO(updated);
    }

    // --- Helper Methods ---

    private PatientLabRequest findRequestById(Integer id) {
        return requestRepo.findById(id)
                .filter(r -> Objects.equals(r.getClinicId(), TenantContext.getCurrentTenant()))
                .orElseThrow(() -> new ValidationException("Lab Request not found", "REQUEST_NOT_FOUND", HttpStatus.NOT_FOUND));
    }

    private LabRequestResponseDTO mapToDTO(PatientLabRequest entity) {
        LabRequestResponseDTO dto = new LabRequestResponseDTO();
        dto.setId(entity.getId());

        // Patient Info
        dto.setPatientId(Math.toIntExact(entity.getPatient().getId()));
        dto.setPatientName(entity.getPatient().getFirstName() + " " + entity.getPatient().getLastName());
        dto.setPatientMobile(entity.getPatient().getMobileNo());

        // Test Info
        dto.setLabTestTypeId(entity.getLabTestType().getId());
        dto.setTestName(entity.getLabTestType().getName());
        dto.setTestCode(entity.getLabTestType().getCode());

        // Doctor / Prescriber Logic
        if (entity.getAppointment() != null) {
            dto.setAppointmentId(Math.toIntExact(entity.getAppointment().getId()));
            // Fetch Doctor Name from Appointment -> DoctorSchedule -> Doctor
            String doctorName = entity.getAppointment().getDoctorSchedule().getDoctor().getFirstName() + " " +
                    entity.getAppointment().getDoctorSchedule().getDoctor().getLastName();
            dto.setPrescribedBy("Dr. " + doctorName);
        } else {
            dto.setAppointmentId(null);
            dto.setPrescribedBy("Walk-in / Self Request");
        }

        // Invoice Link
        if (entity.getInvoice() != null) {
            dto.setInvoiceId(Math.toIntExact(entity.getInvoice().getId()));
        }

        // Status & Financials
        dto.setStatus(entity.getStatus());
        dto.setPaymentStatus(entity.getPaymentStatus());
        dto.setBilledAmount(entity.getBilledAmount());

        // Results
        dto.setTechnicianNotes(entity.getTechnicianNotes());
        dto.setReportUrl(entity.getReportUrl());

        // Audit
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCompletedDate(entity.getCompletedDate());

        return dto;
    }
}