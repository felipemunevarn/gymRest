package com.epam.gym.cucumber;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(new TestAuthInjectionFilter(),
                        org.springframework.security.web.authentication.AnonymousAuthenticationFilter.class)
                .build();
    }

    static class TestAuthInjectionFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
                throws ServletException, IOException {
            String username = req.getHeader("X-Username");
            if (username == null || username.isBlank()) username = "alice";
            var principal = User.withUsername(username).password("N/A").authorities(List.of()).build();
            var auth = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
            chain.doFilter(req, res);
        }
    }
}