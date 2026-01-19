package com.dinethdf.ClinicManagementSystem.Service.Patient;

import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientHealthDTO;
import com.dinethdf.ClinicManagementSystem.Model.Patient.PatientAllergy;
import com.dinethdf.ClinicManagementSystem.Model.Patient.PatientChronicDisease;
import com.dinethdf.ClinicManagementSystem.Repo.Patient.PatientAllergyRepo;
import com.dinethdf.ClinicManagementSystem.Repo.Patient.PatientChronicDiseaseRepo;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientHealthServiceImpl {

    private final PatientAllergyRepo allergyRepo;
    private final PatientChronicDiseaseRepo diseaseRepo;

    public PatientHealthServiceImpl(PatientAllergyRepo allergyRepo, PatientChronicDiseaseRepo diseaseRepo) {
        this.allergyRepo = allergyRepo;
        this.diseaseRepo = diseaseRepo;
    }

    // ================= ALLERGY METHODS =================

    @Transactional
    public PatientHealthDTO.AllergyDTO addAllergy(Integer patientId, PatientHealthDTO.AllergyDTO dto) {
        PatientAllergy allergy = new PatientAllergy();
        allergy.setPatientId(patientId);
        allergy.setClinicId(TenantContext.getCurrentTenant());
        allergy.setAllergyName(dto.getAllergyName());
        allergy.setNotes(dto.getNotes());

        PatientAllergy saved = allergyRepo.save(allergy);

        dto.setId(saved.getId());
        return dto;
    }

    public List<PatientHealthDTO.AllergyDTO> getAllergies(Integer patientId) {
        return allergyRepo.findByPatientIdAndClinicId(patientId, TenantContext.getCurrentTenant())
                .stream()
                .map(a -> new PatientHealthDTO.AllergyDTO(a.getId(), a.getAllergyName(), a.getNotes()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAllergy(Integer id) {
        // Validate ownership/clinic before delete if strict security needed
        allergyRepo.deleteById(id);
    }

    // ================= DISEASE METHODS =================

    @Transactional
    public PatientHealthDTO.ChronicDiseaseDTO addDisease(Integer patientId, PatientHealthDTO.ChronicDiseaseDTO dto) {
        PatientChronicDisease disease = new PatientChronicDisease();
        disease.setPatientId(patientId);
        disease.setClinicId(TenantContext.getCurrentTenant());
        disease.setDiseaseName(dto.getDiseaseName());
        disease.setNotes(dto.getNotes());

        PatientChronicDisease saved = diseaseRepo.save(disease);

        dto.setId(saved.getId());
        return dto;
    }

    public List<PatientHealthDTO.ChronicDiseaseDTO> getDiseases(Integer patientId) {
        return diseaseRepo.findByPatientIdAndClinicId(patientId, TenantContext.getCurrentTenant())
                .stream()
                .map(d -> new PatientHealthDTO.ChronicDiseaseDTO(d.getId(), d.getDiseaseName(), d.getNotes()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteDisease(Integer id) {
        diseaseRepo.deleteById(id);
    }
}