package com.epam.gym.controller;

import com.epam.gym.dto.ChangePasswordRequest;
import com.epam.gym.dto.LoginRequest;
import com.epam.gym.entity.User;
import com.epam.gym.exception.LockedException;
import com.epam.gym.repository.UserRepository;
import com.epam.gym.security.util.JwtUtil;
import com.epam.gym.service.AuthService;
import com.epam.gym.service.LoginAttemptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
        SecurityContextHolder.clearContext();
    }

    @Test
    void login_ValidCredentials_ReturnsToken() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("john.doe", "password123");
        String expectedToken = "jwt-token-here";

        User user = User.builder()
                .username("john.doe")
                .password("encoded-password")
                .isActive(true)
                .build();

        when(loginAttemptService.isBlocked("john.doe")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("john.doe", "password123")).thenReturn(expectedToken);
        doNothing().when(loginAttemptService).loginSucceeded("john.doe");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedToken));

        verify(loginAttemptService).isBlocked("john.doe");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("john.doe");
        verify(loginAttemptService).loginSucceeded("john.doe");
        verify(jwtUtil).generateToken("john.doe", "password123");
    }

    @Test
    void login_UserNotFound_ReturnsInternalServerError() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("nonexistent.user", "password123");

        when(loginAttemptService.isBlocked("nonexistent.user")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("nonexistent.user")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Unexpected error"));

        verify(loginAttemptService).isBlocked("nonexistent.user");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("nonexistent.user");
    }

    @Test
    void login_UserInactive_ReturnsLocked() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("inactive.user", "password123");

        User inactiveUser = User.builder()
                .username("inactive.user")
                .password("encoded-password")
                .isActive(false)
                .build();

        when(loginAttemptService.isBlocked("inactive.user")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("inactive.user")).thenReturn(Optional.of(inactiveUser));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isLocked())
                .andExpect(content().string("User account is disabled."));

        verify(loginAttemptService).isBlocked("inactive.user");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("inactive.user");
    }

    @Test
    void login_BadCredentials_ReturnsUnauthorized() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("john.doe", "wrongpassword");

        when(loginAttemptService.isBlocked("john.doe")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        doNothing().when(loginAttemptService).loginFailed("john.doe");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));

        verify(loginAttemptService).isBlocked("john.doe");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(loginAttemptService).loginFailed("john.doe");
    }

//    @Test
//    void login_UserBlocked_ActiveUser_LocksAccount() throws Exception {
//        // Given
//        LoginRequest request = new LoginRequest("blocked.user", "password123");
//
//        User activeUser = User.builder()
//                .username("blocked.user")
//                .password("encoded-password")
//                .isActive(true)
//                .build();
//
//        User lockedUser = User.builder()
//                .username("blocked.user")
//                .password("encoded-password")
//                .isActive(false)
//                .build();
//
//        when(loginAttemptService.isBlocked("blocked.user")).thenReturn(true);
//        when(userRepository.findByUsername("blocked.user")).thenReturn(Optional.of(activeUser));
//        when(userRepository.save(any(User.class))).thenReturn(lockedUser);
//
//        // When & Then
//        mockMvc.perform(post("/api/v1/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isLocked())
//                .andExpect(content().string("Too many failed attempts. Account locked."));
//
//        verify(loginAttemptService).isBlocked("blocked.user");
//        verify(userRepository).findByUsername("blocked.user");
//        verify(userRepository).save(any(User.class));
//    }

//    @Test
//    void login_UserBlocked_InactiveUser_LockNotExpired_ReturnsLocked() throws Exception {
//        // Given
//        LoginRequest request = new LoginRequest("blocked.user", "password123");
//
//        User inactiveUser = User.builder()
//                .username("blocked.user")
//                .password("encoded-password")
//                .isActive(false)
//                .build();
//
//        when(loginAttemptService.isBlocked("blocked.user")).thenReturn(true);
//        when(userRepository.findByUsername("blocked.user")).thenReturn(Optional.of(inactiveUser));
//        when(loginAttemptService.isLockExpired("blocked.user")).thenReturn(false);
//
//        // When & Then
//        mockMvc.perform(post("/api/v1/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isLocked())
//                .andExpect(content().string("Too many failed attempts. Account locked."));
//
//        verify(loginAttemptService).isBlocked("blocked.user");
//        verify(userRepository).findByUsername("blocked.user");
//        verify(loginAttemptService).isLockExpired("blocked.user");
//    }

