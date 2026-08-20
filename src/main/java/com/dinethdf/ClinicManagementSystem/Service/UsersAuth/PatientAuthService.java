package com.dinethdf.ClinicManagementSystem.Service.UsersAuth;

import com.dinethdf.ClinicManagementSystem.DTO.ClinicStaff.ClinicStaffResponseDto;
import com.dinethdf.ClinicManagementSystem.DTO.Doctor.DoctorDetailsDto;
import com.dinethdf.ClinicManagementSystem.DTO.LoginRequest;
import com.dinethdf.ClinicManagementSystem.DTO.Patient.PatientResponseDTO;
import com.dinethdf.ClinicManagementSystem.Model.AuthModels.JwtAuthenticationResponse;
import com.dinethdf.ClinicManagementSystem.Model.AuthModels.RefreshTokenRequest;
import com.dinethdf.ClinicManagementSystem.Model.Clinic.Clinic;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.PatientAuth;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.PatientPrincipal;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserPrincipal;
import com.dinethdf.ClinicManagementSystem.Repo.UsersAuth.PatientRepo;

import com.dinethdf.ClinicManagementSystem.Service.AuthService.JWTService;
import com.dinethdf.ClinicManagementSystem.Service.Clinic.ClinicService;
import com.dinethdf.ClinicManagementSystem.Service.Patient.PatientService;
import com.dinethdf.ClinicManagementSystem.config.AuthConfig.CustomAuthToken;
import com.dinethdf.ClinicManagementSystem.config.TenantContext; // Required for context handling
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.util.EncryptionUtil;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Service
public class PatientAuthService {

    @Autowired
    PatientRepo patientRepo;

    @Autowired
    private JWTService jwtService;

    @Autowired
    AuthenticationManager authManager;

    @Autowired
    PatientDetailsService patientDetailsService; // Needed for refresh token logic

    @Autowired
    PatientService patientService;

    @Autowired
    ClinicService clinicService;

    @Autowired
    EncryptionUtil encryptionUtil;

    @Autowired
    private MessageSourceUtil messageSourceUtil;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public PatientAuth register(PatientAuth patientAuth){
        // Validation: Clinic ID is mandatory
        if (patientAuth.getClinicId() == null) {
            throw new ValidationException(messageSourceUtil.getMessageOrCustom("clinic.required", null, "Clinic ID Required"), "CLINIC_ID_MISSING");
        }

        patientAuth.setPassword(encoder.encode(patientAuth.getPassword()));
        return patientRepo.save(patientAuth);
    }


    public JwtAuthenticationResponse patientSignIn(LoginRequest request){

        try {

            Clinic loginClinic = resolveClinic(request);
            Integer clinicId = loginClinic.getId();

            CustomAuthToken authToken = new CustomAuthToken(
                    request.getUsername(),
                    request.getPassword(),
                    request.getLoginType(),
                    request.getClinicId() // <--- CRITICAL: Pass Clinic ID
            );

            // 2. Authenticate
            Authentication auth = authManager.authenticate(authToken);
            PatientPrincipal patient = (PatientPrincipal) auth.getPrincipal();

            System.out.println("Authenticated: " + auth.isAuthenticated());
            System.out.println("Principal: " + patient.getPatientDetailsId());
            System.out.println("Authorities: " + auth.getAuthorities());
            System.out.println("Details: " + auth.getDetails());

            JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();

            if (auth.isAuthenticated()) {

                Clinic clinic = clinicService.getClinicById(patient.getClinicId());


                var jwt = jwtService.generatePatientToken(patient.getUsername(), patient.getRole().getName(),
                        patient.getPatientDetailsId(), patient.getClinicId(),
                        encryptionUtil.encryptClinicPlane(clinic.getPlanCode()), clinic.getClinicCode());

                jwtAuthenticationResponse.setAccessToken(jwt);
                jwtAuthenticationResponse.setRefreshToken("refreshToken");

                if (patient.getPatientDetailsId() != null){

                    PatientResponseDTO patientUser = patientService.getPatientByIdForLogin(patient.getPatientDetailsId(), patient.getClinicId());

                     jwtAuthenticationResponse.setUserName(patientUser.firstName() + " " + patientUser.lastName() );
                     jwtAuthenticationResponse.setUserEmail(patientUser.email());
                     jwtAuthenticationResponse.setUserImg(patientUser.profileImg());
                     jwtAuthenticationResponse.setUserRole(patient.getRole().getName());
                }
                return jwtAuthenticationResponse;
            }

            return jwtAuthenticationResponse;

        } catch (BadCredentialsException e) {
            throw new ValidationException(messageSourceUtil.getMessage("auth.invalid.credentials"),  "INVALID_CREDENTIALS");
        } catch (AuthenticationException e) {
            throw new ValidationException(messageSourceUtil.getMessage("auth.failed"),  "INVALID_AUTHENTICATION");
        } catch (IllegalArgumentException e) {
            throw new ValidationException(messageSourceUtil.getMessage("auth.invalid.credentials"),  "INVALID_CREDENTIALS");
        } finally {
            TenantContext.clear();
        }
    }

