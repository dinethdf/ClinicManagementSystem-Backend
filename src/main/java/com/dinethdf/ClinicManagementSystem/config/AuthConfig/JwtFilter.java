package com.dinethdf.ClinicManagementSystem.config.AuthConfig;

import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.PatientPrincipal;
import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserPrincipal;
import com.dinethdf.ClinicManagementSystem.Service.AuthService.JWTService;
import com.dinethdf.ClinicManagementSystem.Service.UsersAuth.PatientDetailsService;
import com.dinethdf.ClinicManagementSystem.Service.UsersAuth.UserDetailsService;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import com.dinethdf.ClinicManagementSystem.config.multitenant.ClinicRoutingInfo;
import com.dinethdf.ClinicManagementSystem.config.multitenant.ClinicRoutingRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private ClinicRoutingRegistry clinicRoutingRegistry;

    @Autowired
    ApplicationContext context;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;
        String role = null;
        Integer clinicId = null;
        String clinicCode = null;
        String planCode = null;
        String currency = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            username = jwtService.extractUserName(token);
            role = jwtService.extractUserRole(token);
            clinicId = jwtService.extractClinicId(token);
            clinicCode = jwtService.extractClinicCode(token);
            planCode = jwtService.extractPlaneCode(token);

            if (clinicId != null && clinicId == -1) {
                // SYSTEM-LEVEL Master Admin Context
                TenantContext.setFromRoutingInfo(-1, "MASTER", "shared");
            } else if (clinicId != null && clinicCode != null) {
                // Look up routing info to get the correct schema
                ClinicRoutingInfo routingInfo = clinicRoutingRegistry.getByCode(clinicCode);
                String schemaName = (routingInfo != null) ? routingInfo.getSchemaName() : "shared";
                
                // Set the FULL context for the dynamic router
                TenantContext.setFromRoutingInfo(clinicId, clinicCode, schemaName);
            } else if (clinicId != null) {
                // Fallback for older tokens without clinicCode
                TenantContext.setCurrentTenant(clinicId);
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtService.isRouteValidateToken(token)) {

                Long detailsId = jwtService.extractUserDetailsId(token);

                UserDetails principal;

                if ("PATIENT".equals(role)) {
                    PatientPrincipal patient = new PatientPrincipal();
                    patient.setUsername(username);
                    patient.setRole(role);
                    patient.setClinicId(clinicId);
                    patient.setPatientDetailsId(detailsId);
                    patient.setPlanCode(planCode);
                    principal = patient;
                } else {
                    UserPrincipal user = new UserPrincipal();
                    user.setUsername(username);
                    user.setRole(role);
                    user.setClinicId(clinicId);
                    user.setUserDetailsId(detailsId);
                    user.setPlanCode(planCode);
                    principal = user;
                }

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                principal, null, principal.getAuthorities()
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}