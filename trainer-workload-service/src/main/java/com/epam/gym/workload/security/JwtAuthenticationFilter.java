package com.epam.gym.workload.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
import java.util.Arrays;
import java.util.Collection;

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

        log.info("DEBUG: Processing request to URI: {}", requestURI);
        log.info("DEBUG: Authorization header: {}", requestTokenHeader);

        // Skip JWT validation for health check and actuator endpoints
        if (requestURI.contains("/actuator/") || requestURI.contains("/health")) {
            log.info("DEBUG: Skipping auth for health/actuator endpoint");
            filterChain.doFilter(request, response);
            return;
        }

        String username = null;
        String jwtToken = null;

        // JWT Token is in the form "Bearer token"
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            log.info("DEBUG: Extracted JWT token: {}", jwtToken.substring(0, Math.min(20, jwtToken.length())) + "...");
            try {
                username = jwtUtil.extractUsername(jwtToken);
                log.info("DEBUG: Extracted username: {}", username);
            } catch (Exception e) {
                log.error("DEBUG: Unable to extract username from JWT token: {}", e.getMessage());
            }
        } else {
            log.info("DEBUG: No Bearer token found in request");
        }

        // Validate token and set authentication
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            log.info("DEBUG: Attempting to validate token for username: {}", username);
            if (jwtUtil.validateToken(jwtToken)) {
                log.info("DEBUG: Token validation successful");
                // Check if this is a service token
                try {
                    String serviceFlag = jwtUtil.extractClaim(jwtToken, claims -> claims.get("service", String.class));
                    if ("true".equals(serviceFlag) || Boolean.TRUE.equals(jwtUtil.extractClaim(jwtToken, claims -> claims.get("service", Boolean.class)))) {
                        // Service token - grant SERVICE role
                        Collection<SimpleGrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_SERVICE"));
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(username, null, authorities);
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.debug("Service authentication set for: {}", username);
                    } else {
                        // Regular user token
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.debug("User authentication set for: {}", username);
                    }
                } catch (Exception e) {
                    // Fallback to regular user token
                    log.debug("Service claim not found, treating as user token");
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("User authentication set for: {}", username);
                }
            } else {
                log.debug("JWT token validation failed for user: {}", username);
            }
        }

        filterChain.doFilter(request, response);
    }
}
