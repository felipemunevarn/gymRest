package com.epam.gym.service;

import com.epam.gym.entity.User;
import jakarta.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(
            UserService userService,
            PasswordEncoder passwordEncoder
    ) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean authenticate(String username, String password) {
        Optional<User> optUser = userService.findByUsername(username);
        if (optUser.isEmpty()) {
            throw new NoResultException("User not found");
        }
        return passwordEncoder.matches(password, optUser.get().getPassword());
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new NoResultException("User not found"));

        if (!authenticate(username, oldPassword)) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        userService.changePassword(username, oldPassword, newPassword);
    }
}
