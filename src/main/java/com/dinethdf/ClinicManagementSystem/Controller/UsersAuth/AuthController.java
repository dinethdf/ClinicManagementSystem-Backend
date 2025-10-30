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
//@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    UserService userService;

    @Autowired
    PatientAuthService patientAuthService;

    @PostMapping("register")
    public UserAuth registerUser(@RequestBody UserAuth userAuth){
        return userService.register(userAuth);
    }

//    @PostMapping("login")
//    public String login(@RequestBody User user){
//      return userService.login(user);
//    }

    @PostMapping("userSignIn")
    public ResponseEntity<JwtAuthenticationResponse> signIn (@RequestBody LoginRequest request){
        return ResponseEntity.ok(userService.signIn(request));
    }

    @PostMapping("patientSignIn")
    public ResponseEntity<JwtAuthenticationResponse> patientSignIn (@RequestBody LoginRequest request){
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
