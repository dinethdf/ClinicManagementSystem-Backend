package com.dinethdf.ClinicManagementSystem.Service.UsersAuth;

import com.dinethdf.ClinicManagementSystem.DTO.ClinicStaff.ClinicStaffResponseDto;
import com.dinethdf.ClinicManagementSystem.DTO.Doctor.DoctorDetailsDto;
import com.dinethdf.ClinicManagementSystem.DTO.LoginRequest;
import com.dinethdf.ClinicManagementSystem.Model.AuthModels.RefreshTokenRequest;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserAuth;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserPrincipal;
import com.dinethdf.ClinicManagementSystem.Repo.UsersAuth.UserRepo;
import com.dinethdf.ClinicManagementSystem.Model.AuthModels.JwtAuthenticationResponse;

import com.dinethdf.ClinicManagementSystem.Service.AuthService.JWTService;
import com.dinethdf.ClinicManagementSystem.Service.ClinicStaff.ClinicStaffService;
import com.dinethdf.ClinicManagementSystem.Service.Doctor.DoctorService;
import com.dinethdf.ClinicManagementSystem.config.AuthConfig.CustomAuthToken;
import com.dinethdf.ClinicManagementSystem.config.TenantContext; // NEW: Context Handling
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
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
public class UserService {

    @Autowired
    UserRepo userRepo;

    @Autowired
    private JWTService jwtService;

    @Autowired
    AuthenticationManager authManager;

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    ClinicStaffService clinicStaffService;

    @Autowired
    DoctorService doctorService;

    @Autowired
    private MessageSourceUtil messageSourceUtil;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public UserAuth register(UserAuth userAuth){
        // Ensure Tenant is present
        if(userAuth.getClinicId() == null) {
            throw new ValidationException(messageSourceUtil.getMessageOrCustom("clinic.required", null, "Clinic ID Required"), "CLINIC_ID_MISSING");
        }

        userAuth.setPassword(encoder.encode(userAuth.getPassword()));
        userAuth.setEnable(true);
        return userRepo.save(userAuth);
    }


    public JwtAuthenticationResponse signIn(LoginRequest request){

        try {
            // 1. Create Token with Clinic ID
            CustomAuthToken authToken = new CustomAuthToken(
                    request.getUsername(),
                    request.getPassword(),
                    request.getLoginType(),
                    request.getClinicId() // Pass Clinic ID to Auth Provider
            );

            // 2. Authenticate
            Authentication auth = authManager.authenticate(authToken);
            UserPrincipal user = (UserPrincipal) auth.getPrincipal();

            System.out.println("Authenticated: " + auth.isAuthenticated());
            System.out.println("Principal:s " + user.getUserDetailsId());
            System.out.println("Authorities: " + auth.getAuthorities());
            System.out.println("Details: " + auth.getDetails());

            JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();

            if (auth.isAuthenticated()) {

                var jwt = jwtService.generateUserToken(user.getUsername(), user.getRole().getName(), user.getUserDetailsId(), user.getClinicId());


                jwtAuthenticationResponse.setAccessToken(jwt);
                jwtAuthenticationResponse.setRefreshToken("NotSet");

                if (user.getUserDetailsId() != null){

                    if (Objects.equals(user.getRole().getName(), "DOCTOR")) {
                        DoctorDetailsDto staffUser = doctorService.getDoctorDetailsByIdForLoging(user.getUserDetailsId(), user.getClinicId());

                        jwtAuthenticationResponse.setUserName(staffUser.getFirstName() + " " + staffUser.getLastName() );
                        jwtAuthenticationResponse.setUserEmail(staffUser.getEmail());
                        jwtAuthenticationResponse.setUserImg(staffUser.getProfileImg());

                    }else {
                        ClinicStaffResponseDto staffUser = clinicStaffService.getStaffByIdForLogin(user.getUserDetailsId(), user.getClinicId());

                        jwtAuthenticationResponse.setUserName(staffUser.getFirstName() + " " + staffUser.getLastName() );
                        jwtAuthenticationResponse.setUserEmail(staffUser.getEmail());
                        jwtAuthenticationResponse.setUserImg(staffUser.getProfileImg());

                    }

                jwtAuthenticationResponse.setUserRole(user.getRole().getName());
                }
                return jwtAuthenticationResponse;
            }

            return jwtAuthenticationResponse;


        } catch (BadCredentialsException e) {
            throw new ValidationException(messageSourceUtil.getMessage("auth.invalid.credentials"),  "true");
        } catch (AuthenticationException e) {
            throw new ValidationException(messageSourceUtil.getMessage("auth.failed"),  "true");
        } catch (IllegalArgumentException e) {
            // Often thrown if clinicId is null
            throw new ValidationException("Invalid Login Request Data", "INVALID_REQUEST");
        }
    }


    public JwtAuthenticationResponse refreshToken (RefreshTokenRequest refreshTokenRequest) {
        String userEmail;
        Integer clinicId;

        try {
            userEmail = jwtService.extractUserName(refreshTokenRequest.getToken());
            clinicId = jwtService.extractClinicId(refreshTokenRequest.getToken()); // New helper method needed
        } catch (RuntimeException e){
            throw new ValidationException(messageSourceUtil.getMessageOrCustom("user.not.found", null, "User not found"), "TOKEN_INVALID");
        } catch (Exception e){
            throw new ValidationException(messageSourceUtil.getMessageOrCustom("token.invalid", null, "Invalid Token"), "TOKEN_INVALID");
        }

        try {
            // Set context so UserDetailsService knows where to look
            if (clinicId != null) {
                TenantContext.setCurrentTenant(clinicId);
            }

            UserDetails user = userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.validateToken(refreshTokenRequest.getToken(), user)){
                // Re-generate token with clinicId

//                var jwt = jwtService.generateUserToken(user.getUsername(), clinicId); // Un comment this whern u sue that

                JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
                jwtAuthenticationResponse.setAccessToken("jwt");
                jwtAuthenticationResponse.setRefreshToken(refreshTokenRequest.getToken());

                return jwtAuthenticationResponse;
            }

        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid Token Credentials", "INVALID_TOKEN");
        } finally {
            // Cleanup context
            TenantContext.clear();
        }

        return null;
    }


    public Map<String, Object> validateToken(String authHeader) {
        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("valid", false);
            return response;
        }

        String token = authHeader.substring(7);
        try {
            String username = jwtService.extractUserName(token);
            if (jwtService.isRouteValidateToken(token)) {
                response.put("valid", true);
                response.put("username", username);
                return response;
            }
        } catch (Exception e) {
            response.put("valid", false);
            return response;
        }

        response.put("valid", false);
        return response;
    }
}