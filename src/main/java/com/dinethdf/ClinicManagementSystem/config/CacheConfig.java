package com.dinethdf.ClinicManagementSystem.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                "systemConfigCache"      // Per-clinic feature flags (SMS_OTP_ENABLED, EMAIL_OTP_ENABLED)
        );
        caffeineCacheManager.setCaffeine(caffeine);
        return caffeineCacheManager;
    }
}