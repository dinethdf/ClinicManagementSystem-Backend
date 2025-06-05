package com.dinethdf.ClinicManagementSystem.config.AuthConfig;

import com.dinethdf.ClinicManagementSystem.Dto.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping
    public String login(@RequestBody LoginRequest request) {
        CustomAuthToken authToken = new CustomAuthToken(
                request.getUsername(),
                request.getPassword(),
                request.getLoginType()
        );

        Authentication auth = authenticationManager.authenticate(authToken);

        // After successful authentication, you can generate and return a JWT token
        // Example:
        // String jwt = jwtUtil.generateToken(auth);
        return "Login successful for: " + auth.getName() + " (" + request.getLoginType() + ")";
    }
}
