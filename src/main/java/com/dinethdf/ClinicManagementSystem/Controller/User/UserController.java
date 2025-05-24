package com.dinethdf.ClinicManagementSystem.Controller.User;

import com.dinethdf.ClinicManagementSystem.Model.AuthModels.RefreshTokenRequest;
import com.dinethdf.ClinicManagementSystem.Model.Users.User;
import com.dinethdf.ClinicManagementSystem.Service.Users.UserService;
import com.dinethdf.ClinicManagementSystem.Model.AuthModels.JwtAuthenticationResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("register")
    public User registerUser(@RequestBody User user){
        return userService.register(user);
    }

//    @PostMapping("login")
//    public String login(@RequestBody User user){
//      return userService.login(user);
//    }

    @PostMapping("signIn")
    public ResponseEntity<JwtAuthenticationResponse> signIn (@RequestBody User user){
        return ResponseEntity.ok(userService.signIn(user));
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
