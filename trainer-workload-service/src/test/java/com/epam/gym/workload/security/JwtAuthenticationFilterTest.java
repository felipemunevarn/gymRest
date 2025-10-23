package com.epam.gym.workload.security;

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
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    // ========== Health/Actuator Endpoint Tests ==========

    @Test
    void testDoFilterInternal_HealthEndpoint_SkipsAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/v1/trainers/health");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(any());
    }

    @Test
    void testDoFilterInternal_ActuatorEndpoint_SkipsAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/actuator/health");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(any());
    }

    // ========== No Token Tests ==========

    @Test
    void testDoFilterInternal_NoAuthorizationHeader_ContinuesWithoutAuth() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/v1/trainers/summary/john.doe");
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(any());
    }

    @Test
    void testDoFilterInternal_AuthorizationHeaderWithoutBearer_ContinuesWithoutAuth() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/v1/trainers/summary/john.doe");
        when(request.getHeader("Authorization")).thenReturn("Basic sometoken");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(any());
    }

    // ========== Valid Token Tests ==========

    @Test
    void testDoFilterInternal_ValidUserToken_SetsAuthentication() throws ServletException, IOException {
        // Arrange
        String token = "validUserToken";
        String username = "john.doe";

        when(request.getRequestURI()).thenReturn("/api/v1/trainers/summary/john.doe");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(jwtUtil.validateToken(token)).thenReturn(true);
//        when(jwtUtil.extractClaim(eq(token), any(Function.class))).thenReturn(null); // No service flag

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(username, auth.getPrincipal());
        assertTrue(auth.getAuthorities().isEmpty());
        verify(filterChain).doFilter(request, response);
    }

//    @Test
//    void testDoFilterInternal_ValidServiceToken_SetsServiceAuthentication() throws ServletException, IOException {
//        // Arrange
//        String token = "validServiceToken";
//        String serviceName = "main-service";
//
//        when(request.getRequestURI()).thenReturn("/api/v1/trainers/workload");
//        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
//        when(jwtUtil.extractUsername(token)).thenReturn(serviceName);
//        when(jwtUtil.validateToken(token)).thenReturn(true);
//
//        // First call returns "true" as String (service flag)
//        when(jwtUtil.extractClaim(eq(token), any(Function.class)))
//                .thenReturn("true");
//
//        // Act
//        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
//
//        // Assert
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        assertNotNull(auth);
//        assertEquals(serviceName, auth.getPrincipal());
//        assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SERVICE")));
//        verify(filterChain).doFilter(request, response);
//    }

