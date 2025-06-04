package com.dinethdf.ClinicManagementSystem.Service.Users;

import com.dinethdf.ClinicManagementSystem.Model.AuthModels.RefreshTokenRequest;
import com.dinethdf.ClinicManagementSystem.Model.Users.User;
import com.dinethdf.ClinicManagementSystem.Repo.UserRepo;
import com.dinethdf.ClinicManagementSystem.Model.AuthModels.JwtAuthenticationResponse;

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
    private MessageSourceUtil messageSourceUtil;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public User register(User user){
        user.setPassword(encoder.encode(user.getPassword()));
       return userRepo.save(user);
    }


// for the refresh tcken not use that

//    public String login(User user) {
//        try {
//            Authentication auth = authManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
//            );
//            return jwtService.generateToken(user.getEmail());
//        } catch (BadCredentialsException e) {
//            throw new ValidationException(messageSourceUtil.getMessage("auth.invalid.credentials"));
//        } catch (AuthenticationException e) {
//            throw new ValidationException(messageSourceUtil.getMessage("auth.failed"));
//        }
////        catch (BadCredentialsException e) {
////            throw new ValidationException(messageSourceUtil.getMessageOrCustom("user.not.found", null, "User not found in the system"));
////
////        }
//    }



    public JwtAuthenticationResponse signIn(User user){

        try {

         Authentication authentication =
              authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));


           var jwt = jwtService.generateToken(user.getEmail());
           var refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);

           JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
           jwtAuthenticationResponse.setAccessToken(jwt);
           jwtAuthenticationResponse.setRefreshToken(refreshToken);

           return jwtAuthenticationResponse;

        }catch (BadCredentialsException e) {
            throw new ValidationException(messageSourceUtil.getMessage("auth.invalid.credentials"),  "true");
        } catch (AuthenticationException e) {
            throw new ValidationException(messageSourceUtil.getMessage("auth.failed"),  "true");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Email Or password");
        }
    }


    public JwtAuthenticationResponse refreshToken (RefreshTokenRequest refreshTokenRequest) {
        String userEmail ;
        try {
            userEmail = jwtService.extractUserName(refreshTokenRequest.getToken());
        }catch (RuntimeException e){
            System.out.println("asdasd");
            throw new ValidationException(messageSourceUtil.getMessageOrCustom("user.not.werwe", null, "User not found in the system"), "dasd");
        }catch (Exception e){
            System.out.println("asdasd");
            throw new ValidationException(messageSourceUtil.getMessageOrCustom("user.not.asda", null, "User not found in the system"), "ewrwerwer");
        }


        try {
            UserDetails user = userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.validateToken(refreshTokenRequest.getToken(), user)){
                var jwt = jwtService.generateToken(user.getUsername());
                JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
                jwtAuthenticationResponse.setAccessToken(jwt);
                jwtAuthenticationResponse.setRefreshToken(refreshTokenRequest.getToken());

                return jwtAuthenticationResponse;
            }


        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Email Or password");
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
            String username = jwtService.extractUserName(token);
            if (jwtService.isRouteValidateToken(token)) {
                response.put("valid", true);
                response.put("username", username);
                return response;
            }



        response.put("valid", false);
        return response;
    }




}
