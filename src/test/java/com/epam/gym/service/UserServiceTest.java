package com.epam.gym.service;

import com.epam.gym.entity.User;
import com.epam.gym.repository.UserRepository;
import com.epam.gym.util.UsernamePasswordUtil;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UsernamePasswordUtil usernamePasswordUtil;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder()
                .username("john.doe")
                .password("hashedPass")
                .isActive(true)
                .build();
    }

    @Test
    void testFindByUsername_userExists() {
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByUsername("john.doe");

        assertTrue(result.isPresent());
        assertEquals("john.doe", result.get().getUsername());
    }

    @Test
    void testAuthenticate_successful() {
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(usernamePasswordUtil.checkPassword("plainPass", "hashedPass")).thenReturn(true);

        assertTrue(userService.authenticate("john.doe", "plainPass"));
    }

    @Test
    void testAuthenticate_wrongPassword() {
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(usernamePasswordUtil.checkPassword("wrongPass", "hashedPass")).thenReturn(false);

        assertFalse(userService.authenticate("john.doe", "wrongPass"));
    }

    @Test
    void testAuthenticate_userInactive() {
        user = user.toBuilder().isActive(false).build();
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));

        assertFalse(userService.authenticate("john.doe", "plainPass"));
    }

    @Test
    void testAuthenticate_userNotFound() {
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.empty());

        assertFalse(userService.authenticate("john.doe", "plainPass"));
    }

    @Test
    void testChangePassword_success() {
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(usernamePasswordUtil.checkPassword("oldPass",
                "hashedPass")).thenReturn(true);
        when(usernamePasswordUtil.hashPassword("newPass")).thenReturn("newHashedPass");

        userService.changePassword("john.doe", "oldPass", "newPass");

        verify(userRepository).save(argThat(savedUser ->
                savedUser.getUsername().equals("john.doe") &&
                        savedUser.getPassword().equals("newHashedPass")
        ));
    }

    @Test
    void testChangePasswordFailsWhenNewPasswordIsNull() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(usernamePasswordUtil.checkPassword("oldPass",
                "hashedOldPass")).thenReturn(true);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        SecurityException thrown = assertThrows(SecurityException.class,
                () -> userService.changePassword("john",
                        "wrongOldPass",
                        null));
        assertEquals("Authentication failed for password change.", thrown.getMessage());
    }

    @Test
    void testChangePasswordFailsWhenNewPasswordIsBlank() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(usernamePasswordUtil.checkPassword("oldPass",
                "hashedOldPass")).thenReturn(true);

        // Simulate finding user again (as in the method)
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        assertThrows(SecurityException.class,
                () -> userService.changePassword("john",
                        "oldPass",
                        "  "));
    }


    @Test
    void testChangePassword_authenticationFails() {
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(usernamePasswordUtil.checkPassword("wrongOldPass",
                "hashedPass")).thenReturn(false);

        assertThrows(SecurityException.class, () ->
                userService.changePassword("john.doe",
                        "wrongOldPass",
                        "newPass")
        );
    }

    @Test
    void testChangePassword_newPasswordBlank() {
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(usernamePasswordUtil.checkPassword("oldPass", "hashedPass")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                userService.changePassword("john.doe", "oldPass", " ")
        );
    }

    @Test
    void testChangePassword_userNotFoundAfterAuth() {
        when(userRepository.findByUsername("john.doe"))
                .thenReturn(Optional.of(user))
                .thenReturn(Optional.empty());
        when(usernamePasswordUtil.checkPassword("oldPass", "hashedPass")).thenReturn(true);

        assertThrows(NoResultException.class, () ->
                userService.changePassword("john.doe", "oldPass", "newPass")
        );
    }
}
