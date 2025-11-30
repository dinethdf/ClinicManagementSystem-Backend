package com.dinethdf.ClinicManagementSystem.Service.Prescription;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Prescription.PrescriptionItemDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Prescription.PrescriptionRequestDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Prescription.PrescriptionResponseDTO;
import com.dinethdf.ClinicManagementSystem.Model.Appointment.Appointment;
import com.dinethdf.ClinicManagementSystem.Model.Prescription.Prescription;
import com.dinethdf.ClinicManagementSystem.Model.Prescription.PrescriptionItem;
import com.dinethdf.ClinicManagementSystem.Model.Prescription.PrescriptionStatus;
import com.dinethdf.ClinicManagementSystem.Repo.Appointment.AppointmentRepo;
import com.dinethdf.ClinicManagementSystem.Repo.Prescription.PrescriptionRepo;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepo prescriptionRepo;
    private final AppointmentRepo appointmentRepo;
    private final MessageSourceUtil messageSourceUtil;

    @Override
    public ApiResponse<PrescriptionResponseDTO> createPrescription(PrescriptionRequestDTO request, String createdBy) {

        Appointment appointment = appointmentRepo.findById(request.getAppointmentId())
                .orElseThrow(() -> new ValidationException("Appointment not found", "APPOINTMENT_NOT_FOUND", HttpStatus.NOT_FOUND));

        if (prescriptionRepo.findByAppointmentId(request.getAppointmentId()).isPresent()) {
            throw new ValidationException("Prescription already exists for this appointment", "DUPLICATE_PRESCRIPTION", HttpStatus.CONFLICT);
        }

        Prescription prescription = new Prescription();
        prescription.setAppointment(appointment);
        prescription.setNote(request.getNote());
        prescription.setStatus(PrescriptionStatus.ISSUED);

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


        if (appointment.getStatus() != Appointment.AppointmentStatus.COMPLETED) {
            appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
            appointmentRepo.save(appointment);
        }

        return ApiResponse.success(mapToResponseDTO(saved), messageSourceUtil.getMessage("prescription.create.success"));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PrescriptionResponseDTO> getPrescriptionByAppointment(Long appointmentId) {
        Prescription prescription = prescriptionRepo.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ValidationException("Prescription not found", "PRESCRIPTION_NOT_FOUND", HttpStatus.NOT_FOUND));

        return ApiResponse.success(mapToResponseDTO(prescription), "Prescription retrieved successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PrescriptionResponseDTO> getPrescriptionById(Long id) {
        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new ValidationException("Prescription not found", "PRESCRIPTION_NOT_FOUND", HttpStatus.NOT_FOUND));

        return ApiResponse.success(mapToResponseDTO(prescription), "Prescription retrieved successfully");
    }

    // Helper Mapper
    private PrescriptionResponseDTO mapToResponseDTO(Prescription p) {
        PrescriptionResponseDTO dto = new PrescriptionResponseDTO();
        dto.setId(p.getId());
        dto.setAppointmentId(p.getAppointment().getId());
        dto.setNote(p.getNote());
        dto.setStatus(p.getStatus());
        dto.setCreatedDate(p.getCreatedDate());
        dto.setCreatedBy(p.getCreatedBy());

        // Assuming Appointment has Patient and DoctorSchedule -> Doctor
        // Adjust these getters based on your actual Appointment Entity structure
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
    public ApiResponse<PrescriptionResponseDTO> updatePrescription(Long id, PrescriptionRequestDTO request, String updatedBy) {

        Prescription existingPrescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new ValidationException("Prescription not found", "PRESCRIPTION_NOT_FOUND", HttpStatus.NOT_FOUND));

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
}