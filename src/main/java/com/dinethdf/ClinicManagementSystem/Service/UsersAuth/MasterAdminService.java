package com.dinethdf.ClinicManagementSystem.Service.UsersAuth;

import com.dinethdf.ClinicManagementSystem.DTO.LoginRequest;
import com.dinethdf.ClinicManagementSystem.Model.AuthModels.JwtAuthenticationResponse;
import com.dinethdf.ClinicManagementSystem.Service.AuthService.JWTService;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MasterAdminService {

    @Qualifier("masterJdbcTemplate")
    private final JdbcTemplate masterJdbcTemplate;

    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;

    public JwtAuthenticationResponse systemLogin(LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            throw new ValidationException("Username and Password are required", "INVALID_REQUEST");
        }

        // Query the master database
        String sql = "SELECT id, username, password, email, is_active FROM super_admins WHERE email = ?";
        List<Map<String, Object>> results = masterJdbcTemplate.queryForList(sql, request.getUsername());

        if (results.isEmpty()) {
            throw new ValidationException("Invalid Master Credentials", "true");
        }

        Map<String, Object> adminData = results.get(0);
        
        Boolean isActive = (Boolean) adminData.get("is_active");
        if (isActive != null && !isActive) {
            throw new ValidationException("Account is disabled", "true");
        }

        String encodedPassword = (String) adminData.get("password");
        if (!passwordEncoder.matches(request.getPassword(), encodedPassword)) {
            throw new ValidationException("Invalid Master Credentials", "true");
        }

        // Generate a Master JWT Token
        // Using clinicId = -1 to indicate this is a SYSTEM level admin
        String jwt = jwtService.generateUserToken(
                (String) adminData.get("email"), 
                "SUPER-ADMIN", 
                ((Number) adminData.get("id")).longValue(), 
                -1, 
                "SYSTEM_PLAN", 
                "MASTER"
        );

        JwtAuthenticationResponse response = new JwtAuthenticationResponse();
        response.setAccessToken(jwt);
        response.setRefreshToken("NotSet");
        response.setUserName((String) adminData.get("username"));
        response.setUserEmail((String) adminData.get("email"));
        response.setUserRole("SUPER-ADMIN");

        return response;
    }
}
