package com.dinethdf.ClinicManagementSystem.Service.Clinic;

import com.dinethdf.ClinicManagementSystem.DTO.Clinic.ClinicResponseDTO;
import com.dinethdf.ClinicManagementSystem.Model.Clinic.Clinic;

import java.util.List;
import java.util.Optional;

public interface ClinicService {

    Clinic createClinic(Clinic clinic);

    List<Clinic> getAllClinics();

    Clinic getClinicById(Integer id);

    String getCurrentEncryptedSecrets();

    ClinicResponseDTO getCurrentClinicDetails();

    Optional<Clinic> getClinicByCode(String clinicCode);

    Optional<Clinic> getClinicBySubdomain(String subdomain);

    Clinic updateClinic(String clinicCode, Clinic clinicDetails);

    void deleteClinic(String clinicCode); // Soft delete

    boolean isSubdomainTaken(String subdomain);
}