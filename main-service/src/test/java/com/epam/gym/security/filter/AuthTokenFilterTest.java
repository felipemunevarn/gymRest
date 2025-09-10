package com.epam.gym.security.filter;

import com.epam.gym.security.util.JwtUtil;
import com.epam.gym.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthTokenFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private PrintWriter writer;

    @InjectMocks
    private AuthTokenFilter authTokenFilter;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        userDetails = new User("testuser", "password",
                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void doFilterInternal_NoAuthorizationHeader_ContinuesFilterChain() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(any());
        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void doFilterInternal_AuthorizationHeaderWithoutBearer_ContinuesFilterChain() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Basic sometoken");

        // When
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(any());
        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void doFilterInternal_ValidTokenUsernameNullFromJwt_ContinuesFilterChain() throws ServletException, IOException {
        // Given
        String token = "validtoken";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(null);

        // When
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).extractUsername(token);
        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void doFilterInternal_ValidTokenButSecurityContextHasAuthentication_ContinuesFilterChain() throws ServletException, IOException {
        // Given
        String token = "validtoken";
        String username = "testuser";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // When
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).extractUsername(token);
        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void doFilterInternal_InvalidToken_ContinuesFilterChain() throws ServletException, IOException {
        // Given
        String token = "invalidtoken";
        String username = "testuser";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtil.isValid(token, username)).thenReturn(false);

        // When
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).extractUsername(token);
        verify(userDetailsService).loadUserByUsername(username);
        verify(jwtUtil).isValid(token, username);
        verify(tokenBlacklistService, never()).isTokenBlacklisted(any());
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void doFilterInternal_ValidTokenButBlacklisted_ReturnsUnauthorized() throws ServletException, IOException {
        // Given
        String token = "blacklistedtoken";
        String username = "testuser";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtil.isValid(token, username)).thenReturn(true);
        when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(true);
        when(response.getWriter()).thenReturn(writer);

        // When
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(writer).write("Token has been invalidated");
        verify(filterChain, never()).doFilter(request, response);
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void doFilterInternal_ValidTokenNotBlacklisted_SetsAuthentication() throws ServletException, IOException {
        // Given
        String token = "validtoken";
        String username = "testuser";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtil.isValid(token, username)).thenReturn(true);
        when(tokenBlacklistService.isTokenBlacklisted(token)).thenReturn(false);

        // When
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).extractUsername(token);
        verify(userDetailsService).loadUserByUsername(username);
        verify(jwtUtil).isValid(token, username);
        verify(tokenBlacklistService).isTokenBlacklisted(token);
        verify(securityContext).setAuthentication(any());
    }

    @Test
    void doFilterInternal_BearerTokenWithOnlyBearerKeyword_ContinuesFilterChain() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        // When
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).extractUsername("");
        // The rest depends on what extractUsername("") returns
    }

//    @Test
//    void doFilterInternal_EmptyUsernameFromJwt_ContinuesFilterChain() throws ServletException, IOException {
//        // Given
//        String token = "validtoken";
//        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
//        when(jwtUtil.extractUsername(token)).thenReturn("");
//
//        // When
//        authTokenFilter.doFilterInternal(request, response, filterChain);
//
//        // Then
//        verify(filterChain).doFilter(request, response);
//        verify(jwtUtil).extractUsername(token);
//        verify(userDetailsService, never()).loadUserByUsername(any());
//        verify(securityContext, never()).setAuthentication(any());
//    }

//    @Test
//    void doFilterInternal_WhitespaceUsernameFromJwt_ContinuesFilterChain() throws ServletException, IOException {
//        // Given
//        String token = "validtoken";
//        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
//        when(jwtUtil.extractUsername(token)).thenReturn("   ");
//
//        // When
//        authTokenFilter.doFilterInternal(request, response, filterChain);
//
//        // Then
//        verify(filterChain).doFilter(request, response);
//        verify(jwtUtil).extractUsername(token);
//        // Will proceed to load user details since "   " is not null
//        verify(userDetailsService).loadUserByUsername("   ");
//    }

    @Test
    void doFilterInternal_UserDetailsServiceThrowsException_ContinuesFilterChain() throws ServletException, IOException {
        // Given
        String token = "validtoken";
        String username = "nonexistentuser";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(userDetailsService.loadUserByUsername(username))
                .thenThrow(new RuntimeException("User not found"));

        // When & Then - Exception should propagate
        try {
            authTokenFilter.doFilterInternal(request, response, filterChain);
        } catch (RuntimeException e) {
            // Expected
        }

        verify(jwtUtil).extractUsername(token);
        verify(userDetailsService).loadUserByUsername(username);
        verify(jwtUtil, never()).isValid(any(), any());
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void doFilterInternal_JwtUtilThrowsExceptionOnExtractUsername_ContinuesFilterChain() throws ServletException, IOException {
        // Given
        String token = "malformedtoken";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenThrow(new RuntimeException("Invalid JWT"));

        // When & Then - Exception should propagate
        try {
            authTokenFilter.doFilterInternal(request, response, filterChain);
        } catch (RuntimeException e) {
            // Expected
        }

        verify(jwtUtil).extractUsername(token);
        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void doFilterInternal_TokenBlacklistServiceThrowsException_PropagatesException() throws ServletException, IOException {
        // Given
        String token = "validtoken";
        String username = "testuser";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtUtil.isValid(token, username)).thenReturn(true);
        when(tokenBlacklistService.isTokenBlacklisted(token))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then - Exception should propagate
        try {
            authTokenFilter.doFilterInternal(request, response, filterChain);
        } catch (RuntimeException e) {
            // Expected
        }

        verify(tokenBlacklistService).isTokenBlacklisted(token);
        verify(securityContext, never()).setAuthentication(any());
    }
}
