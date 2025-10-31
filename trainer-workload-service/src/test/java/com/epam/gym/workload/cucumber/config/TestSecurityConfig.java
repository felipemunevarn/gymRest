package com.epam.gym.workload.cucumber.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableWebSecurity
@Primary
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("=== AGGRESSIVE SECURITY DISABLE ===");

        // Most permissive security configuration possible
        return http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/**").permitAll()  // Explicitly permit all paths
                        .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .logout(logout -> logout.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }
}
