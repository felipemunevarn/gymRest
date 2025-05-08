package com.epam.gym.service;

import com.epam.gym.entity.User;
import com.epam.gym.repository.UserRepository;
import com.epam.gym.util.UsernamePasswordUtil;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UsernamePasswordUtil usernamePasswordUtil;
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository, UsernamePasswordUtil usernamePasswordUtil) {
        this.userRepository = userRepository;
        this.usernamePasswordUtil = usernamePasswordUtil;
    }

    @Transactional
    public User createUser(String firstName, String lastName) {
        String newUsername = usernamePasswordUtil.generateUsername(firstName, lastName);
        String newPassword = usernamePasswordUtil.generatePassword();
        User user = User.builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .username(newUsername)
                        .password(newPassword)
                        .isActive(true)
                        .build();

        log.info("Creating user: {}", newUsername);
        try {
            userRepository.save(user);
            log.debug("User saved with ID: {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to save user: {}", e.getMessage(), e);
        }
        return user;
    }

    @Transactional
    public Optional<User> findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    @Transactional
    public boolean authenticate(String username, String password) {
        log.info("Authenticating user: {}", username);
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && userOpt.get().isActive()) {
            User user = userOpt.get();
            boolean matches = usernamePasswordUtil.checkPassword(password, user.getPassword());
            if(matches) {
                log.info("Authentication successful for user: {}", username);
                return true;
            } else {
                log.warn("Authentication failed for user: {} - Incorrect password", username);
                return false;
            }
        }
        log.warn("Authentication failed for user: {} - User not found or inactive", username);
        return false;
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        log.info("Attempting to change password for trainee: {}", username);

        if (!authenticate(username, oldPassword)) {
            log.error("Password change failed for {}: Authentication failed (old password incorrect or user inactive/not found).", username);
            throw new SecurityException("Authentication failed for password change.");
        }

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> {
                log.error("Password change failed: User {} not found after successful authentication.", username); // Should not happen
                return new NoResultException("User not found: " + username);
            });

        if (newPassword == null || newPassword.isBlank()) {
            log.error("Password change failed for {}: New password cannot be empty.", username);
            throw new IllegalArgumentException("New password cannot be empty.");
        }

        user = user.toBuilder()
            .password(usernamePasswordUtil.hashPassword(newPassword))
            .build();

        userRepository.save(user); // Persist the change
        log.info("Password changed successfully for user: {}", username);
    }

    @Transactional
    public void setActiveStatus(String username, boolean isActive) {
        log.info("Setting active status for user {} to {}", username, isActive);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Failed to set active status: User {} not found.", username);
                    return new NoResultException("User not found: " + username);
                });
        User updated = user.toBuilder()
                        .isActive(isActive)
                                .build();
        userRepository.save(updated);
        log.info("Active status updated successfully for user: {}", username);
    }

    @Transactional
    public void updateUser(String username,
                           String firstName,
                           String lastName,
                           boolean isActive) {
        Optional<User> user = userRepository.findByUsername(username);
        User updated = user.get().toBuilder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .isActive(isActive)
                        .build();
        userRepository.save(updated);
        log.info("FirstName and LastName updated successfully for user: {}", username);
    }
}
