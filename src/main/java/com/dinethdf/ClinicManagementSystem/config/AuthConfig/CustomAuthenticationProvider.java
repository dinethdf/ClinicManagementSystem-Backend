package com.dinethdf.ClinicManagementSystem.config.AuthConfig;


import com.dinethdf.ClinicManagementSystem.Service.UsersAuth.PatientDetailsService;
import com.dinethdf.ClinicManagementSystem.Service.UsersAuth.UserDetailsService;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
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

        CustomAuthToken customToken = (CustomAuthToken) authentication;
        String loginType = customToken.getLoginType();
        Integer clinicId = customToken.getClinicId(); // <--- Get Clinic ID

        UserDetails user;
        TenantContext.setCurrentTenant(clinicId);
        if ("PATIENT".equalsIgnoreCase(loginType)) {
            user = patientDetailsService.loadUserByUsernameAndClinic(username, clinicId);
        } else if ("USER".equalsIgnoreCase(loginType)) {
            user = userDetailsService.loadUserByUsernameAndClinic(username, clinicId);
        } else {
            throw new BadCredentialsException("Unknown login type");
        }


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
