package com.dinethdf.ClinicManagementSystem.config.AuthConfig;


import com.dinethdf.ClinicManagementSystem.Service.UsersAuth.PatientDetailsService;
import com.dinethdf.ClinicManagementSystem.Service.UsersAuth.UserDetailsService;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import com.dinethdf.ClinicManagementSystem.config.multitenant.ClinicRoutingInfo;
import com.dinethdf.ClinicManagementSystem.config.multitenant.ClinicRoutingRegistry;
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

    @Autowired
    private ClinicRoutingRegistry clinicRoutingRegistry;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String rawPassword = authentication.getCredentials().toString();

        CustomAuthToken customToken = (CustomAuthToken) authentication;
        String loginType = customToken.getLoginType();
        Integer clinicId = customToken.getClinicId(); // <--- Get Clinic ID

        System.out.println("DEBUG: Login attempt for clinicId: " + clinicId);

        UserDetails user;
        
        // Resolve full context (schema) before querying user
        ClinicRoutingInfo routingInfo = clinicRoutingRegistry.getById(clinicId);
        if (routingInfo != null) {
            System.out.println("DEBUG: Found Routing Info. Schema: " + routingInfo.getSchemaName());
            TenantContext.setFromRoutingInfo(
                routingInfo.getClinicId(), 
                routingInfo.getClinicCode(), 
                routingInfo.getSchemaName()
            );
        } else {
            System.out.println("DEBUG: NO Routing Info found for ID: " + clinicId + ". Falling back to shared.");
            TenantContext.setCurrentTenant(clinicId);
        }

        System.out.println("DEBUG: Current TenantContext Schema: " + TenantContext.getCurrentSchema());

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
