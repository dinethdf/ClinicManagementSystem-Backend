package com.dinethdf.ClinicManagementSystem.Service.Users;

import com.dinethdf.ClinicManagementSystem.Model.Users.Patient;
import com.dinethdf.ClinicManagementSystem.Model.Users.PatientPrincipal;
import com.dinethdf.ClinicManagementSystem.Model.Users.User;
import com.dinethdf.ClinicManagementSystem.Model.Users.UserPrincipal;
import com.dinethdf.ClinicManagementSystem.Repo.PatientRepo;
import com.dinethdf.ClinicManagementSystem.Repo.UserRepo;
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
        Patient patient = patientRepo.findByEmail(email);

        if (patient == null){
            System.out.println("patient Not Found");
            throw new UsernameNotFoundException("patient Not Found");
        }

        return new PatientPrincipal(patient);
    }

    public Patient getUserByEmail(String email) throws UsernameNotFoundException {
        Patient patient = patientRepo.findByEmail(email);

        if (patient == null){
            System.out.println("patient Not Found");
            throw new UsernameNotFoundException("patient Not Found");
        }
        return patient;
    }
}
