package com.epam.gym.controller;

import com.epam.gym.dto.ChangePasswordRequest;
import com.epam.gym.dto.LoginRequest;
import com.epam.gym.dto.MessageResponse;
import com.epam.gym.entity.User;
import com.epam.gym.exception.InvalidTokenException;
import com.epam.gym.exception.LockedException;
import com.epam.gym.repository.UserRepository;
import com.epam.gym.security.util.JwtUtil;
import com.epam.gym.service.AuthService;
import com.epam.gym.service.LoginAttemptService;
import com.epam.gym.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final LoginAttemptService loginAttemptService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserRepository userRepository;

    @Autowired
    public AuthController(AuthService authService,
                          JwtUtil jwtUtil,
                          AuthenticationManager authenticationManager,
                          LoginAttemptService loginAttemptService,
                          TokenBlacklistService tokenBlacklistService,
                          UserRepository userRepository
    ) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.loginAttemptService = loginAttemptService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.userRepository = userRepository;
    }

    /**
     * Authenticates a user and generates a token on successful login.
     *
     * @param request The login request body containing username and password.
     * @return ResponseEntity with the token string and HTTP status OK on success,
     * or HTTP status UNAUTHORIZED on failure.
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {
        String username = request.username();

        if (loginAttemptService.isBlocked(username)) {

            userRepository.findByUsername(username).ifPresent(user -> {
                if (user.isActive()) {
                    User updated = user.toBuilder()
                            .isActive(false)
                            .build();
                    userRepository.save(updated);
                    throw new LockedException("Too many failed attempts. Account locked.");
                } else {

                    if (loginAttemptService.isLockExpired(username)) {
                        User unlocked = user.toBuilder()
                                .isActive(true)
                                .build();
                        userRepository.save(unlocked);
                        loginAttemptService.reset(username); // clear lock
                    } else {
                        throw new LockedException("Too many failed attempts. Account locked.");
                    }
                }
            });
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.password())
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            if (!user.isActive()) {
                throw new DisabledException("User account is disabled.");
            }

            loginAttemptService.loginSucceeded(username);
            String token = jwtUtil.generateToken(username, request.password());
            return ResponseEntity.ok(token);

        } catch (BadCredentialsException e) {
            loginAttemptService.loginFailed(username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        } catch (LockedException | DisabledException e) {
            return ResponseEntity.status(HttpStatus.LOCKED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error");
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
     * @return ResponseEntity with HTTP status NO_CONTENT on success.
     * @throws InvalidTokenException if the token is invalid (handled by global exception handler).
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            // Extract token from Authorization header
            String token = extractTokenFromRequest(request);

            if (token == null) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("No token provided"));
            }
            String username = jwtUtil.extractUsername(token);
            // Validate token before blacklisting (optional but recommended)
            if (!jwtUtil.isValid(token, username)) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Invalid token"));
            }

            // Blacklist the token
            tokenBlacklistService.blacklistToken(token);

            return ResponseEntity.ok()
                    .body(new MessageResponse("Successfully logged out"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Logout failed: " + e.getMessage()));
        }
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // Remove "Bearer " prefix
        }

        return null;
    }
}
