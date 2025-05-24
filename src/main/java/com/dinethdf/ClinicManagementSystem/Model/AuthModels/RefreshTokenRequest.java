package com.dinethdf.ClinicManagementSystem.Model.AuthModels;

public class RefreshTokenRequest {
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "RefreshTokenRequest{" +
                "token='" + token + '\'' +
                '}';
    }
}
