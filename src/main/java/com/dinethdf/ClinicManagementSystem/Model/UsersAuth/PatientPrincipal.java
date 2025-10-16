package com.dinethdf.ClinicManagementSystem.Model.UsersAuth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class PatientPrincipal implements UserDetails {

    private PatientAuth patientAuth;

    public PatientPrincipal(PatientAuth patientAuth) {
        this.patientAuth = patientAuth;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return Collections.singleton(new SimpleGrantedAuthority(Role.ADMIN.name()));
         return Collections.singleton(new SimpleGrantedAuthority(patientAuth.getRole().getName()));
    }


//    public String getName() {
//        return user.getName();
//    }
//
//    public UserRole getRole() {
//        return user.getRole();
//    }

    @Override
    public String getPassword() {
        return patientAuth.getPassword();
    }

    @Override
    public String getUsername() {
        return patientAuth.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return patientAuth.getEnable();
    }
}
