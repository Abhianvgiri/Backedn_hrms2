package com.mwm.hrms.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/assets/**",
                                "/favicon.svg",
                                "/logo.jpeg",
                                "/icon-192.png",
                                "/manifest.json",
                                "/sw.js",
                                "/api/auth/**",
                                "/api/attendance/**",
                                "/api/dashboard/**",
                                "/api/payroll/**",
                                "/api/leaves/**",
                                "/api/holidays/**",
                                "/api/documents/**",
                                "/uploads/**",
                                "/api/employees/**",
                                "/api/test/**",
                                "/api/loans/**",
                                "/api/policies/**",
                                "/api/users/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}