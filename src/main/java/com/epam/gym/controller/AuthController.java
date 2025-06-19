package com.epam.gym.controller;

import com.epam.gym.dto.ChangePasswordRequest;
import com.epam.gym.dto.LoginRequest;
import com.epam.gym.dto.TokenValidationResponse;
import com.epam.gym.exception.InvalidTokenException;
import com.epam.gym.security.util.JwtUtil;
import com.epam.gym.service.AuthService;
import com.epam.gym.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(AuthService authService,
                          JwtUtil jwtUtil,
                          TokenService tokenService,
                          AuthenticationManager authenticationManager
    ) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
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
            @Valid @RequestBody LoginRequest request
    ) {
        try {
            // Use Spring Security's AuthenticationManager directly for authentication
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    ));

            SecurityContextHolder.getContext().setAuthentication(auth);

            // Generate JWT token
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String token = jwtUtil.generateToken(userDetails.getUsername(), request.password());
            return ResponseEntity.ok(token);

        } catch (Exception e) {
            // Authentication failed
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
            @RequestBody @Valid ChangePasswordRequest request
    ) {

        authService.changePassword(request.username(),
                request.oldPassword(),
                request.newPassword());
        return ResponseEntity.noContent().build();
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
        tokenService.invalidateToken(token);
        return ResponseEntity.noContent().build();
    }

//    /**
//     * Validates the provided token and returns its status and associated username if valid.
//     *
//     * @param token The authentication token to validate.
//     * @return ResponseEntity with TokenValidationResponse and HTTP status OK if valid,
//     * or HTTP status UNAUTHORIZED if invalid.
//     */
    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestHeader("X-Auth-Token") String token) {
        if (tokenService.isValidToken(tokenService.getUsername(token),token)) {
            String username = tokenService.getUsername(token);
            TokenValidationResponse response = new TokenValidationResponse(true, username);
            return ResponseEntity.ok(response);
        } else {
            TokenValidationResponse response = new TokenValidationResponse(false, null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // Return UNAUTHORIZED for invalid
        }
    }
}
