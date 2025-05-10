package com.epam.gym.controller;

import com.epam.gym.dto.ChangePasswordRequest;
import com.epam.gym.dto.LoginRequest;
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

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestBody LoginRequest request
            ) {
        boolean isAuthenticated = authService.authenticate(request.username(),
                request.password());
        if (isAuthenticated) {
            return ResponseEntity.ok(tokenService.generateToken(request.username()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestBody @Valid ChangePasswordRequest request
    ) {
        authService.changePassword(request.username(),
                request.oldPassword(),
                request.newPassword());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

//    @PostMapping("/logout")
//    public String logout(@RequestHeader("X-Auth-Token") String token) {
//        tokenService.invalidateToken(token);
//        return "Logged out!";
//    }

    @GetMapping("/validate")
    public String validateToken(@RequestHeader("X-Auth-Token") String token) {
        if (tokenService.isValidToken(token)) {
            return "Token valid for user: " + tokenService.getUsername(token);
        } else {
            return "Invalid token";
        }
    }
}
