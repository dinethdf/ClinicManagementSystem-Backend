package com.dinethdf.ClinicManagementSystem.config.AuthConfig;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class CustomAuthToken extends UsernamePasswordAuthenticationToken {
    private final String loginType;
    private final Integer clinicId;

    public CustomAuthToken(String username, String password, String loginType, Integer clinicId) {
        super(username, password);
        this.loginType = loginType;
        this.clinicId = clinicId;
    }

    public String getLoginType() {
        return loginType;
    }
    public Integer getClinicId() { return clinicId; }
}
