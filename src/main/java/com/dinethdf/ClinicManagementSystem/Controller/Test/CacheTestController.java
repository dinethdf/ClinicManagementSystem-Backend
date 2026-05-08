package com.dinethdf.ClinicManagementSystem.Controller.Test;

import com.dinethdf.ClinicManagementSystem.DTO.ApiResponse;
import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * CacheTestController — Utility to verify Spring Caching status.
 * Access via: GET /api/test/cache/status
 */
@RestController
@RequestMapping("/api/test/cache")
@RequiredArgsConstructor
public class CacheTestController {

    private final CacheManager cacheManager;

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkCacheStatus(
            @RequestHeader(value = "X-Clinic-Id", required = false) Integer headerClinicId) {
        
        // If header provided, manually set it for this test request
        if (headerClinicId != null) {
            TenantContext.setCurrentTenant(headerClinicId);
        }

        Integer tenantId = TenantContext.getCurrentTenant();
        Map<String, Object> status = new HashMap<>();
        status.put("tenantId", tenantId == null ? "NULL (No Tenant)" : tenantId);

        // Check all three caches
        status.put("clinicDetailsCache", isKeyInCache("clinicDetailsCache", tenantId));
        status.put("clinicSecretsCache", isKeyInCache("clinicSecretsCache", tenantId));
        // systemConfigCache uses composite keys e.g. "1:SMS_OTP_ENABLED"
        status.put("systemConfigCache:SMS_OTP_ENABLED",  isKeyInCache("systemConfigCache", tenantId == null ? null : tenantId + ":SMS_OTP_ENABLED"));
        status.put("systemConfigCache:EMAIL_OTP_ENABLED", isKeyInCache("systemConfigCache", tenantId == null ? null : tenantId + ":EMAIL_OTP_ENABLED"));

        return ResponseEntity.ok(ApiResponse.success(status, "Cache status checked successfully"));
    }

    private String isKeyInCache(String cacheName, Object key) {
        if (key == null) return "MISS (No Key)";
        
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) return "CACHE_NOT_FOUND";
        
        Cache.ValueWrapper wrapper = cache.get(key);
        return (wrapper != null) ? "HIT (Data exists in cache)" : "MISS (Data not in cache)";
    }
}
