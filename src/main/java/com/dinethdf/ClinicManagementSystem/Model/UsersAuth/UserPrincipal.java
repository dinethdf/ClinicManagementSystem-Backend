package com.dinethdf.ClinicManagementSystem.Model.UsersAuth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    private UserAuth userAuth; // ✅ CHANGED - removed final
    private String planCode;

    // ✅ ADDED - empty constructor for JWT flow
    public UserPrincipal() {}

    // ✅ ADDED - new fields
    private String username;
    private String role;
    private Integer clinicId;
    private Long userDetailsId;

    // ✅ ADDED - setters for new fields
    public void setUsername(String username) { this.username = username; }
    public void setRole(String role) { this.role = role; }
    public void setClinicId(Integer clinicId) { this.clinicId = clinicId; }
    public void setUserDetailsId(Long userDetailsId) { this.userDetailsId = userDetailsId; }

    public UserPrincipal(UserAuth userAuth) {
        this.userAuth = userAuth;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // ✅ CHANGED - handle both cases
        String roleName = (userAuth != null) ? userAuth.getRole().getName() : this.role;
        return Collections.singleton(new SimpleGrantedAuthority(roleName));
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public UserRole getRole() {
        return userAuth.getRole();
    }

    public Integer getClinicId() {
        // ✅ CHANGED - handle both cases
        return (userAuth != null) ? userAuth.getClinicId() : this.clinicId;
    }

    @Override
    public String getPassword() {
        return (userAuth != null) ? userAuth.getPassword() : null; // ✅ CHANGED
    }

    @Override
    public String getUsername() {
        return (userAuth != null) ? userAuth.getEmail() : this.username; // ✅ CHANGED
    }

    public Long getUserDetailsId() {
        return (userAuth != null) ? userAuth.getDetailsId() : this.userDetailsId; // ✅ CHANGED
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
        return (userAuth != null) ? userAuth.getEnable() : true; // ✅ CHANGED
    }

    @Override
    public String toString() {
        return "UserPrincipal{" +
                "userAuth=" + userAuth +
                ", planCode='" + planCode + '\'' +
                '}';
    }
}