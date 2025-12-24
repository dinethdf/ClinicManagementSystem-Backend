package com.dinethdf.ClinicManagementSystem.Controller.UsersAuth;

import com.dinethdf.ClinicManagementSystem.DTO.LoginRequest;
import com.dinethdf.ClinicManagementSystem.Model.AuthModels.RefreshTokenRequest;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserAuth;
import com.dinethdf.ClinicManagementSystem.Service.UsersAuth.PatientAuthService;
import com.dinethdf.ClinicManagementSystem.Service.UsersAuth.UserService;
import com.dinethdf.ClinicManagementSystem.Model.AuthModels.JwtAuthenticationResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
// @CrossOrigin(origins = "http://localhost:3000") // Configure global CORS instead
public class AuthController {

    @Autowired
    UserService userService;

    @Autowired
    PatientAuthService patientAuthService;

    @PostMapping("register")
    public UserAuth registerUser(@RequestBody UserAuth userAuth){
        if (userAuth.getClinicId() == null) {
            throw new IllegalArgumentException("Clinic ID is required for registration");
        }
        return userService.register(userAuth);
    }

    @PostMapping("userSignIn")
    public ResponseEntity<JwtAuthenticationResponse> signIn (@RequestBody LoginRequest request){
        // Security check
        if (request.getClinicId() == null) {
            throw new IllegalArgumentException("Clinic ID is required for login");
        }
        return ResponseEntity.ok(userService.signIn(request));
    }

    /**
     * Patient Login
     * Request body must include: username, password, loginType="PATIENT", clinicId
     */
    @PostMapping("patientSignIn")
    public ResponseEntity<JwtAuthenticationResponse> patientSignIn (@RequestBody LoginRequest request){
        if (request.getClinicId() == null) {
            throw new IllegalArgumentException("Clinic ID is required for login");
        }
        return ResponseEntity.ok(patientAuthService.patientSignIn(request));
    }

    @PostMapping("refresh")
    public ResponseEntity<JwtAuthenticationResponse> refreshToken (@RequestBody RefreshTokenRequest refreshTokenRequest){
        return ResponseEntity.ok(userService.refreshToken(refreshTokenRequest));
    }

    @PostMapping("validate-token")
    public Map<String, Object> validateToken(@RequestHeader(value = "Authorization") String authHeader) {
        return userService.validateToken(authHeader);
    }
}