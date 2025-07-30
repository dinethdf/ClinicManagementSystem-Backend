package com.dinethdf.ClinicManagementSystem.Controller.User;

import com.dinethdf.ClinicManagementSystem.DTO.LoginRequest;
import com.dinethdf.ClinicManagementSystem.Model.AuthModels.RefreshTokenRequest;
import com.dinethdf.ClinicManagementSystem.Model.Users.User;
import com.dinethdf.ClinicManagementSystem.Service.Users.PatientService;
import com.dinethdf.ClinicManagementSystem.Service.Users.UserService;
import com.dinethdf.ClinicManagementSystem.Model.AuthModels.JwtAuthenticationResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
//@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    PatientService patientService;

    @PostMapping("register")
    public User registerUser(@RequestBody User user){
        return userService.register(user);
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
        return ResponseEntity.ok(patientService.patientSignIn(request));
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
