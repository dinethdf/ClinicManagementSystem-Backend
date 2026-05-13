package com.dinethdf.ClinicManagementSystem.config;

import com.dinethdf.ClinicManagementSystem.config.TenantContext;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .expireAfterWrite(12, TimeUnit.HOURS) // Deletes secrets after 12 hours
                .maximumSize(500);
    }

    // 2. The Manager Bean: Connects Caffeine to Spring Boot's @Cacheable system
    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager(
                "clinicSecretsCache",    // Clinic AES-encrypted secrets (smsAppApiKey, etc.)
                "clinicDetailsCache",    // Clinic name, settings, SMS gateway config
                "systemConfigCache",     // Per-clinic feature flags (SMS_OTP_ENABLED, EMAIL_OTP_ENABLED)
                "clinicByIdCache"        // Clinic entity by ID — used by login flow & JWT generation
        );
        caffeineCacheManager.setCaffeine(caffeine);
        return caffeineCacheManager;
    }

    /**
     * Custom Key Generator for Multi-Tenancy.
     * Generates keys like: "tenantCode:methodName:param1,param2"
     * Usage: @Cacheable(value = "myCache", keyGenerator = "tenantKeyGenerator")
     */
    @Bean("tenantKeyGenerator")
    public KeyGenerator tenantKeyGenerator() {
        return (target, method, params) -> {
            String tenantCode = TenantContext.getCurrentTenantCode();
            if (tenantCode == null) {
                tenantCode = "GLOBAL";
            }
            return tenantCode + ":" + method.getName() + ":" + StringUtils.arrayToCommaDelimitedString(params);
        };
    }
}