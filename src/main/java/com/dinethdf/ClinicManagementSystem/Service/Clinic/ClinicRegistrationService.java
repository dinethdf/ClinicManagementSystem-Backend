package com.dinethdf.ClinicManagementSystem.Service.Clinic;


import com.dinethdf.ClinicManagementSystem.DTO.Clinic.ClinicRegistrationRequest;
import com.dinethdf.ClinicManagementSystem.DTO.Clinic.ClinicResponseDTO;
import com.dinethdf.ClinicManagementSystem.Model.Clinic.Clinic;
import com.dinethdf.ClinicManagementSystem.Model.Clinic.ClinicSettings;
import com.dinethdf.ClinicManagementSystem.Repo.Clinic.ClinicRepository;
import com.dinethdf.ClinicManagementSystem.config.multitenant.ClinicRoutingRegistry;
import com.dinethdf.ClinicManagementSystem.config.multitenant.TenantSchemaMode;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClinicRegistrationService {

    private final ClinicRepository clinicRepository;
    private final ClinicRoutingRegistry clinicRoutingRegistry;
    // private final UserAuthRepository userAuthRepository;
    // private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional // Critical: If user creation fails, Clinic is rolled back
    public ClinicResponseDTO registerNewClinic(ClinicRegistrationRequest request) {

        // 1. VALIDATION
        if (clinicRepository.existsBySubdomain(request.getSubdomain())) {
            throw new ValidationException("Subdomain is already taken", "SUBDOMAIN_TAKEN");
        }
        // if (userAuthRepository.existsByEmail(request.getEmail())) {
        //     throw new ValidationException("Email is already registered", "EMAIL_TAKEN");
        // }

        // 2. CREATE CLINIC (The "Trial" Logic)
        Clinic clinic = new Clinic();
        clinic.setName(request.getClinicName());
        clinic.setSubdomain(request.getSubdomain());
        clinic.setMobileNo1(request.getMobileNo1());
        clinic.setAddress(request.getAddress());
        clinic.setCity(request.getCity());

        // --- DEFAULTS ---
        clinic.setClinicCode(UUID.randomUUID().toString());
        clinic.setClinicSecret(generateRandomSecret()); // Helper method
        clinic.setPlanCode("PLAN_BASIC"); // Default Start Plan
        clinic.setStatus(Clinic.ClinicStatus.TRIAL);

        // Logic: 14 Days Trial
        clinic.setTrialEndsAt(LocalDateTime.now().plusDays(14));
        clinic.setRegisteredAt(LocalDateTime.now());

        // Logic: Default Settings
        ClinicSettings defaultSettings = new ClinicSettings();
        defaultSettings.setCurrency("LKR");
        defaultSettings.setTimezone("Asia/Colombo");
        clinic.setSettings(defaultSettings);

        // Save Clinic first to get the ID
        clinic = clinicRepository.save(clinic);

        // --- MULTI-TENANT: Add to Routing Registry dynamically ---
        // Public registration defaults to SHARED mode for now.
        clinicRoutingRegistry.registerNewClinic(
                clinic.getId(),
                clinic.getClinicCode(),
                clinic.getName(),
                clinic.getSubdomain(),
                "shared",
                false,
                clinic.getPlanCode()
        );


        // 3. CREATE ADMIN USER (Linked to this Clinic)
        // UserAuth adminUser = new UserAuth();
        // adminUser.setEmail(request.getEmail());
        // adminUser.setPassword(passwordEncoder.encode(request.getPassword()));
        // adminUser.setClinicId(clinic.getId()); // LINKING HAPPENS HERE
        // adminUser.setRole(roleRepository.findByName("ADMIN"));
        // adminUser.setEnable(true);

        // Save User...
        // userAuthRepository.save(adminUser);

        return ClinicResponseDTO.fromEntity(clinic);
    }

    private String generateRandomSecret() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }
}