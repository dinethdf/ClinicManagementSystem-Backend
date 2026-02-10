package com.dinethdf.ClinicManagementSystem.Service.Clinic;


import com.dinethdf.ClinicManagementSystem.Model.Clinic.Clinic;
import com.dinethdf.ClinicManagementSystem.Model.Clinic.ClinicSettings;

import com.dinethdf.ClinicManagementSystem.Repo.Clinic.ClinicRepository;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ClinicServiceImpl implements ClinicService {

    private final ClinicRepository clinicRepository;

    @Autowired
    public ClinicServiceImpl(ClinicRepository clinicRepository) {
        this.clinicRepository = clinicRepository;
    }

    @Override
    public Clinic createClinic(Clinic clinic) {
        // 1. Validation: Check if subdomain is unique
        if (clinicRepository.existsBySubdomain(clinic.getSubdomain())) {
            throw new RuntimeException("Subdomain '" + clinic.getSubdomain() + "' is already taken.");
        }

        // 2. Generate Clinic Code (UUID) if not provided
        if (clinic.getClinicCode() == null || clinic.getClinicCode().isEmpty()) {
            clinic.setClinicCode(UUID.randomUUID().toString());
        }

        // 3. Set Defaults if missing
        if (clinic.getSettings() == null) {
            clinic.setSettings(new ClinicSettings()); // Uses defaults defined in POJO
        }
        if (clinic.getStatus() == null) {
            clinic.setStatus(Clinic.ClinicStatus.TRIAL);
        }

        // 4. Set Trial Logic (e.g., Default 14 days if not set)
        if (clinic.getTrialEndsAt() == null && clinic.getStatus() == Clinic.ClinicStatus.TRIAL) {
            clinic.setTrialEndsAt(LocalDateTime.now().plusDays(14));
        }

        return clinicRepository.save(clinic);
    }

    @Override
    public List<Clinic> getAllClinics() {
        return clinicRepository.findAll();
    }

    @Override
    public Clinic getClinicById(Integer id) {
        return clinicRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Clinic not found with ID: " + id, "CLINIC_NOT_FOUND"));
    }

    @Override
    public Optional<Clinic> getClinicByCode(String clinicCode) {
        return clinicRepository.findByClinicCodeAndDeletedFalse(clinicCode);
    }

    @Override
    public Optional<Clinic> getClinicBySubdomain(String subdomain) {
        return clinicRepository.findBySubdomain(subdomain);
    }

    @Override
    public Clinic updateClinic(String clinicCode, Clinic clinicDetails) {
        Clinic existingClinic = clinicRepository.findByClinicCodeAndDeletedFalse(clinicCode)
                .orElseThrow(() -> new RuntimeException("Clinic not found with code: " + clinicCode));

        // Update basic fields
        existingClinic.setName(clinicDetails.getName());
        existingClinic.setMobileNo(clinicDetails.getMobileNo());
        existingClinic.setAddress(clinicDetails.getAddress());
        existingClinic.setCity(clinicDetails.getCity());
        existingClinic.setDistrict(clinicDetails.getDistrict());
        existingClinic.setLogoUrl(clinicDetails.getLogoUrl());

        // Update Status/Plan if provided
        if (clinicDetails.getPlanCode() != null) {
            existingClinic.setPlanCode(clinicDetails.getPlanCode());
        }
        if (clinicDetails.getStatus() != null) {
            existingClinic.setStatus(clinicDetails.getStatus());
        }

        // Update Settings (Merge or Replace logic)
        // Here we replace, but you could implement logic to merge specific keys
        if (clinicDetails.getSettings() != null) {
            existingClinic.setSettings(clinicDetails.getSettings());
        }

        return clinicRepository.save(existingClinic);
    }

    @Override
    public void deleteClinic(String clinicCode) {
        Clinic clinic = clinicRepository.findByClinicCode(clinicCode)
                .orElseThrow(() -> new RuntimeException("Clinic not found"));

        // Soft Delete
        clinic.setDeleted(true);
        clinic.setStatus(Clinic.ClinicStatus.CANCELLED);
        clinicRepository.save(clinic);
    }

    @Override
    public boolean isSubdomainTaken(String subdomain) {
        return clinicRepository.existsBySubdomain(subdomain);
    }
}