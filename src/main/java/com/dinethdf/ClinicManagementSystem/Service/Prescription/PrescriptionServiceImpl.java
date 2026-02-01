package com.dinethdf.ClinicManagementSystem.Service.Prescription;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Prescription.PrescriptionItemDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Prescription.PrescriptionRequestDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Prescription.PrescriptionResponseDTO;
import com.dinethdf.ClinicManagementSystem.Model.Appointment.Appointment;
import com.dinethdf.ClinicManagementSystem.Model.Appointment.AppointmentStatusLookup;
import com.dinethdf.ClinicManagementSystem.Model.Prescription.Prescription;
import com.dinethdf.ClinicManagementSystem.Model.Prescription.PrescriptionItem;
import com.dinethdf.ClinicManagementSystem.Model.Prescription.PrescriptionStatus;
import com.dinethdf.ClinicManagementSystem.Repo.Appointment.AppointmentRepo;
import com.dinethdf.ClinicManagementSystem.Repo.Appointment.AppointmentStatusLookupRepo;
import com.dinethdf.ClinicManagementSystem.Repo.Prescription.PrescriptionRepo;
import com.dinethdf.ClinicManagementSystem.config.TenantContext; // <--- Import Context
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepo prescriptionRepo;
    private final AppointmentRepo appointmentRepo;
    private final MessageSourceUtil messageSourceUtil;
    private final AppointmentStatusLookupRepo statusLookupRepo;

    @Override
    public ApiResponse<PrescriptionResponseDTO> createPrescription(PrescriptionRequestDTO request, String createdBy) {

        Integer clinicId = TenantContext.getCurrentTenant();

        // 1. Fetch Appointment & Verify Tenant Access
        Appointment appointment = appointmentRepo.findById(request.getAppointmentId())
                .orElseThrow(() -> new ValidationException("Appointment not found", "APPOINTMENT_NOT_FOUND", HttpStatus.NOT_FOUND));

        // SECURITY CHECK: Appointment must belong to current clinic
        // (Assuming Appointment entity has getClinicId())
        /* NOTE: If your Appointment Entity uses 'clinic_id' column but doesn't expose getClinicId(),
           you might need to update Appointment Entity first.
           But usually, Multi-tenant setups should allow this check.
        */
         if (!Objects.equals(appointment.getClinicId(), clinicId)) {
            throw new ValidationException("Appointment not found", "APPOINTMENT_NOT_FOUND", HttpStatus.NOT_FOUND);
         }

        // 2. Check Duplicate (Scoped to Clinic)
        if (prescriptionRepo.findByAppointmentIdAndClinicId(request.getAppointmentId(), clinicId).isPresent()) {
            throw new ValidationException("Prescription already exists for this appointment", "DUPLICATE_PRESCRIPTION", HttpStatus.CONFLICT);
        }

        // 3. Create Prescription
        Prescription prescription = new Prescription();
        prescription.setClinicId(clinicId); // <--- Set Clinic ID
        prescription.setAppointment(appointment);
        prescription.setNote(request.getNote());
        prescription.setStatus(PrescriptionStatus.ISSUED);
//        prescription.setIsApproved(false);

        prescription.setCreatedDate(LocalDateTime.now());
        prescription.setCreatedBy(createdBy);

        // 4. Map Items
        if (request.getItems() != null) {
            for (PrescriptionItemDTO itemDTO : request.getItems()) {
                PrescriptionItem item = new PrescriptionItem();
                item.setMedicineName(itemDTO.getMedicineName());
                item.setDosage(itemDTO.getDosage());
                item.setDuration(itemDTO.getDuration());
                item.setQuantity(itemDTO.getQuantity());
                item.setInstructions(itemDTO.getInstructions());

                item.setPrescription(prescription);
                prescription.getItems().add(item);
            }
        }

        Prescription saved = prescriptionRepo.save(prescription);

// 5. Auto-Complete Appointment (UPDATED LOGIC)
        // Check if current status is NOT "COMPLETED"
        if (appointment.getStatus() == null || !appointment.getStatus().getCode().equals("COMPLETED")) {

            // Fetch the "COMPLETED" status entity from DB
            AppointmentStatusLookup completedStatus = statusLookupRepo.findByCode("COMPLETED")
                    .orElseThrow(() -> new ValidationException("System Configuration Error: Status 'COMPLETED' not found.", "CONFIG_ERROR", HttpStatus.INTERNAL_SERVER_ERROR));

            appointment.setStatus(completedStatus); // Assign the Entity, not an Enum
            appointmentRepo.save(appointment);
        }

        return ApiResponse.success(mapToResponseDTO(saved), messageSourceUtil.getMessage("prescription.create.success"));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PrescriptionResponseDTO> getPrescriptionByAppointment(Long appointmentId) {

        // Find using specific Clinic ID to prevent viewing other clinics' data
        Prescription prescription = prescriptionRepo.findByAppointmentIdAndClinicId(
                appointmentId,
                TenantContext.getCurrentTenant()
        ).orElseThrow(() -> new ValidationException("Prescription not found", "PRESCRIPTION_NOT_FOUND", HttpStatus.NOT_FOUND));

        return ApiResponse.success(mapToResponseDTO(prescription), "Prescription retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PrescriptionResponseDTO> getPrescriptionById(Long id) {

        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new ValidationException("Prescription not found", "PRESCRIPTION_NOT_FOUND", HttpStatus.NOT_FOUND));

        // SECURITY CHECK: Ensure it belongs to this clinic
        if (!Objects.equals(prescription.getClinicId(), TenantContext.getCurrentTenant())) {
            throw new ValidationException("Prescription not found", "PRESCRIPTION_NOT_FOUND", HttpStatus.NOT_FOUND);
        }

        return ApiResponse.success(mapToResponseDTO(prescription), "Prescription retrieved successfully");
    }

    @Override
    public ApiResponse<PrescriptionResponseDTO> updatePrescription(Long id, PrescriptionRequestDTO request, String updatedBy) {

        Prescription existingPrescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new ValidationException("Prescription not found", "PRESCRIPTION_NOT_FOUND", HttpStatus.NOT_FOUND));

        // SECURITY CHECK
        if (!Objects.equals(existingPrescription.getClinicId(), TenantContext.getCurrentTenant())) {
            throw new ValidationException("Prescription not found", "PRESCRIPTION_NOT_FOUND", HttpStatus.NOT_FOUND);
        }

        if (existingPrescription.getStatus() == PrescriptionStatus.DISPENSED) {
            throw new ValidationException("Cannot update a prescription that has already been dispensed.", "PRESCRIPTION_LOCKED", HttpStatus.BAD_REQUEST);
        }

        existingPrescription.setNote(request.getNote());

        existingPrescription.setUpdatedDate(LocalDateTime.now());
        existingPrescription.setUpdatedBy(updatedBy);

        existingPrescription.getItems().clear();

        if (request.getItems() != null) {
            for (PrescriptionItemDTO itemDTO : request.getItems()) {
                PrescriptionItem item = new PrescriptionItem();
                item.setMedicineName(itemDTO.getMedicineName());
                item.setDosage(itemDTO.getDosage());
                item.setDuration(itemDTO.getDuration());
                item.setQuantity(itemDTO.getQuantity());
                item.setInstructions(itemDTO.getInstructions());

                item.setPrescription(existingPrescription);
                existingPrescription.getItems().add(item);
            }
        }

        Prescription saved = prescriptionRepo.save(existingPrescription);

        return ApiResponse.success(mapToResponseDTO(saved), messageSourceUtil.getMessage("prescription.update.success"));
    }

    // Helper Mapper
    private PrescriptionResponseDTO mapToResponseDTO(Prescription p) {
        PrescriptionResponseDTO dto = new PrescriptionResponseDTO();
        dto.setId(p.getId());
        dto.setAppointmentId(p.getAppointment().getId());

        // Null checks for relationships
        if(p.getAppointment().getDoctorSchedule() != null && p.getAppointment().getDoctorSchedule().getDoctor() != null) {
            dto.setDoctorName((p.getAppointment().getDoctorSchedule().getDoctor().getFirstName() + " " + p.getAppointment().getDoctorSchedule().getDoctor().getLastName()));
        }

        dto.setNote(p.getNote());
        dto.setStatus(p.getStatus());
        dto.setCreatedDate(p.getCreatedDate());
        dto.setCreatedBy(p.getCreatedBy());

        if(p.getAppointment().getPatient() != null) {
            dto.setPatientName(p.getAppointment().getPatient().getFirstName() + " " + p.getAppointment().getPatient().getLastName());
        }

        // Map Items
        dto.setItems(p.getItems().stream().map(item -> {
            PrescriptionItemDTO iDto = new PrescriptionItemDTO();
            iDto.setMedicineName(item.getMedicineName());
            iDto.setDosage(item.getDosage());
            iDto.setDuration(item.getDuration());
            iDto.setQuantity(item.getQuantity());
            iDto.setInstructions(item.getInstructions());
            return iDto;
        }).collect(Collectors.toList()));

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PrescriptionResponseDTO> getPrescriptionForPatient(Long appointmentId, Long patientId) {

        Integer clinicId = TenantContext.getCurrentTenant();

        // 1. Fetch the Appointment first to validate ownership
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new ValidationException("Appointment not found", "APPOINTMENT_NOT_FOUND", HttpStatus.NOT_FOUND));

        // 2. SECURITY: Check Tenant
        if (!Objects.equals(appointment.getClinicId(), clinicId)) {
            throw new ValidationException("Appointment not found", "APPOINTMENT_NOT_FOUND", HttpStatus.NOT_FOUND);
        }

        // 3. SECURITY: Check if this appointment actually belongs to the logged-in Patient
        if (!Objects.equals(appointment.getPatient().getId(), patientId)) {
            throw new ValidationException("Access denied. This appointment does not belong to you.", "ACCESS_DENIED", HttpStatus.FORBIDDEN);
        }

        // 4. Fetch the Prescription
        Prescription prescription = prescriptionRepo.findByAppointmentIdAndClinicId(appointmentId, clinicId)
                .orElseThrow(() -> new ValidationException("No prescription issued for this appointment yet.", "PRESCRIPTION_NOT_FOUND", HttpStatus.NOT_FOUND));

        return ApiResponse.success(mapToResponseDTO(prescription), "Prescription retrieved successfully");
    }
}