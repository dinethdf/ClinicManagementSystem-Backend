package com.dinethdf.ClinicManagementSystem.Service.Users;

import com.dinethdf.ClinicManagementSystem.Model.AuthModels.JwtAuthenticationResponse;
import com.dinethdf.ClinicManagementSystem.Model.AuthModels.RefreshTokenRequest;
import com.dinethdf.ClinicManagementSystem.Model.Users.Patient;
import com.dinethdf.ClinicManagementSystem.Repo.PatientRepo;

import com.dinethdf.ClinicManagementSystem.Service.AuthService.JWTService;
import com.dinethdf.ClinicManagementSystem.exception.ValidationException;
import com.dinethdf.ClinicManagementSystem.util.MessageSourceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PatientService {

    @Autowired
    PatientRepo patientRepo;

    @Autowired
    private JWTService jwtService;

    @Autowired
    AuthenticationManager authManager;


    @Autowired
    private MessageSourceUtil messageSourceUtil;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public Patient register(Patient patient){
        patient.setPassword(encoder.encode(patient.getPassword()));
       return patientRepo.save(patient);
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

    public JwtAuthenticationResponse patientSignIn(Patient patient){

        try {
           Authentication authentication =
                    authManager.authenticate(new UsernamePasswordAuthenticationToken(patient.getEmail(), patient.getPassword()));

//            var jwt = jwtService.generateToken(patient.getEmail());
//            var refreshToken = jwtService.generateRefreshToken(new HashMap<>(), patient.getEmail());
            JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
            jwtAuthenticationResponse.setAccessToken("asdasd");
            jwtAuthenticationResponse.setRefreshToken("asdasdasd");
            return jwtAuthenticationResponse;

        }catch (BadCredentialsException e) {
            throw new ValidationException(messageSourceUtil.getMessage("patient.invalid.credentials"),  "true");
        } catch (AuthenticationException e) {
            throw new ValidationException(messageSourceUtil.getMessage("auth.failed"),  "true");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid patient Email Or password");
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
