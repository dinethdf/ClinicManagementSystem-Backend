package com.dinethdf.ClinicManagementSystem.Service.UsersAuth;

import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserAuth;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserPrincipal;
import com.dinethdf.ClinicManagementSystem.Repo.UsersAuth.UserRepo;
import com.dinethdf.ClinicManagementSystem.config.TenantContext; // Import your TenantContext
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    @Autowired
    private UserRepo repo;

    // This method is used by Spring Security during authentication
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Get the current Clinic ID from the ThreadLocal context
        Integer clinicId = TenantContext.getCurrentTenant();

        if (clinicId == null) {
            throw new UsernameNotFoundException("Tenant ID is missing. Cannot authenticate user.");
        }

        // 2. Find User by Email AND Clinic ID
        // Note: You must update UserRepo to have this method!
        UserAuth userAuth = repo.findByEmailAndClinicId(email, clinicId);

        if (userAuth == null){
            System.out.println("User Not Found in Clinic ID: " + clinicId);
            throw new UsernameNotFoundException("User Not Found");
        }

        return new UserPrincipal(userAuth);
    }

    // Helper method (if needed elsewhere)
    public UserAuth getUserByEmail(String email) throws UsernameNotFoundException {
        Integer clinicId = TenantContext.getCurrentTenant();
        UserAuth userAuth = repo.findByEmailAndClinicId(email, clinicId);

        if (userAuth == null){
            throw new UsernameNotFoundException("User Not Found");
        }
        return userAuth;
    }

    // NEW: Method for Login Phase (where Context might not be set yet)
    // The AuthenticationProvider will call this manually
    public UserDetails loadUserByUsernameAndClinic(String email, Integer clinicId) {
        UserAuth userAuth = repo.findByEmailAndClinicId(email, clinicId);
        if (userAuth == null) {
            throw new UsernameNotFoundException("User Not Found in Clinic " + clinicId);
        }
        return new UserPrincipal(userAuth);
    }
}