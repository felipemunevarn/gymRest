package com.epam.gym.service;

import com.epam.gym.entity.User;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthService authService;

    @Test
    void authenticate_withValidCredentials_shouldReturnTrue() {
        // Given
        String username = "john.doe";
        String password = "correctPassword";

        User user = User.builder()
                .username(username)
                .password(password)
                .build();

        when(userService.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        boolean result = authService.authenticate(username, password);

        // Then
        assertTrue(result);
        verify(userService).findByUsername(username);
    }

    @Test
    void authenticate_withInvalidPassword_shouldReturnFalse() {
        // Given
        String username = "john.doe";
        String correctPassword = "correctPassword";
        String wrongPassword = "wrongPassword";

        User user = User.builder()
                .username(username)
                .password(correctPassword)
                .build();

        when(userService.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        boolean result = authService.authenticate(username, wrongPassword);

        // Then
        assertFalse(result);
        verify(userService).findByUsername(username);
    }

    @Test
    void authenticate_withNonExistentUser_shouldThrowNoResultException() {
        // Given
        String username = "nonexistent.user";
        String password = "anyPassword";

        when(userService.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        NoResultException exception = assertThrows(NoResultException.class,
                () -> authService.authenticate(username, password));

        assertEquals("User not found", exception.getMessage());
        verify(userService).findByUsername(username);
    }

    @Test
    void authenticate_withNullPassword_shouldReturnFalse() {
        // Given
        String username = "john.doe";
        String userPassword = "correctPassword";
        String nullPassword = null;

        User user = User.builder()
                .username(username)
                .password(userPassword)
                .build();

        when(userService.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        boolean result = authService.authenticate(username, nullPassword);

        // Then
        assertFalse(result);
        verify(userService).findByUsername(username);
    }

    @Test
    void authenticate_withUserHavingNullPassword_shouldReturnFalseWhenPasswordProvided() {
        // Given
        String username = "john.doe";
        String providedPassword = "somePassword";

        User user = User.builder()
                .username(username)
                .password(null)
                .build();

        when(userService.findByUsername(username)).thenReturn(Optional.of(user));

        // When & Then
        assertThrows(NullPointerException.class,
                () -> authService.authenticate(username, providedPassword));

        verify(userService).findByUsername(username);
    }

    @Test
    void authenticate_withBothPasswordsNull_shouldReturnTrue() {
        // Given
        String username = "john.doe";
        String nullPassword = null;

        User user = User.builder()
                .username(username)
                .password(null)
                .build();

        when(userService.findByUsername(username)).thenReturn(Optional.of(user));

        // When & Then
        assertThrows(NullPointerException.class,
                () -> authService.authenticate(username, nullPassword));

        verify(userService).findByUsername(username);
    }

    @Test
    void changePassword_withValidCredentials_shouldSucceed() {
        // Given
        String username = "john.doe";
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";

        User user = User.builder()
                .username(username)
                .password(oldPassword)
                .build();

        when(userService.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        authService.changePassword(username, oldPassword, newPassword);

        // Then
        verify(userService, times(2)).findByUsername(username); // Called twice: once in changePassword, once in authenticate
        verify(userService).changePassword(username, oldPassword, newPassword);
    }

    @Test
    void changePassword_withNonExistentUser_shouldThrowNoResultException() {
        // Given
        String username = "nonexistent.user";
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";

        when(userService.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        NoResultException exception = assertThrows(NoResultException.class,
                () -> authService.changePassword(username, oldPassword, newPassword));

        assertEquals("User not found", exception.getMessage());
        verify(userService).findByUsername(username);
        verify(userService, never()).changePassword(anyString(), anyString(), anyString());
    }

    @Test
    void changePassword_withIncorrectOldPassword_shouldThrowIllegalArgumentException() {
        // Given
        String username = "john.doe";
        String correctOldPassword = "correctOldPassword";
        String incorrectOldPassword = "incorrectOldPassword";
        String newPassword = "newPassword";

        User user = User.builder()
                .username(username)
                .password(correctOldPassword)
                .build();

        when(userService.findByUsername(username)).thenReturn(Optional.of(user));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.changePassword(username, incorrectOldPassword, newPassword));

        assertEquals("Old password is incorrect", exception.getMessage());
        verify(userService, times(2)).findByUsername(username); // Called twice: once in changePassword, once in authenticate
        verify(userService, never()).changePassword(anyString(), anyString(), anyString());
    }

    @Test
    void changePassword_withEmptyOldPassword_shouldThrowIllegalArgumentException() {
        // Given
        String username = "john.doe";
        String correctPassword = "correctPassword";
        String emptyOldPassword = "";
        String newPassword = "newPassword";

        User user = User.builder()
                .username(username)
                .password(correctPassword)
                .build();

        when(userService.findByUsername(username)).thenReturn(Optional.of(user));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.changePassword(username, emptyOldPassword, newPassword));

        assertEquals("Old password is incorrect", exception.getMessage());
        verify(userService, times(2)).findByUsername(username);
        verify(userService, never()).changePassword(anyString(), anyString(), anyString());
    }

    @Test
    void changePassword_withNullOldPassword_shouldThrowIllegalArgumentException() {
        // Given
        String username = "john.doe";
        String correctPassword = "correctPassword";
        String nullOldPassword = null;
        String newPassword = "newPassword";

        User user = User.builder()
                .username(username)
                .password(correctPassword)
                .build();

        when(userService.findByUsername(username)).thenReturn(Optional.of(user));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.changePassword(username, nullOldPassword, newPassword));

        assertEquals("Old password is incorrect", exception.getMessage());
        verify(userService, times(2)).findByUsername(username);
        verify(userService, never()).changePassword(anyString(), anyString(), anyString());
    }

    @Test
    void changePassword_withSameOldAndNewPassword_shouldSucceed() {
        // Given
        String username = "john.doe";
        String password = "samePassword";

        User user = User.builder()
                .username(username)
                .password(password)
                .build();

        when(userService.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        authService.changePassword(username, password, password);

        // Then
        verify(userService, times(2)).findByUsername(username);
        verify(userService).changePassword(username, password, password);
    }

    @Test
    void changePassword_withNullNewPassword_shouldSucceed() {
        // Given
        String username = "john.doe";
        String oldPassword = "oldPassword";
        String newPassword = null;

        User user = User.builder()
                .username(username)
                .password(oldPassword)
                .build();

        when(userService.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        authService.changePassword(username, oldPassword, newPassword);

        // Then
        verify(userService, times(2)).findByUsername(username);
        verify(userService).changePassword(username, oldPassword, newPassword);
    }
}
