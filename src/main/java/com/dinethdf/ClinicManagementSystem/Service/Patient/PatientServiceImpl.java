package com.dinethdf.ClinicManagementSystem.Service.Patient;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientRegistrationDto;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientResponseDto;
import com.dinethdf.ClinicManagementSystem.Model.Patient.PatientDetails;
import com.dinethdf.ClinicManagementSystem.Repo.Patient.PatientDetailsRepo;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    PatientDetailsRepo patientDetailsRepo;

    @Autowired
    private MessageSourceUtil messageSourceUtil;

    @Override
    public List<PatientResponseDto> getAllPatients() {
        return List.of();
    }

    @Override
    public PatientResponseDto getPatientById(Long patientId) {
        return null;
    }

    @Override
    public PatientResponseDto getPatientByAuthId(Long authId) {
        return null;
    }

    @Override
    public PatientResponseDto registerPatientDetailsSelf(PatientRegistrationDto requestDto) {
        return null;
    }

    @Override
    @Transactional
    public PatientDetails addOrUpdatePatientDetailsByStaff(PatientDetails patientDetails) {

        try {
            System.out.println("system patien saving");
            System.out.println(patientDetails.getPatientNo());


            patientDetails.setPatientNo(null);
            PatientDetails savedPatient = patientDetailsRepo.save(patientDetails);
            System.out.println(patientDetails.getId());
            // 2. Generate patientNo using ID
            String patientNo = generatePatientNo(savedPatient.getId());
            System.out.println(patientNo);
            // 3. Update patientNo and save again
            savedPatient.setPatientNo(patientNo);
            return patientDetailsRepo.save(savedPatient);

        }catch (Exception e){
            System.out.println("errror is the");
            System.out.println(e.getMessage());
            throw new ValidationException(messageSourceUtil.getMessage("patient.self.register.error"),  "1234", HttpStatus.CONFLICT);
        }


    }


    private String generatePatientNo(Long id) {
        String prefix = "P-2025-";
        return prefix + String.format("%04d", id);  // e.g., P-2025-0001
    }

    @Override
    public PatientResponseDto editPatientDetails(Long patientId, PatientRegistrationDto requestDto) {
        return null;
    }

    @Override
    public PatientResponseDto approvePatient(Long patientId) {
        return null;
    }

    @Override
    public void deletePatient(Long patientId) {

    }
}
