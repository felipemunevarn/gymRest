package com.epam.gym.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginAttemptService Tests")
class LoginAttemptServiceTest {

    private LoginAttemptService loginAttemptService;
    private static final String TEST_USERNAME = "testuser";
    private static final String ANOTHER_USERNAME = "anotheruser";

    @BeforeEach
    void setUp() {
        loginAttemptService = new LoginAttemptService();
    }

    @Test
    @DisplayName("Should increment failed attempts for new user")
    void loginFailed_NewUser_ShouldIncrementAttempts() {
        // When
        loginAttemptService.loginFailed(TEST_USERNAME);

        // Then
        assertFalse(loginAttemptService.isBlocked(TEST_USERNAME));
    }

    @Test
    @DisplayName("Should increment failed attempts for existing user")
    void loginFailed_ExistingUser_ShouldIncrementAttempts() {
        // Given
        loginAttemptService.loginFailed(TEST_USERNAME);

        // When
        loginAttemptService.loginFailed(TEST_USERNAME);

        // Then
        assertFalse(loginAttemptService.isBlocked(TEST_USERNAME));
    }

    @Test
    @DisplayName("Should block user after maximum attempts reached")
    void loginFailed_MaxAttemptsReached_ShouldBlockUser() {
        // When - reach maximum attempts (3)
        loginAttemptService.loginFailed(TEST_USERNAME);
        loginAttemptService.loginFailed(TEST_USERNAME);
        loginAttemptService.loginFailed(TEST_USERNAME);

        // Then
        assertTrue(loginAttemptService.isBlocked(TEST_USERNAME));
    }

    @Test
    @DisplayName("Should block user after exceeding maximum attempts")
    void loginFailed_ExceedMaxAttempts_ShouldKeepUserBlocked() {
        // When - exceed maximum attempts
        loginAttemptService.loginFailed(TEST_USERNAME);
        loginAttemptService.loginFailed(TEST_USERNAME);
        loginAttemptService.loginFailed(TEST_USERNAME);
        loginAttemptService.loginFailed(TEST_USERNAME); // 4th attempt

        // Then
        assertTrue(loginAttemptService.isBlocked(TEST_USERNAME));
    }

    @Test
    @DisplayName("Should remove attempts and lock when login succeeds")
    void loginSucceeded_BlockedUser_ShouldClearAttemptsAndUnblock() {
        // Given - user is blocked
        loginAttemptService.loginFailed(TEST_USERNAME);
        loginAttemptService.loginFailed(TEST_USERNAME);
        loginAttemptService.loginFailed(TEST_USERNAME);
        assertTrue(loginAttemptService.isBlocked(TEST_USERNAME));

        // When
        loginAttemptService.loginSucceeded(TEST_USERNAME);

        // Then
        assertFalse(loginAttemptService.isBlocked(TEST_USERNAME));
    }

    @Test
    @DisplayName("Should handle login success for non-blocked user")
    void loginSucceeded_NonBlockedUser_ShouldClearAttempts() {
        // Given
        loginAttemptService.loginFailed(TEST_USERNAME);
        assertFalse(loginAttemptService.isBlocked(TEST_USERNAME));

        // When
        loginAttemptService.loginSucceeded(TEST_USERNAME);

        // Then
        assertFalse(loginAttemptService.isBlocked(TEST_USERNAME));
    }

    @Test
    @DisplayName("Should handle login success for user with no previous attempts")
    void loginSucceeded_NoAttempts_ShouldNotThrowException() {
        // When & Then - should not throw exception
        assertDoesNotThrow(() -> loginAttemptService.loginSucceeded(TEST_USERNAME));
        assertFalse(loginAttemptService.isBlocked(TEST_USERNAME));
    }

    @Test
    @DisplayName("Should return false for non-blocked user")
    void isBlocked_NonBlockedUser_ShouldReturnFalse() {
        // When & Then
        assertFalse(loginAttemptService.isBlocked(TEST_USERNAME));
    }

    @Test
    @DisplayName("Should return true for blocked user")
    void isBlocked_BlockedUser_ShouldReturnTrue() {
        // Given - block user
        loginAttemptService.loginFailed(TEST_USERNAME);
        loginAttemptService.loginFailed(TEST_USERNAME);
        loginAttemptService.loginFailed(TEST_USERNAME);

        // When & Then
        assertTrue(loginAttemptService.isBlocked(TEST_USERNAME));
    }

    @Test
    @DisplayName("Should return false when user is not locked")
    void isLockExpired_UserNotLocked_ShouldReturnFalse() {
        // When & Then
        assertFalse(loginAttemptService.isLockExpired(TEST_USERNAME));
    }

