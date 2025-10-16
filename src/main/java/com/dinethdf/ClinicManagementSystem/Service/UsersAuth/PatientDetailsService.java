package com.dinethdf.ClinicManagementSystem.Service.UsersAuth;

import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.PatientAuth;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.PatientPrincipal;
import com.dinethdf.ClinicManagementSystem.Repo.UsersAuth.PatientRepo;
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
        PatientAuth patientAuth = patientRepo.findByEmail(email);

        if (patientAuth == null){
            System.out.println("patient Not Found");
            throw new UsernameNotFoundException("patient Not Found");
        }

        return new PatientPrincipal(patientAuth);
    }

    public PatientAuth getUserByEmail(String email) throws UsernameNotFoundException {
        PatientAuth patientAuth = patientRepo.findByEmail(email);

        if (patientAuth == null){
            System.out.println("patient Not Found");
            throw new UsernameNotFoundException("patient Not Found");
        }
        return patientAuth;
    }
}
