package com.dinethdf.ClinicManagementSystem.Model.UsersAuth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class PatientPrincipal implements UserDetails {

    private PatientAuth patientAuth;
    private String planCode;

    // ✅ ADDED - new fields
    private String username;
    private String role;
    private Integer clinicId;
    private Long patientDetailsId;

    // ✅ ADDED - empty constructor for JWT flow
    public PatientPrincipal() {}

    // ✅ ADDED - setters for new fields
    public void setUsername(String username) { this.username = username; }
    public void setRole(String role) { this.role = role; }
    public void setClinicId(Integer clinicId) { this.clinicId = clinicId; }
    public void setPatientDetailsId(Long patientDetailsId) { this.patientDetailsId = patientDetailsId; }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public PatientPrincipal(PatientAuth patientAuth) {
        this.patientAuth = patientAuth;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // ✅ CHANGED - handle both cases
        String roleName = (patientAuth != null) ? patientAuth.getRole().getName() : this.role;
        return Collections.singleton(new SimpleGrantedAuthority(roleName));
    }

    //    public String getName() {
//        return user.getName();
//    }
//
    public UserRole getRole() {
        return patientAuth.getRole();
    }

    public Integer getClinicId() {
        return (patientAuth != null) ? patientAuth.getClinicId() : this.clinicId; // ✅ CHANGED
    }

    @Override
    public String getPassword() {
        return (patientAuth != null) ? patientAuth.getPassword() : null; // ✅ CHANGED
    }

    @Override
    public String getUsername() {
        return (patientAuth != null) ? patientAuth.getEmail() : this.username; // ✅ CHANGED
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    public Long getPatientDetailsId() {
        return (patientAuth != null) ? patientAuth.getDetailsId() : this.patientDetailsId; // ✅ CHANGED
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
        return (patientAuth != null) ? patientAuth.getEnable() : true; // ✅ CHANGED
    }
}