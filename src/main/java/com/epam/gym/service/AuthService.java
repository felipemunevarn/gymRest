package com.epam.gym.service;

import com.epam.gym.entity.User;
import com.epam.gym.repository.UserRepository;
import jakarta.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserService userService;

    @Autowired
    public AuthService(UserService userService) {
        this.userService = userService;
    }

    public boolean authenticate(String username, String password) {
        Optional<User> optUser = userService.findByUsername(username);
        if (optUser.isEmpty()) {
            throw new NoResultException("User not found");
        }
        return optUser.get().getPassword().equals(password);
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
