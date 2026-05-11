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

        if (tenantCode != null && !tenantCode.isEmpty()) {
            ClinicRoutingInfo routingInfo = clinicRoutingRegistry.getByCode(tenantCode);
            
            if (routingInfo != null) {
                TenantContext.setFromRoutingInfo(
                        routingInfo.getClinicId(),
                        routingInfo.getClinicCode(),
                        routingInfo.getSchemaName()
                );
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // We only clear the context here if we were the ones who set it.
            // If JwtFilter set it, JwtFilter's finally block will clear it.
            // But to be safe, TenantContext.clear() handles it perfectly if we call it again.
            if (tenantCode != null && !tenantCode.isEmpty()) {
                TenantContext.clear();
            }
        }
    }
}
