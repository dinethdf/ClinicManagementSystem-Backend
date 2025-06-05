package com.dinethdf.ClinicManagementSystem.config.AuthConfig;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class CustomAuthToken extends UsernamePasswordAuthenticationToken {
    private final String loginType;

    public CustomAuthToken(String username, String password, String loginType) {
        super(username, password);
        this.loginType = loginType;
    }

    public String getLoginType() {
        return loginType;
    }
}
