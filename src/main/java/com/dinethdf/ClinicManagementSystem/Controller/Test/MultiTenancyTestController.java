package com.dinethdf.ClinicManagementSystem.Controller.Test;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import com.dinethdf.ClinicManagementSystem.config.multitenant.TenantDataSourcePoolManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Controller to manually test and verify multi-tenancy configurations.
 */
@RestController
@RequiredArgsConstructor
public class MultiTenancyTestController {

    private final TenantDataSourcePoolManager poolManager;

    /**
     * Get the active schemas loaded into Caffeine memory.
     * Publicly accessible via: GET /api/test/multitenancy/pools
     */
    @GetMapping("/api/test/multitenancy/pools")
    public ResponseEntity<ApiResponse<Set<String>>> getActivePools() {
        Set<String> activeSchemas = poolManager.getActiveSchemas();
        return ResponseEntity.ok(ApiResponse.success(activeSchemas, "Active connection pools retrieved successfully"));
    }

    /**
     * Evict a schema from the connection pool.
     * Publicly accessible via: POST /api/test/multitenancy/evict?schema=...
     */
    @PostMapping("/api/test/multitenancy/evict")
    public ResponseEntity<ApiResponse<String>> evictPool(@RequestParam String schema) {
        poolManager.removeDataSource(schema);
        return ResponseEntity.ok(ApiResponse.success(schema, "Evicted pool for schema: " + schema));
    }

    /**
     * Inspect the ThreadLocal TenantContext of the current request.
     * Publicly accessible via: GET /api/test/multitenancy/context
     */
    @GetMapping("/api/test/multitenancy/context")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getContext() {
        Map<String, Object> contextInfo = new HashMap<>();
        contextInfo.put("tenantId", TenantContext.getCurrentTenant());
        contextInfo.put("tenantCode", TenantContext.getCurrentTenantCode());
        contextInfo.put("schema", TenantContext.getCurrentSchema());
        return ResponseEntity.ok(ApiResponse.success(contextInfo, "Tenant context retrieved successfully"));
    }

    /**
     * Secured endpoint to test JWT authentication and context propagation.
     * Falls back to anyRequest().authenticated() in SecurityConfig.
     * Accessible via: GET /api/multitenancy-test/secured
     */
    @GetMapping("/api/multitenancy-test/secured")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSecuredContext() {
        Map<String, Object> contextInfo = new HashMap<>();
        contextInfo.put("secured", true);
        contextInfo.put("tenantId", TenantContext.getCurrentTenant());
        contextInfo.put("tenantCode", TenantContext.getCurrentTenantCode());
        contextInfo.put("schema", TenantContext.getCurrentSchema());
        return ResponseEntity.ok(ApiResponse.success(contextInfo, "Secured tenant context retrieved successfully"));
    }
}
