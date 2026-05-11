package com.dinethdf.ClinicManagementSystem.config;

import com.dinethdf.ClinicManagementSystem.config.AuthConfig.CustomAuthenticationProvider;
import com.dinethdf.ClinicManagementSystem.config.AuthConfig.JwtFilter;
import com.dinethdf.ClinicManagementSystem.config.AuthConfig.TenantResolutionFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private TenantResolutionFilter tenantResolutionFilter;

    @Value("${cors.allowed.origins:http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                        .requestMatchers("register", "login", "signIn", "refresh", "patientSignIn", "validate-token")
                        .permitAll()
                        .requestMatchers("/api/admin/clinics/login").permitAll()
                        // .anyRequest().authenticated()
                        .requestMatchers("/userSignIn", "/patientSignIn").permitAll()

                        .requestMatchers("/api/good/a").hasAnyAuthority("ADMIN")
                        .requestMatchers("/api/good/b").hasAnyAuthority("PATIENT")
                        .requestMatchers("/api/hello/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/test/**").permitAll()

                        // Public
                        .requestMatchers("/api/schedules/upcoming-public").permitAll()
                        .requestMatchers("/api/otp/**").permitAll()
                        .requestMatchers("/api/specialization/**").permitAll()

                        // .requestMatchers("api/patient/get-details/staff/**").permitAll()
                        .requestMatchers("/api/patient/**")
                        .hasAnyAuthority("ADMIN", "USER", "PATIENT", "STAFF", "DOCTOR")

                        .requestMatchers("/api/auth/staff/**").hasAnyAuthority("ADMIN", "USER", "STAFF", "DOCTOR")
                        .requestMatchers("/api/roles/**").hasAnyAuthority("ADMIN", "SUPER-ADMIN")
                        .requestMatchers("/api/auth/patient/**").hasAnyAuthority("PATIENT")
                        .requestMatchers("/api/staff/**").hasAnyAuthority("ADMIN")
                        .requestMatchers("/api/doctor/**").hasAnyAuthority("ADMIN", "USER", "STAFF", "DOCTOR")
                        .requestMatchers("/api/schedules/**").hasAnyAuthority("ADMIN", "USER", "STAFF", "DOCTOR")
                        .requestMatchers("/api/lab/types/**").hasAnyAuthority("ADMIN", "USER")
                        .requestMatchers("/api/lab/requests/**")
                        .hasAnyAuthority("ADMIN", "USER", "STAFF", "DOCTOR", "PATIENT")
                        .requestMatchers("/api/appointments/**").hasAnyAuthority("ADMIN", "USER", "STAFF", "DOCTOR")
                        .requestMatchers("/api/payments/**").hasAnyAuthority("ADMIN", "USER", "STAFF", "DOCTOR")
                        .requestMatchers("/api/financials/**").hasAnyAuthority("ADMIN", "USER", "STAFF", "DOCTOR")
                        .requestMatchers("/api/payouts/**").hasAnyAuthority("ADMIN", "USER", "DOCTOR")
                        .requestMatchers("/api/prescriptions/**").hasAnyAuthority("ADMIN", "DOCTOR")

                        .requestMatchers("/api/admin-analysis/**").hasAnyAuthority("ADMIN", "USER", "STAFF", "DOCTOR")
                        .requestMatchers("/api/patient-analysis/**").hasAnyAuthority("ADMIN", "USER", "STAFF", "DOCTOR")

                        .requestMatchers("/api/patient-portal/**").hasAnyAuthority("ADMIN", "PATIENT") // remove admin

                        .requestMatchers("/api/report/admin/**").hasAnyAuthority("ADMIN", "USER")

                        .requestMatchers("/api/queue/**").hasAnyAuthority("ADMIN", "USER", "STAFF", "DOCTOR")

                        .requestMatchers("/api/clinic/info/**").authenticated()
                        .requestMatchers("/api/clinic/settings/**").hasAnyAuthority("ADMIN")

                        // All other requests MUST be authenticated — this MUST be last
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(tenantResolutionFilter, JwtFilter.class)
                .build();

    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Restrict to configured origins only — no more wildcard
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        config.setAllowedOriginPatterns(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, CustomAuthenticationProvider provider)
            throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(provider)
                .build();
    }

    // @Bean
    // public AuthenticationProvider authenticationProvider(){
    // DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    // provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
    // provider.setUserDetailsService(userDetailsService);
    // return provider;
    // }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    // @Bean
    // public AuthenticationManager
    // authenticationManager(AuthenticationConfiguration config) throws Exception {
    // return config.getAuthenticationManager();
    // }

}
