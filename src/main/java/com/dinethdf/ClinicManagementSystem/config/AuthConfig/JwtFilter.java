package com.dinethdf.ClinicManagementSystem.config.AuthConfig;

import com.dinethdf.ClinicManagementSystem.Model.UsersAuth.UserPrincipal;
import com.dinethdf.ClinicManagementSystem.Service.AuthService.JWTService;
import com.dinethdf.ClinicManagementSystem.Service.UsersAuth.PatientDetailsService;
import com.dinethdf.ClinicManagementSystem.Service.UsersAuth.UserDetailsService;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
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
    ApplicationContext context;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;
        String role = null;
        Integer clinicId = null;
        String planCode = null;
        String currency = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            username = jwtService.extractUserName(token);
            role = jwtService.extractUserRole(token);
            clinicId = jwtService.extractClinicId(token);

            planCode = jwtService.extractPlaneCode(token);


            if (clinicId != null) {
                TenantContext.setCurrentTenant(clinicId);
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails;

            if ("PATIENT".equals(role)) {
                userDetails = context.getBean(PatientDetailsService.class).loadUserByUsername(username);
            } else {
                userDetails = context.getBean(UserDetailsService.class).loadUserByUsername(username);
            }

            if (jwtService.validateToken(token, userDetails)) {

                if (userDetails instanceof UserPrincipal) {
                    UserPrincipal principal = (UserPrincipal) userDetails;
                    principal.setPlanCode(planCode);
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
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