package com.dinethdf.ClinicManagementSystem.config.AuthConfig;


import com.dinethdf.ClinicManagementSystem.Service.Users.PatientDetailsService;
import com.dinethdf.ClinicManagementSystem.Service.Users.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PatientDetailsService patientDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String rawPassword = authentication.getCredentials().toString();
        String loginType = ((CustomAuthToken) authentication).getLoginType();

        UserDetails user;
        if ("PATIENT".equalsIgnoreCase(loginType)) {
            user = patientDetailsService.loadUserByUsername(username);
        } else if (true) {
            user = userDetailsService.loadUserByUsername("dineth@dd.com");
        } else {
            throw new BadCredentialsException("Unknown login type: " + loginType);
        }
        System.out.println(user.toString());
        System.out.println(rawPassword);
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        return new UsernamePasswordAuthenticationToken(user, rawPassword, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return CustomAuthToken.class.isAssignableFrom(authentication);
    }
}
