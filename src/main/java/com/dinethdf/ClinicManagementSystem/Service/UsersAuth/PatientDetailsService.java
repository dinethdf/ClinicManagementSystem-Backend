package com.dinethdf.ClinicManagementSystem.Service.UsersAuth;

import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.PatientAuth;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.PatientPrincipal;
import com.dinethdf.ClinicManagementSystem.Repo.UsersAuth.PatientRepo;
import com.dinethdf.ClinicManagementSystem.config.TenantContext; // Import TenantContext
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PatientDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    @Autowired
    private PatientRepo patientRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Get Clinic ID
        Integer clinicId = TenantContext.getCurrentTenant();

        if (clinicId == null) {
            throw new UsernameNotFoundException("Tenant ID missing.");
        }

        // 2. Find Patient specific to this clinic
        PatientAuth patientAuth = patientRepo.findByEmailAndClinicId(email, clinicId);

        if (patientAuth == null){
            System.out.println("Patient Not Found in Clinic " + clinicId);
            throw new UsernameNotFoundException("Patient Not Found");
        }

        return new PatientPrincipal(patientAuth);
    }

    public PatientAuth getUserByEmail(String email) throws UsernameNotFoundException {
        Integer clinicId = TenantContext.getCurrentTenant();
        PatientAuth patientAuth = patientRepo.findByEmailAndClinicId(email, clinicId);

        if (patientAuth == null){
            throw new UsernameNotFoundException("Patient Not Found");
        }
        return patientAuth;
    }

    // NEW: Method for Login Phase
    public UserDetails loadUserByUsernameAndClinic(String email, Integer clinicId) {
        PatientAuth patientAuth = patientRepo.findByEmailAndClinicId(email, clinicId);
        if (patientAuth == null) {
            throw new UsernameNotFoundException("Patient Not Found in Clinic " + clinicId);
        }
        return new PatientPrincipal(patientAuth);
    }
}