//    @Test
//    void testDoFilterInternal_ValidServiceTokenBooleanFlag_SetsServiceAuthentication() throws ServletException, IOException {
//        // Arrange
//        String token = "validServiceToken";
//        String serviceName = "main-service";
//
//        when(request.getRequestURI()).thenReturn("/api/v1/trainers/workload");
//        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
//        when(jwtUtil.extractUsername(token)).thenReturn(serviceName);
//        when(jwtUtil.validateToken(token)).thenReturn(true);
//
//        // First call returns null, second call returns Boolean TRUE
//        when(jwtUtil.extractClaim(eq(token), any(Function.class)))
//                .thenReturn(null)
//                .thenReturn(Boolean.TRUE);
//
//        // Act
//        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
//
//        // Assert
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        assertNotNull(auth);
//        assertEquals(serviceName, auth.getPrincipal());
//        assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SERVICE")));
//        verify(filterChain).doFilter(request, response);
//    }

    // ========== Invalid Token Tests ==========

    @Test
    void testDoFilterInternal_InvalidToken_NoAuthenticationSet() throws ServletException, IOException {
        // Arrange
        String token = "invalidToken";
        String username = "john.doe";

        when(request.getRequestURI()).thenReturn("/api/v1/trainers/summary/john.doe");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(jwtUtil.validateToken(token)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ExceptionExtractingUsername_ContinuesWithoutAuth() throws ServletException, IOException {
        // Arrange
        String token = "malformedToken";

        when(request.getRequestURI()).thenReturn("/api/v1/trainers/summary/john.doe");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenThrow(new RuntimeException("Invalid JWT"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    // ========== Already Authenticated Tests ==========

    @Test
    void testDoFilterInternal_AlreadyAuthenticated_SkipsValidation() throws ServletException, IOException {
        // Arrange
        String token = "validToken";
        String username = "john.doe";

        // Set up existing authentication
        Authentication existingAuth = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getRequestURI()).thenReturn("/api/v1/trainers/summary/john.doe");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(username);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertEquals(existingAuth, SecurityContextHolder.getContext().getAuthentication());
        verify(jwtUtil, never()).validateToken(any());
        verify(filterChain).doFilter(request, response);
    }

    // ========== Exception Handling in Service Claim Tests ==========

//    @Test
//    void testDoFilterInternal_ExceptionExtractingServiceClaim_FallsBackToUserAuth() throws ServletException, IOException {
//        // Arrange
//        String token = "validToken";
//        String username = "john.doe";
//
//        when(request.getRequestURI()).thenReturn("/api/v1/trainers/summary/john.doe");
//        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
//        when(jwtUtil.extractUsername(token)).thenReturn(username);
//        when(jwtUtil.validateToken(token)).thenReturn(true);
//        when(jwtUtil.extractClaim(eq(token), any(Function.class)))
//                .thenThrow(new RuntimeException("Claim extraction failed"));
//
//        // Act
//        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
//
//        // Assert
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        assertNotNull(auth);
//        assertEquals(username, auth.getPrincipal());
//        assertTrue(auth.getAuthorities().isEmpty()); // Regular user, no service role
//        verify(filterChain).doFilter(request, response);
//    }

    // ========== Edge Cases ==========

    @Test
    void testDoFilterInternal_NullUsername_ContinuesWithoutAuth() throws ServletException, IOException {
        // Arrange
        String token = "validToken";

        when(request.getRequestURI()).thenReturn("/api/v1/trainers/summary/john.doe");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.extractUsername(token)).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtUtil, never()).validateToken(any());
        verify(filterChain).doFilter(request, response);
    }

//    @Test
//    void testDoFilterInternal_ServiceFlagFalseString_TreatsAsUserToken() throws ServletException, IOException {
//        // Arrange
//        String token = "validToken";
//        String username = "john.doe";
//
//        when(request.getRequestURI()).thenReturn("/api/v1/trainers/summary/john.doe");
//        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
//        when(jwtUtil.extractUsername(token)).thenReturn(username);
//        when(jwtUtil.validateToken(token)).thenReturn(true);
//        when(jwtUtil.extractClaim(eq(token), any(Function.class)))
//                .thenReturn("false"); // Service flag is "false"
//
//        // Act
//        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
//
//        // Assert
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        assertNotNull(auth);
//        assertEquals(username, auth.getPrincipal());
//        assertTrue(auth.getAuthorities().isEmpty()); // No service role
//        verify(filterChain).doFilter(request, response);
//    }
//
//    @Test
//    void testDoFilterInternal_ServiceFlagBooleanFalse_TreatsAsUserToken() throws ServletException, IOException {
//        // Arrange
//        String token = "validToken";
//        String username = "john.doe";
//
//        when(request.getRequestURI()).thenReturn("/api/v1/trainers/summary/john.doe");
//        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
//        when(jwtUtil.extractUsername(token)).thenReturn(username);
//        when(jwtUtil.validateToken(token)).thenReturn(true);
//        when(jwtUtil.extractClaim(eq(token), any(Function.class)))
//                .thenReturn(null)
//                .thenReturn(Boolean.FALSE); // Service flag is Boolean FALSE
//
//        // Act
//        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
//
//        // Assert
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        assertNotNull(auth);
//        assertEquals(username, auth.getPrincipal());
//        assertTrue(auth.getAuthorities().isEmpty()); // No service role
//        verify(filterChain).doFilter(request, response);
//    }
}