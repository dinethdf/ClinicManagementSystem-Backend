package com.dinethdf.ClinicManagementSystem.Service.UsersAuth;

import com.dinethdf.ClinicManagementSystem.DTO.LoginRequest;
import com.dinethdf.ClinicManagementSystem.Model.AuthModels.JwtAuthenticationResponse;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.PatientAuth;
import com.dinethdf.ClinicManagementSystem.Repo.UsersAuth.PatientRepo;

import com.dinethdf.ClinicManagementSystem.Service.AuthService.JWTService;
import com.dinethdf.ClinicManagementSystem.config.AuthConfig.CustomAuthToken;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PatientAuthService {

    @Autowired
    PatientRepo patientRepo;

    @Autowired
    private JWTService jwtService;

    @Autowired
    AuthenticationManager authManager;


    @Autowired
    private MessageSourceUtil messageSourceUtil;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public PatientAuth register(PatientAuth patientAuth){
        patientAuth.setPassword(encoder.encode(patientAuth.getPassword()));
       return patientRepo.save(patientAuth);
    }


//    public JwtAuthenticationResponse signIn(Patient patient){
//
//        try {
//
//         Authentication authentication =
//              authManager.authenticate(new UsernamePasswordAuthenticationToken(patient.getEmail(), patient.getPassword()));
//
//
//           var jwt = jwtService.generateToken(user.getEmail());
//           var refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user.getEmail());
//
//           JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
//           jwtAuthenticationResponse.setAccessToken(jwt);
//           jwtAuthenticationResponse.setRefreshToken(refreshToken);
//
//           return jwtAuthenticationResponse;
//
//        }catch (BadCredentialsException e) {
//            throw new ValidationException(messageSourceUtil.getMessage("auth.invalid.credentials"),  "true");
//        } catch (AuthenticationException e) {
//            throw new ValidationException(messageSourceUtil.getMessage("auth.failed"),  "true");
//        } catch (IllegalArgumentException e) {
//            throw new IllegalArgumentException("Invalid Email Or password");
//        }
//    }

    public JwtAuthenticationResponse patientSignIn(LoginRequest request){

        try {
            CustomAuthToken authToken = new CustomAuthToken(
                    request.getUsername(),
                    request.getPassword(),
                    request.getLoginType()
            );

//         Authentication authentication =
//              authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
            Authentication auth = authManager.authenticate(authToken);

            // hear add api call to user details and with the requst send user details for FE


            var jwt = jwtService.generatePatientToken(request.getUsername());
//         var refreshToken = jwtService.generateRefreshToken(new HashMap<>(), request.getUsername());

            JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
            jwtAuthenticationResponse.setAccessToken(jwt);
            jwtAuthenticationResponse.setRefreshToken("refreshToken");

            return jwtAuthenticationResponse;

        }catch (BadCredentialsException e) {
            throw new ValidationException(messageSourceUtil.getMessage("auth.invalid.credentials"),  "INVALID_CREDENTIALS");
        } catch (AuthenticationException e) {
            throw new ValidationException(messageSourceUtil.getMessage("auth.failed"),  "INVALID_AUTHENTICATION");
        } catch (IllegalArgumentException e) {
//            throw new IllegalArgumentException("Invalid Email Or password");
            throw new ValidationException(messageSourceUtil.getMessage("auth.invalid.credentials"),  "INVALID_CREDENTIALS");
        }
    }


//    public JwtAuthenticationResponse refreshToken (RefreshTokenRequest refreshTokenRequest) {
//        String userEmail ;
//        try {
//            userEmail = jwtService.extractUserName(refreshTokenRequest.getToken());
//        }catch (RuntimeException e){
//            System.out.println("asdasd");
//            throw new ValidationException(messageSourceUtil.getMessageOrCustom("user.not.werwe", null, "User not found in the system"), "dasd");
//        }catch (Exception e){
//            System.out.println("asdasd");
//            throw new ValidationException(messageSourceUtil.getMessageOrCustom("user.not.asda", null, "User not found in the system"), "ewrwerwer");
//        }
//
//
//        try {
//            UserDetails user = userDetailsService.loadUserByUsername(userEmail);
//
//            if (jwtService.validateToken(refreshTokenRequest.getToken(), user)){
//                var jwt = jwtService.generateToken(user.getUsername());
//                JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
//                jwtAuthenticationResponse.setAccessToken(jwt);
//                jwtAuthenticationResponse.setRefreshToken(refreshTokenRequest.getToken());
//
//                return jwtAuthenticationResponse;
//            }
//        } catch (IllegalArgumentException e) {
//            throw new IllegalArgumentException("Invalid Email Or password");
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
//            String username = jwtService.extractUserName(token);
//            if (jwtService.isRouteValidateToken(token)) {
//                response.put("valid", true);
//                response.put("username", username);
//                return response;
//            }
//        response.put("valid", false);
//        return response;
//    }




}
