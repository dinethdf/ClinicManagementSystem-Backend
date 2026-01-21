package com.dinethdf.ClinicManagementSystem.Service.Prescription;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Prescription.PrescriptionRequestDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Prescription.PrescriptionResponseDTO;

public interface PrescriptionService {
    ApiResponse<PrescriptionResponseDTO> createPrescription(PrescriptionRequestDTO request, String createdBy);
    ApiResponse<PrescriptionResponseDTO> getPrescriptionByAppointment(Long appointmentId);
    ApiResponse<PrescriptionResponseDTO> getPrescriptionById(Long id);
    ApiResponse<PrescriptionResponseDTO> updatePrescription(Long id, PrescriptionRequestDTO request, String updatedBy);
    ApiResponse<PrescriptionResponseDTO> getPrescriptionForPatient(Long appointmentId, Long patientId);
}