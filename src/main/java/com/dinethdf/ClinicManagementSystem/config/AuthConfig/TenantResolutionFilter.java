package com.dinethdf.ClinicManagementSystem.config.AuthConfig;

import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import com.dinethdf.ClinicManagementSystem.config.multitenant.ClinicRoutingInfo;
import com.dinethdf.ClinicManagementSystem.config.multitenant.ClinicRoutingRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Resolves the tenant for public endpoints (where there is no JWT token).
 * It looks for an 'X-Tenant-Code' header. If found, it populates the TenantContext
 * so that the TenantRoutingDataSource knows which database to use.
 */
@Component
public class TenantResolutionFilter extends OncePerRequestFilter {

    @Autowired
    private ClinicRoutingRegistry clinicRoutingRegistry;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // If the context is already set (e.g., by JwtFilter), skip
        if (TenantContext.getCurrentTenant() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // For public endpoints (like /api/public/**), the frontend must send the clinic code.
        String tenantCode = request.getHeader("X-Tenant-Code");
        boolean contextSetByUs = false;

        if (tenantCode != null && !tenantCode.isEmpty()) {
            ClinicRoutingInfo routingInfo = clinicRoutingRegistry.getByCode(tenantCode);
            
            if (routingInfo != null) {
                TenantContext.setFromRoutingInfo(
                        routingInfo.getClinicId(),
                        routingInfo.getClinicCode(),
                        routingInfo.getSchemaName()
                );
                contextSetByUs = true;
            } else {
                // Issue 4: Fail fast with 404 instead of silent fallback to master
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"status\":\"error\",\"message\":\"Invalid Tenant Code\"}");
                return;
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Issue 5: We only clear the context here if we were the ones who set it.
            if (contextSetByUs) {
                TenantContext.clear();
            }
        }
    }
}