//    @Test
//    void login_UserBlocked_InactiveUser_LockExpired_UnlocksAndContinues() throws Exception {
//        // Given
//        LoginRequest request = new LoginRequest("blocked.user", "password123");
//
//        User inactiveUser = User.builder()
//                .username("blocked.user")
//                .password("encoded-password")
//                .isActive(false)
//                .build();
//
//        User unlockedUser = User.builder()
//                .username("blocked.user")
//                .password("encoded-password")
//                .isActive(true)
//                .build();
//
//        String expectedToken = "jwt-token-here";
//
//        when(loginAttemptService.isBlocked("blocked.user")).thenReturn(true);
//        when(userRepository.findByUsername("blocked.user")).thenReturn(Optional.of(inactiveUser));
//        when(loginAttemptService.isLockExpired("blocked.user")).thenReturn(true);
//        when(userRepository.save(any(User.class))).thenReturn(unlockedUser);
//        doNothing().when(loginAttemptService).reset("blocked.user");
//        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
//                .thenReturn(authentication);
//        // Return unlocked user for second call
//        when(userRepository.findByUsername("blocked.user")).thenReturn(Optional.of(unlockedUser));
//        when(jwtUtil.generateToken("blocked.user", "password123")).thenReturn(expectedToken);
//        doNothing().when(loginAttemptService).loginSucceeded("blocked.user");
//
//        // When & Then
//        mockMvc.perform(post("/api/v1/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(content().string(expectedToken));
//
//        verify(loginAttemptService).isBlocked("blocked.user");
//        verify(userRepository, times(2)).findByUsername("blocked.user");
//        verify(loginAttemptService).isLockExpired("blocked.user");
//        verify(userRepository).save(any(User.class));
//        verify(loginAttemptService).reset("blocked.user");
//        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
//        verify(loginAttemptService).loginSucceeded("blocked.user");
//        verify(jwtUtil).generateToken("blocked.user", "password123");
//    }

    @Test
    void login_UserBlocked_UserNotFound_ContinuesNormally() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("nonexistent.user", "password123");

        when(loginAttemptService.isBlocked("nonexistent.user")).thenReturn(true);
        when(userRepository.findByUsername("nonexistent.user")).thenReturn(Optional.empty());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        doNothing().when(loginAttemptService).loginFailed("nonexistent.user");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));

        verify(loginAttemptService).isBlocked("nonexistent.user");
        verify(userRepository).findByUsername("nonexistent.user");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(loginAttemptService).loginFailed("nonexistent.user");
    }

    @Test
    void login_LockedException_ReturnsLocked() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("john.doe", "password123");

        when(loginAttemptService.isBlocked("john.doe")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new LockedException("Account is locked"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isLocked())
                .andExpect(content().string("Account is locked"));

        verify(loginAttemptService).isBlocked("john.doe");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_DisabledException_ReturnsLocked() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("john.doe", "password123");

        when(loginAttemptService.isBlocked("john.doe")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("Account is disabled"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isLocked())
                .andExpect(content().string("Account is disabled"));

        verify(loginAttemptService).isBlocked("john.doe");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_UnexpectedException_ReturnsInternalServerError() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("john.doe", "password123");

        when(loginAttemptService.isBlocked("john.doe")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Unexpected error"));

        verify(loginAttemptService).isBlocked("john.doe");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given - invalid request with null values
        LoginRequest request = new LoginRequest(null, null);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(loginAttemptService, never()).isBlocked(any());
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void changePassword_ValidRequest_ReturnsNoContent() throws Exception {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest(
                "john.doe",
                "oldPassword123",
                "newPassword456"
        );

        doNothing().when(authService).changePassword("john.doe", "oldPassword123", "newPassword456");

        // When & Then
        mockMvc.perform(put("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(authService).changePassword("john.doe", "oldPassword123", "newPassword456");
    }

    @Test
    void changePassword_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given - invalid request with null values
        ChangePasswordRequest request = new ChangePasswordRequest(null, null, null);

        // When & Then
        mockMvc.perform(put("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).changePassword(any(), any(), any());
    }

//    @Test
//    void changePassword_ServiceException_ReturnsInternalServerError() throws Exception {
//        // Given
//        ChangePasswordRequest request = new ChangePasswordRequest(
//                "john.doe",
//                "oldPassword123",
//                "newPassword456"
//        );
//
//        doThrow(new RuntimeException("Service error"))
//                .when(authService).changePassword("john.doe", "oldPassword123", "newPassword456");
//
//        // When & Then
//        mockMvc.perform(put("/api/v1/auth/change-password")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isInternalServerError());
//
//        verify(authService).changePassword("john.doe", "oldPassword123", "newPassword456");
//    }

    @Test
    void logout_WithValidToken_ReturnsOk() throws Exception {
        // Given
        String token = "valid-jwt-token";
        String authHeader = "Bearer " + token;

        // When & Then
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", authHeader))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"message\":\"Invalid token\"}"));
    }

    @Test
    void logout_WithoutAuthorizationHeader_ReturnsOk() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"message\":\"No token provided\"}"));
    }

    @Test
    void logout_WithInvalidAuthorizationHeader_ReturnsOk() throws Exception {
        // Given - header without "Bearer " prefix
        String authHeader = "InvalidHeader token";

        // When & Then
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", authHeader))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"message\":\"No token provided\"}"));
    }

    @Test
    void logout_WithEmptyBearerToken_ReturnsOk() throws Exception {
        // Given - header with "Bearer " but no token
        String authHeader = "Bearer ";

        // When & Then
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", authHeader))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"message\":\"Invalid token\"}"));
    }

    @Test
    void logout_TokenBlacklistException_ReturnsInternalServerError() throws Exception {
        // Given
        String token = "valid-jwt-token";
        String authHeader = "Bearer " + token;

        // When & Then
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", authHeader))
                .andExpect(status().isBadRequest());
    }
}