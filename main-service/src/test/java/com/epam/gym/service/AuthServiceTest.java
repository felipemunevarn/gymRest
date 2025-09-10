package com.epam.gym.service;

import com.epam.gym.entity.User;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    private static final String TEST_USERNAME = "testuser";
    private static final String ANOTHER_USERNAME = "anotheruser";
    private static final String TEST_PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "$2a$10$encoded.password.hash";
    private static final String OLD_PASSWORD = "oldpass123";
    private static final String NEW_PASSWORD = "newpass456";
    private static final String WRONG_PASSWORD = "wrongpass";

    @BeforeEach
    void setUp() {
        authService = new AuthService(userService, passwordEncoder);
    }

    @Test
    @DisplayName("Should create AuthService with required dependencies")
    void constructor_WithValidDependencies_ShouldCreateInstance() {
        // When
        AuthService service = new AuthService(userService, passwordEncoder);

        // Then
        assertNotNull(service);
    }

    @Test
    @DisplayName("Should authenticate user with correct credentials")
    void authenticate_ValidCredentials_ShouldReturnTrue() {
        // Given
        User user = createTestUser();
        when(userService.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        // When
        boolean result = authService.authenticate(TEST_USERNAME, TEST_PASSWORD);

        // Then
        assertTrue(result);
        verify(userService).findByUsername(TEST_USERNAME);
        verify(passwordEncoder).matches(TEST_PASSWORD, ENCODED_PASSWORD);
    }

    @Test
    @DisplayName("Should fail authentication with incorrect password")
    void authenticate_InvalidPassword_ShouldReturnFalse() {
        // Given
        User user = createTestUser();
        when(userService.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(WRONG_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        // When
        boolean result = authService.authenticate(TEST_USERNAME, WRONG_PASSWORD);

        // Then
        assertFalse(result);
        verify(userService).findByUsername(TEST_USERNAME);
        verify(passwordEncoder).matches(WRONG_PASSWORD, ENCODED_PASSWORD);
    }

    @Test
    @DisplayName("Should throw NoResultException when user not found")
    void authenticate_UserNotFound_ShouldThrowNoResultException() {
        // Given
        when(userService.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

        // When & Then
        NoResultException exception = assertThrows(
                NoResultException.class,
                () -> authService.authenticate(TEST_USERNAME, TEST_PASSWORD)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userService).findByUsername(TEST_USERNAME);
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("Should authenticate with null password")
    void authenticate_NullPassword_ShouldCallPasswordEncoder() {
        // Given
        User user = createTestUser();
        when(userService.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(null, ENCODED_PASSWORD)).thenReturn(false);

        // When
        boolean result = authService.authenticate(TEST_USERNAME, null);

        // Then
        assertFalse(result);
        verify(userService).findByUsername(TEST_USERNAME);
        verify(passwordEncoder).matches(null, ENCODED_PASSWORD);
    }

    @Test
    @DisplayName("Should authenticate with empty password")
    void authenticate_EmptyPassword_ShouldCallPasswordEncoder() {
        // Given
        User user = createTestUser();
        when(userService.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("", ENCODED_PASSWORD)).thenReturn(false);

        // When
        boolean result = authService.authenticate(TEST_USERNAME, "");

        // Then
        assertFalse(result);
        verify(userService).findByUsername(TEST_USERNAME);
        verify(passwordEncoder).matches("", ENCODED_PASSWORD);
    }

    @Test
    @DisplayName("Should change password with valid credentials")
    void changePassword_ValidCredentials_ShouldChangePassword() {
        // Given
        User user = createTestUser();
        when(userService.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(OLD_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        // When
        authService.changePassword(TEST_USERNAME, OLD_PASSWORD, NEW_PASSWORD);

        // Then
        verify(userService, times(2)).findByUsername(TEST_USERNAME); // Once for finding user, once for authentication
        verify(passwordEncoder).matches(OLD_PASSWORD, ENCODED_PASSWORD);
        verify(userService).changePassword(TEST_USERNAME, OLD_PASSWORD, NEW_PASSWORD);
    }

    @Test
    @DisplayName("Should throw NoResultException when user not found during password change")
    void changePassword_UserNotFound_ShouldThrowNoResultException() {
        // Given
        when(userService.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

        // When & Then
        NoResultException exception = assertThrows(
                NoResultException.class,
                () -> authService.changePassword(TEST_USERNAME, OLD_PASSWORD, NEW_PASSWORD)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userService).findByUsername(TEST_USERNAME);
        verify(passwordEncoder, never()).matches(any(), any());
        verify(userService, never()).changePassword(any(), any(), any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when old password is incorrect")
    void changePassword_IncorrectOldPassword_ShouldThrowIllegalArgumentException() {
        // Given
        User user = createTestUser();
        when(userService.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(WRONG_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.changePassword(TEST_USERNAME, WRONG_PASSWORD, NEW_PASSWORD)
        );

        assertEquals("Old password is incorrect", exception.getMessage());
        verify(userService, times(2)).findByUsername(TEST_USERNAME);
        verify(passwordEncoder).matches(WRONG_PASSWORD, ENCODED_PASSWORD);
        verify(userService, never()).changePassword(any(), any(), any());
    }

    @Test
    @DisplayName("Should handle user not found during authentication within changePassword")
    void changePassword_UserNotFoundDuringAuthentication_ShouldThrowNoResultException() {
        // Given
        User user = createTestUser();
        // First call returns user, second call (during authentication) returns empty
        when(userService.findByUsername(TEST_USERNAME))
                .thenReturn(Optional.of(user))
                .thenReturn(Optional.empty());

        // When & Then
        NoResultException exception = assertThrows(
                NoResultException.class,
                () -> authService.changePassword(TEST_USERNAME, OLD_PASSWORD, NEW_PASSWORD)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userService, times(2)).findByUsername(TEST_USERNAME);
        verify(passwordEncoder, never()).matches(any(), any());
        verify(userService, never()).changePassword(any(), any(), any());
    }

    @Test
    @DisplayName("Should change password with null old password")
    void changePassword_NullOldPassword_ShouldCallAuthenticate() {
        // Given
        User user = createTestUser();
        when(userService.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(null, ENCODED_PASSWORD)).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.changePassword(TEST_USERNAME, null, NEW_PASSWORD)
        );

        assertEquals("Old password is incorrect", exception.getMessage());
        verify(userService, times(2)).findByUsername(TEST_USERNAME);
        verify(passwordEncoder).matches(null, ENCODED_PASSWORD);
        verify(userService, never()).changePassword(any(), any(), any());
    }

    @Test
    @DisplayName("Should change password with null new password")
    void changePassword_NullNewPassword_ShouldCallUserService() {
        // Given
        User user = createTestUser();
        when(userService.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(OLD_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        // When
        authService.changePassword(TEST_USERNAME, OLD_PASSWORD, null);

        // Then
        verify(userService, times(2)).findByUsername(TEST_USERNAME);
        verify(passwordEncoder).matches(OLD_PASSWORD, ENCODED_PASSWORD);
        verify(userService).changePassword(TEST_USERNAME, OLD_PASSWORD, null);
    }

    @Test
    @DisplayName("Should handle empty strings in changePassword")
    void changePassword_EmptyStrings_ShouldProcessNormally() {
        // Given
        User user = createTestUser();
        when(userService.findByUsername("")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("", ENCODED_PASSWORD)).thenReturn(true);

        // When
        authService.changePassword("", "", "");

        // Then
        verify(userService, times(2)).findByUsername("");
        verify(passwordEncoder).matches("", ENCODED_PASSWORD);
        verify(userService).changePassword("", "", "");
    }

    @Test
    @DisplayName("Should handle multiple users independently")
    void multipleUsers_ShouldHandleIndependently() {
        // Given
        User user1 = createTestUser();
        User user2 = createAnotherTestUser();

        when(userService.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user1));
        when(userService.findByUsername(ANOTHER_USERNAME)).thenReturn(Optional.of(user2));
        when(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(passwordEncoder.matches(TEST_PASSWORD, "another.encoded.password")).thenReturn(false);

        // When
        boolean result1 = authService.authenticate(TEST_USERNAME, TEST_PASSWORD);
        boolean result2 = authService.authenticate(ANOTHER_USERNAME, TEST_PASSWORD);

        // Then
        assertTrue(result1);
        assertFalse(result2);
        verify(userService).findByUsername(TEST_USERNAME);
        verify(userService).findByUsername(ANOTHER_USERNAME);
        verify(passwordEncoder).matches(TEST_PASSWORD, ENCODED_PASSWORD);
        verify(passwordEncoder).matches(TEST_PASSWORD, "another.encoded.password");
    }

    @Test
    @DisplayName("Should handle same user multiple operations")
    void sameUser_MultipleOperations_ShouldWork() {
        // Given
        User user = createTestUser();
        when(userService.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(passwordEncoder.matches(OLD_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        // When
        boolean authResult = authService.authenticate(TEST_USERNAME, TEST_PASSWORD);

        // Then
        assertTrue(authResult);

        // When
        authService.changePassword(TEST_USERNAME, OLD_PASSWORD, NEW_PASSWORD);

        // Then
        verify(userService, times(3)).findByUsername(TEST_USERNAME); // 1 for auth + 2 for changePassword
        verify(passwordEncoder).matches(TEST_PASSWORD, ENCODED_PASSWORD);
        verify(passwordEncoder).matches(OLD_PASSWORD, ENCODED_PASSWORD);
        verify(userService).changePassword(TEST_USERNAME, OLD_PASSWORD, NEW_PASSWORD);
    }

    @Test
    @DisplayName("Should handle authentication failure after successful user lookup")
    void authenticate_UserFoundPasswordMismatch_ShouldReturnFalse() {
        // Given
        User user = createTestUserWithDifferentPassword();
        when(userService.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(TEST_PASSWORD, "different.encoded.password")).thenReturn(false);

        // When
        boolean result = authService.authenticate(TEST_USERNAME, TEST_PASSWORD);

        // Then
        assertFalse(result);
        verify(userService).findByUsername(TEST_USERNAME);
        verify(passwordEncoder).matches(TEST_PASSWORD, "different.encoded.password");
    }

    // Helper methods to create test data following builder pattern
    private User createTestUser() {
        return User.builder()
                .username(TEST_USERNAME)
                .password(ENCODED_PASSWORD)
                .firstName("Test")
                .lastName("User")
                .isActive(true)
                .build();
    }

    private User createAnotherTestUser() {
        return User.builder()
                .username(ANOTHER_USERNAME)
                .password("another.encoded.password")
                .firstName("Another")
                .lastName("User")
                .isActive(true)
                .build();
    }

    private User createTestUserWithDifferentPassword() {
        return User.builder()
                .username(TEST_USERNAME)
                .password("different.encoded.password")
                .firstName("Test")
                .lastName("User")
                .isActive(true)
                .build();
    }
}
