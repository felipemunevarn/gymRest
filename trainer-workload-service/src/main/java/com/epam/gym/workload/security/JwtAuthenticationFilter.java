package com.epam.gym.workload.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");
        final String requestURI = request.getRequestURI();

        log.debug("Processing request to URI: {}", requestURI);

        // Skip JWT validation for health check and actuator endpoints
        if (requestURI.contains("/actuator/") || requestURI.contains("/health")) {
            log.debug("Skipping auth for health/actuator endpoint");
            filterChain.doFilter(request, response);
            return;
        }

        String username = null;
        String jwtToken = null;

        // JWT Token is in the form "Bearer token"
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            log.debug("Extracted JWT token");
            try {
                username = jwtUtil.extractUsername(jwtToken);
                log.debug("Extracted username: {}", username);
            } catch (Exception e) {
                log.error("Unable to extract username from JWT token: {}", e.getMessage());
            }
        } else {
            log.debug("No Bearer token found in request");
        }

        // Validate token and set authentication
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            log.debug("Attempting to validate token for username: {}", username);
            if (jwtUtil.validateToken(jwtToken)) {
                log.debug("Token validation successful for user: {}", username);

                // Set authentication with no authorities
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("Authentication set for: {}", username);
            } else {
                log.debug("JWT token validation failed for user: {}", username);
            }
        }

        filterChain.doFilter(request, response);
    }
}
