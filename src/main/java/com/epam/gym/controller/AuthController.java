package com.epam.gym.controller;

import com.epam.gym.dto.ChangePasswordRequest;
import com.epam.gym.dto.LoginRequest;
import com.epam.gym.dto.TokenValidationResponse;
import com.epam.gym.exception.InvalidTokenException;
import com.epam.gym.service.AuthService;
import com.epam.gym.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    @Autowired
    public AuthController(AuthService authService,
                          TokenService tokenService
    ) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    /**
     * Authenticates a user and generates a token on successful login.
     *
     * @param request The login request body containing username and password.
     * @return ResponseEntity with the token string and HTTP status OK on success,
     * or HTTP status UNAUTHORIZED on failure.
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(
            @Valid @RequestBody LoginRequest request // Added @Valid for input validation
    ) {
        boolean isAuthenticated = authService.authenticate(request.username(),
                request.password());
        if (isAuthenticated) {
            // Assuming generateToken returns the token string
            String token = tokenService.generateToken(request.username());
            return ResponseEntity.ok(token);
        } else {
            // Return UNAUTHORIZED status for failed authentication
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Changes the password for an authenticated user. Requires old password for verification.
     *
     * @param request The change password request body.
     * @return ResponseEntity with HTTP status NO_CONTENT on success.
     * Requires global exception handling for authentication failures (401),
     * invalid new password (400), etc.
     */
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestBody @Valid ChangePasswordRequest request // @Valid for input validation
    ) {
        // Delegate password change logic to AuthService
        authService.changePassword(request.username(),
                request.oldPassword(),
                request.newPassword());
        // Return NO_CONTENT status for a successful update with no response body
        return ResponseEntity.noContent().build(); // Changed status to NO_CONTENT
    }

    /**
     * Invalidates the provided token, effectively logging out the user.
     *
     * @param token The authentication token to invalidate.
     * @return ResponseEntity with HTTP status NO_CONTENT on success.
     * @throws InvalidTokenException if the token is invalid (handled by global exception handler).
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("X-Auth-Token") String token) {
        // Invalidate the token using TokenService
        tokenService.invalidateToken(token);
        // Return NO_CONTENT status for successful logout
        return ResponseEntity.noContent().build(); // Changed to return ResponseEntity
    }

    /**
     * Validates the provided token and returns its status and associated username if valid.
     *
     * @param token The authentication token to validate.
     * @return ResponseEntity with TokenValidationResponse and HTTP status OK if valid,
     * or HTTP status UNAUTHORIZED if invalid.
     */
    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestHeader("X-Auth-Token") String token) {
        if (tokenService.isValidToken(token)) {
            // Assuming getUsername is available in TokenService to retrieve the username from the token
            String username = tokenService.getUsername(token);
            // Create a success response DTO
            TokenValidationResponse response = new TokenValidationResponse(true, username);
            // Return OK status with the structured response
            return ResponseEntity.ok(response);
        } else {
            // Create a failure response DTO
            TokenValidationResponse response = new TokenValidationResponse(false, null);
            // Return UNAUTHORIZED status for invalid token
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // Return UNAUTHORIZED for invalid
        }
    }
}
