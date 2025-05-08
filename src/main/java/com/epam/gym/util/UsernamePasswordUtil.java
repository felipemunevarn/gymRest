package com.epam.gym.util;

import com.epam.gym.repository.UserRepository;
import com.epam.gym.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility component for generating usernames and passwords,
 * and for handling password hashing and checking (using PasswordEncoder).
 */
@Component
public class UsernamePasswordUtil {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    // Uncomment and Autowire if using PasswordEncoder
    // private final PasswordEncoder passwordEncoder;

    private static final SecureRandom random = new SecureRandom();
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 10; // Length for generated passwords

    /**
     * Constructor injection for dependencies.
     * @param userDao DAO to check for username existence.
     * //@param passwordEncoder Encoder for hashing/checking passwords (Uncomment if used).
     */
    @Autowired
    public UsernamePasswordUtil(UserRepository userRepository /*, PasswordEncoder passwordEncoder */) {
        this.userRepository = userRepository;
        // this.passwordEncoder = passwordEncoder; // Uncomment if used
    }

    /**
     * Generates a unique username based on first and last name.
     * Appends a serial number if the base username already exists.
     * Example: John.Smith, John.Smith1, John.Smith2, ...
     * @param firstName User's first name.
     * @param lastName User's last name.
     * @return A unique username string.
     */
    public String generateUsername(String firstName, String lastName) {
        String baseUsername = (firstName + "." + lastName).toLowerCase();
        String finalUsername = baseUsername;
        int serial = 1;
        // Loop to find a unique username by appending a serial number
        while (userRepository.existsByUsername(finalUsername)) {
            finalUsername = baseUsername + serial;
            serial++;
        }
        return finalUsername;
    }

    /**
     * Generates a random password of predefined length.
     * @return A randomly generated password string.
     */
    public String generatePassword() {
        return IntStream.range(0, PASSWORD_LENGTH)
                .map(i -> random.nextInt(CHARACTERS.length()))
                .mapToObj(randomIndex -> String.valueOf(CHARACTERS.charAt(randomIndex)))
                .collect(Collectors.joining());
    }

    /**
     * Hashes a plain text password using the configured PasswordEncoder.
     * IMPORTANT: Requires a PasswordEncoder bean to be configured and injected.
     * @param plainPassword The plain text password.
     * @return The hashed password string.
     */
    public String hashPassword(String plainPassword) {
        // Replace with actual hashing using injected passwordEncoder
        log.warn("WARNING: Password hashing not properly implemented! Returning plain text.");
        // return passwordEncoder.encode(plainPassword); // Uncomment and use when PasswordEncoder is configured
        return plainPassword; // Placeholder - insecure!
    }

    /**
     * Checks if a raw password matches a stored encoded password using the PasswordEncoder.
     * IMPORTANT: Requires a PasswordEncoder bean to be configured and injected.
     * @param rawPassword The raw password entered by the user.
     * @param encodedPassword The stored hashed password.
     * @return true if the passwords match, false otherwise.
     */
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        // Replace with actual checking using injected passwordEncoder
        log.warn("WARNING: Password checking not properly implemented!");
        // return passwordEncoder.matches(rawPassword, encodedPassword); // Uncomment and use when PasswordEncoder is configured
        return rawPassword.equals(encodedPassword); // Placeholder - insecure!
    }
}

