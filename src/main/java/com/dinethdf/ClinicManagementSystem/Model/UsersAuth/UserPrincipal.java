package com.dinethdf.ClinicManagementSystem.Model.UsersAuth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    private final UserAuth userAuth;
    private String planCode;

    public UserPrincipal(UserAuth userAuth) {
        this.userAuth = userAuth;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
         return Collections.singleton(new SimpleGrantedAuthority(userAuth.getRole().getName()));
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
        return userAuth.getClinicId();
    }

    @Override
    public String getPassword() {
        return userAuth.getPassword();
    }

    @Override
    public String getUsername() {
        return userAuth.getEmail();
    }

    public Long getUserDetailsId() {
        return userAuth.getDetailsId();
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
        return userAuth.getEnable();
    }

    @Override
    public String toString() {
        return "UserPrincipal{" +
                "userAuth=" + userAuth +
                ", planCode='" + planCode + '\'' +
                '}';
    }


}
