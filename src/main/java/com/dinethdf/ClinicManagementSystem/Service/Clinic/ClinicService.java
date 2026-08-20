package com.dinethdf.ClinicManagementSystem.Service.Clinic;

import com.dinethdf.ClinicManagementSystem.DTO.Clinic.ClinicResponseDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Clinic.ClinicUpdateDTO;
import com.dinethdf.ClinicManagementSystem.DTO.Clinic.PlanChangeDTO;
import com.dinethdf.ClinicManagementSystem.Model.Clinic.Clinic;
import com.dinethdf.ClinicManagementSystem.Model.Clinic.ClinicSettings;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ClinicService {

    Clinic createClinic(Clinic clinic);

    List<Clinic> getAllClinics();

    Clinic getClinicById(Integer id);

    String getCurrentEncryptedSecrets();

    void updateEncryptedSecrets(Map<String, String> incomingSecrets);

    ClinicResponseDTO getCurrentClinicDetails();

    ClinicResponseDTO updateCurrentClinicDetails(ClinicUpdateDTO request);

    ClinicResponseDTO updateCurrentClinicSettings(ClinicSettings request);

    ClinicResponseDTO updateSubscriptionPlan(PlanChangeDTO request);

    Optional<Clinic> getClinicByCode(String clinicCode);

    Optional<Clinic> getClinicBySubdomain(String subdomain);

    Clinic updateClinic(String clinicCode, Clinic clinicDetails);

    void deleteClinic(String clinicCode); // Soft delete

    boolean isSubdomainTaken(String subdomain);
}