    @Test
    @DisplayName("Should return false when lock is not expired")
    void isLockExpired_LockNotExpired_ShouldReturnFalse() {
        // Given - mock current time
        LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        try (MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedTime);

            // Block user at fixed time
            loginAttemptService.loginFailed(TEST_USERNAME);
            loginAttemptService.loginFailed(TEST_USERNAME);
            loginAttemptService.loginFailed(TEST_USERNAME);

            // Check immediately after blocking (same time)
            assertFalse(loginAttemptService.isLockExpired(TEST_USERNAME));

            // Check 4 minutes later (still within 5-minute lock)
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedTime.plusMinutes(4));
            assertFalse(loginAttemptService.isLockExpired(TEST_USERNAME));
        }
    }

    @Test
    @DisplayName("Should return true when lock is expired")
    void isLockExpired_LockExpired_ShouldReturnTrue() {
        // Given - mock current time
        LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        try (MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedTime);

            // Block user at fixed time
            loginAttemptService.loginFailed(TEST_USERNAME);
            loginAttemptService.loginFailed(TEST_USERNAME);
            loginAttemptService.loginFailed(TEST_USERNAME);

            // Check 6 minutes later (after 5-minute lock expires)
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedTime.plusMinutes(6));
            assertTrue(loginAttemptService.isLockExpired(TEST_USERNAME));
        }
    }

    @Test
    @DisplayName("Should return true when lock expires exactly at duration limit")
    void isLockExpired_LockExpiresExactly_ShouldReturnTrue() {
        // Given - mock current time
        LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        try (MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedTime);

            // Block user at fixed time
            loginAttemptService.loginFailed(TEST_USERNAME);
            loginAttemptService.loginFailed(TEST_USERNAME);
            loginAttemptService.loginFailed(TEST_USERNAME);

            // Check exactly 5 minutes and 1 second later
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedTime.plusMinutes(5).plusSeconds(1));
            assertTrue(loginAttemptService.isLockExpired(TEST_USERNAME));
        }
    }

    @Test
    @DisplayName("Should reset attempts and lock for blocked user")
    void reset_BlockedUser_ShouldClearAttemptsAndUnblock() {
        // Given - block user
        loginAttemptService.loginFailed(TEST_USERNAME);
        loginAttemptService.loginFailed(TEST_USERNAME);
        loginAttemptService.loginFailed(TEST_USERNAME);
        assertTrue(loginAttemptService.isBlocked(TEST_USERNAME));

        // When
        loginAttemptService.reset(TEST_USERNAME);

        // Then
        assertFalse(loginAttemptService.isBlocked(TEST_USERNAME));
    }

    @Test
    @DisplayName("Should reset attempts for non-blocked user")
    void reset_NonBlockedUser_ShouldClearAttempts() {
        // Given
        loginAttemptService.loginFailed(TEST_USERNAME);
        assertFalse(loginAttemptService.isBlocked(TEST_USERNAME));

        // When
        loginAttemptService.reset(TEST_USERNAME);

        // Then
        assertFalse(loginAttemptService.isBlocked(TEST_USERNAME));
    }

    @Test
    @DisplayName("Should handle reset for user with no attempts")
    void reset_NoAttempts_ShouldNotThrowException() {
        // When & Then - should not throw exception
        assertDoesNotThrow(() -> loginAttemptService.reset(TEST_USERNAME));
        assertFalse(loginAttemptService.isBlocked(TEST_USERNAME));
    }

    @Test
    @DisplayName("Should handle multiple users independently")
    void multipleUsers_ShouldHandleIndependently() {
        // Given - different states for different users
        loginAttemptService.loginFailed(TEST_USERNAME); // 1 attempt

        loginAttemptService.loginFailed(ANOTHER_USERNAME); // 1 attempt
        loginAttemptService.loginFailed(ANOTHER_USERNAME); // 2 attempts
        loginAttemptService.loginFailed(ANOTHER_USERNAME); // 3 attempts - blocked

        // Then
        assertFalse(loginAttemptService.isBlocked(TEST_USERNAME));
        assertTrue(loginAttemptService.isBlocked(ANOTHER_USERNAME));

        // When - successful login for first user
        loginAttemptService.loginSucceeded(TEST_USERNAME);

        // Then - should not affect second user
        assertFalse(loginAttemptService.isBlocked(TEST_USERNAME));
        assertTrue(loginAttemptService.isBlocked(ANOTHER_USERNAME));
    }

//    @Test
//    @DisplayName("Should handle null username gracefully")
//    void nullUsername_ShouldNotThrowException() {
//        // When & Then - should not throw exception for any method
//        assertDoesNotThrow(() -> {
//            loginAttemptService.loginFailed(null);
//            loginAttemptService.loginSucceeded(null);
//            loginAttemptService.isBlocked(null);
//            loginAttemptService.isLockExpired(null);
//            loginAttemptService.reset(null);
//        });
//    }

    @Test
    @DisplayName("Should handle empty username gracefully")
    void emptyUsername_ShouldNotThrowException() {
        String emptyUsername = "";

        // When & Then - should not throw exception for any method
        assertDoesNotThrow(() -> {
            loginAttemptService.loginFailed(emptyUsername);
            loginAttemptService.loginSucceeded(emptyUsername);
            loginAttemptService.isBlocked(emptyUsername);
            loginAttemptService.isLockExpired(emptyUsername);
            loginAttemptService.reset(emptyUsername);
        });
    }

    @Test
    @DisplayName("Should maintain state consistency after multiple operations")
    void stateConsistency_MultipleOperations_ShouldMaintainConsistency() {
        // Given - build up attempts
        loginAttemptService.loginFailed(TEST_USERNAME);
        loginAttemptService.loginFailed(TEST_USERNAME);
        assertFalse(loginAttemptService.isBlocked(TEST_USERNAME));

        // When - one more failure to block
        loginAttemptService.loginFailed(TEST_USERNAME);
        assertTrue(loginAttemptService.isBlocked(TEST_USERNAME));

        // When - more failures after blocking
        loginAttemptService.loginFailed(TEST_USERNAME);
        loginAttemptService.loginFailed(TEST_USERNAME);

        // Then - should still be blocked
        assertTrue(loginAttemptService.isBlocked(TEST_USERNAME));

        // When - reset
        loginAttemptService.reset(TEST_USERNAME);

        // Then - should be unblocked and ready for new attempts
        assertFalse(loginAttemptService.isBlocked(TEST_USERNAME));
        assertFalse(loginAttemptService.isLockExpired(TEST_USERNAME));
    }
}
