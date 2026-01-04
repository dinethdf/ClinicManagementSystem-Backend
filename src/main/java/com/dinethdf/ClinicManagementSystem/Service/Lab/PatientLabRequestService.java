package com.dinethdf.ClinicManagementSystem.Service.Lab;

import com.dinethdf.ClinicManagementSystem.DTO.Lab.LabRequestCreateDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Lab.LabRequestResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Lab.LabRequestUpdateDTO;
import com.dinethdf.ClinicManagementSystem.Model.Lab.PatientLabRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PatientLabRequestService {

    // 1. Create a new Test Request (Doctor or Walk-in)
    LabRequestResponseDTO createLabRequest(LabRequestCreateDTO createDTO);

    Page<LabRequestResponseDTO> getAllLabRequests(String search, PatientLabRequest.LabStatus status, Pageable pageable);

    // 2. View All Requests (For Lab Technician / Admin)
    // Supports search by Patient Name or Test Name
    Page<LabRequestResponseDTO> getAllLabRequests(String search, Pageable pageable);

    // 3. View Requests by Patient (For Patient History)
    Page<LabRequestResponseDTO> getLabRequestsByPatient(Integer patientId, Pageable pageable);

    // 4. Update Status or Add Results (For Technician)
    LabRequestResponseDTO updateLabRequest(Integer id, LabRequestUpdateDTO updateDTO);

    // 5. Get Single Request Details
    LabRequestResponseDTO getLabRequestById(Integer id);

    void deleteLabRequest(Integer id);
}