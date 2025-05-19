package com.epam.gym.controller;

import com.epam.gym.dto.ChangePasswordRequest;
import com.epam.gym.dto.LoginRequest;
import com.epam.gym.dto.TokenValidationResponse;
import com.epam.gym.service.AuthService;
import com.epam.gym.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_ValidCredentials_ShouldReturnToken() {
        LoginRequest request = new LoginRequest("user", "pass");
        when(authService.authenticate("user", "pass")).thenReturn(true);
        when(tokenService.generateToken("user")).thenReturn("mockToken");

        ResponseEntity<String> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("mockToken", response.getBody());
    }

    @Test
    void login_InvalidCredentials_ShouldReturnUnauthorized() {
        LoginRequest request = new LoginRequest("user", "wrong");
        when(authService.authenticate("user", "wrong")).thenReturn(false);

        ResponseEntity<String> response = authController.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void changePassword_ShouldReturnNoContent() {
        ChangePasswordRequest request = new ChangePasswordRequest("user", "old", "new");

        ResponseEntity<Void> response = authController.changePassword(request);

        verify(authService).changePassword("user", "old", "new");
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void logout_ShouldInvalidateToken() {
        String token = "validToken";

        ResponseEntity<Void> response = authController.logout(token);

        verify(tokenService).invalidateToken(token);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void validateToken_ValidToken_ShouldReturnOk() {
        String token = "validToken";
        when(tokenService.isValidToken(token)).thenReturn(true);
        when(tokenService.getUsername(token)).thenReturn("user");

        ResponseEntity<TokenValidationResponse> response = authController.validateToken(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().valid());
        assertEquals("user", response.getBody().username());
    }

    @Test
    void validateToken_InvalidToken_ShouldReturnUnauthorized() {
        String token = "invalidToken";
        when(tokenService.isValidToken(token)).thenReturn(false);

        ResponseEntity<TokenValidationResponse> response = authController.validateToken(token);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody().valid());
        assertNull(response.getBody().username());
    }
}