    private Clinic resolveClinic(LoginRequest request) {

        if (request.getClinicId() != null) {
            return clinicService.getClinicById(request.getClinicId());
        }

        if (request.getSubdomain() != null && !request.getSubdomain().isEmpty()) {
            return clinicService.getClinicBySubdomain(request.getSubdomain())
                    .orElseThrow(() -> new ValidationException("Clinic not found for subdomain: " + request.getSubdomain(), "INVALID_SUBDOMAIN"));
        }

        throw new ValidationException("Either Clinic ID or Subdomain is required for login", "MISSING_TENANT_INFO");
    }

    /*
     * UPDATED REFRESH TOKEN LOGIC FOR MULTI-TENANCY
     * When uncommenting, ensure JWTService has extractClinicId()
     */
//    public JwtAuthenticationResponse refreshToken (RefreshTokenRequest refreshTokenRequest) {
//        String userEmail;
//        Integer clinicId;
//
//        try {
//            userEmail = jwtService.extractUserName(refreshTokenRequest.getToken());
//            clinicId = jwtService.extractClinicId(refreshTokenRequest.getToken()); // Need to extract tenant
//        } catch (RuntimeException e){
//            System.out.println("Token Extraction Error");
//            throw new ValidationException(messageSourceUtil.getMessageOrCustom("user.not.werwe", null, "User not found"), "TOKEN_ERROR");
//        } catch (Exception e){
//            System.out.println("General Token Error");
//            throw new ValidationException(messageSourceUtil.getMessageOrCustom("user.not.asda", null, "Invalid Token"), "TOKEN_INVALID");
//        }
//
//        try {
//            // CRITICAL: We must set the context before loading the user,
//            // otherwise the repository won't know which clinic to look in.
//            if (clinicId != null) {
//                TenantContext.setCurrentTenant(clinicId);
//            }
//
//            UserDetails user = patientDetailsService.loadUserByUsername(userEmail);
//
//            if (jwtService.validateToken(refreshTokenRequest.getToken(), user)){
//                // Re-generate token WITH clinicId
//                var jwt = jwtService.generatePatientToken(user.getUsername(), clinicId);
//
//                JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
//                jwtAuthenticationResponse.setAccessToken(jwt);
//                jwtAuthenticationResponse.setRefreshToken(refreshTokenRequest.getToken());
//
//                return jwtAuthenticationResponse;
//            }
//        } catch (IllegalArgumentException e) {
//            throw new IllegalArgumentException("Invalid Email Or password");
//        } finally {
//            // Always clear context to prevent memory leaks
//            TenantContext.clear();
//        }
//
//        return null;
//    }


//    public Map<String, Object> validateToken(String authHeader) {
//
//    Map<String, Object> response = new HashMap<>();
//            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//                response.put("valid", false);
//                return response;
//            }
//            String token = authHeader.substring(7);
//
//            try {
//                String username = jwtService.extractUserName(token);
//                // Check if token is valid structure-wise
//                if (jwtService.isRouteValidateToken(token)) {
//                    response.put("valid", true);
//                    response.put("username", username);
//                    return response;
//                }
//            } catch (Exception e) {
//                response.put("valid", false);
//                return response;
//            }
//
//        response.put("valid", false);
//        return response;
//    }

}