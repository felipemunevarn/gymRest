package com.epam.gym.cucumber;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@TestConfiguration
public class TestNoSecurityConfig {

    /**
     * Highest precedence, matches ALL requests, permits ALL.
     * This ensures NO auth challenge (401) is ever sent during tests.
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    SecurityFilterChain testPermissiveChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(request -> true) // match all
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                // keep a lightweight Authentication to satisfy any @PreAuthorize checks
                .addFilterBefore(new InjectTestUserFilter(), org.springframework.security.web.authentication.AnonymousAuthenticationFilter.class)
                .build();
    }

    static class InjectTestUserFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
                throws ServletException, IOException {
            var principal = User.withUsername("test-user")
                    .password("N/A")
                    .roles("TRAINER","USER") // helpful for @PreAuthorize("hasRole('TRAINER')")
                    .build();
            var auth = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
            chain.doFilter(req, res);
        }
    }
}

