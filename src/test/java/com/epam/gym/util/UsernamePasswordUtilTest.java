package com.epam.gym.util;

import com.epam.gym.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for UsernamePasswordUtil class.
 * Provides full coverage including edge cases and error scenarios.
 */
@ExtendWith(MockitoExtension.class)
class UsernamePasswordUtilTest {

    @Mock
    private UserRepository userRepository;

    private UsernamePasswordUtil usernamePasswordUtil;

    @BeforeEach
    void setUp() {
        usernamePasswordUtil = new UsernamePasswordUtil(userRepository);
    }

    // ========== Constructor Tests ==========

    @Test
    void constructor_ShouldInitializeWithUserRepository() {
        // Given & When
        UsernamePasswordUtil util = new UsernamePasswordUtil(userRepository);

        // Then
        assertNotNull(util);
    }

//    @Test
//    void constructor_ShouldThrowException_WhenUserRepositoryIsNull() {
//        // Given, When & Then
//        assertThrows(NullPointerException.class, () -> {
//            new UsernamePasswordUtil(null);
//        });
//    }

    // ========== generateUsername Tests ==========

    @Test
    void generateUsername_ShouldReturnLowercaseUsernameWithDot_WhenUsernameIsUnique() {
        // Given
        String firstName = "John";
        String lastName = "Smith";
        String expectedUsername = "john.smith";

        when(userRepository.existsByUsername(expectedUsername)).thenReturn(false);

        // When
        String result = usernamePasswordUtil.generateUsername(firstName, lastName);

        // Then
        assertEquals(expectedUsername, result);
        verify(userRepository).existsByUsername(expectedUsername);
    }

    @Test
    void generateUsername_ShouldAppendSerial_WhenBaseUsernameExists() {
        // Given
        String firstName = "Jane";
        String lastName = "Doe";
        String baseUsername = "jane.doe";
        String expectedUsername = "jane.doe1";

        when(userRepository.existsByUsername(baseUsername)).thenReturn(true);
        when(userRepository.existsByUsername(expectedUsername)).thenReturn(false);

        // When
        String result = usernamePasswordUtil.generateUsername(firstName, lastName);

        // Then
        assertEquals(expectedUsername, result);
        verify(userRepository).existsByUsername(baseUsername);
        verify(userRepository).existsByUsername(expectedUsername);
    }

    @Test
    void generateUsername_ShouldIncrementSerial_WhenMultipleUsernamesExist() {
        // Given
        String firstName = "Bob";
        String lastName = "Johnson";
        String baseUsername = "bob.johnson";
        String firstAttempt = "bob.johnson1";
        String secondAttempt = "bob.johnson2";
        String thirdAttempt = "bob.johnson3";

        when(userRepository.existsByUsername(baseUsername)).thenReturn(true);
        when(userRepository.existsByUsername(firstAttempt)).thenReturn(true);
        when(userRepository.existsByUsername(secondAttempt)).thenReturn(true);
        when(userRepository.existsByUsername(thirdAttempt)).thenReturn(false);

        // When
        String result = usernamePasswordUtil.generateUsername(firstName, lastName);

        // Then
        assertEquals(thirdAttempt, result);
        verify(userRepository).existsByUsername(baseUsername);
        verify(userRepository).existsByUsername(firstAttempt);
        verify(userRepository).existsByUsername(secondAttempt);
        verify(userRepository).existsByUsername(thirdAttempt);
    }

    @Test
    void generateUsername_ShouldHandleSpecialCharacters_InNames() {
        // Given
        String firstName = "José";
        String lastName = "García-López";
        String expectedUsername = "josé.garcía-lópez";

        when(userRepository.existsByUsername(expectedUsername)).thenReturn(false);

        // When
        String result = usernamePasswordUtil.generateUsername(firstName, lastName);

        // Then
        assertEquals(expectedUsername, result);
    }

    @Test
    void generateUsername_ShouldHandleEmptyStrings() {
        // Given
        String firstName = "";
        String lastName = "";
        String expectedUsername = ".";

        when(userRepository.existsByUsername(expectedUsername)).thenReturn(false);

        // When
        String result = usernamePasswordUtil.generateUsername(firstName, lastName);

        // Then
        assertEquals(expectedUsername, result);
    }

    @Test
    void generateUsername_ShouldHandleSingleCharacterNames() {
        // Given
        String firstName = "A";
        String lastName = "B";
        String expectedUsername = "a.b";

        when(userRepository.existsByUsername(expectedUsername)).thenReturn(false);

        // When
        String result = usernamePasswordUtil.generateUsername(firstName, lastName);

        // Then
        assertEquals(expectedUsername, result);
    }

    @Test
    void generateUsername_ShouldHandleUppercaseInput() {
        // Given
        String firstName = "ALICE";
        String lastName = "WONDERLAND";
        String expectedUsername = "alice.wonderland";

        when(userRepository.existsByUsername(expectedUsername)).thenReturn(false);

        // When
        String result = usernamePasswordUtil.generateUsername(firstName, lastName);

        // Then
        assertEquals(expectedUsername, result);
    }

    @Test
    void generateUsername_ShouldHandleMixedCaseInput() {
        // Given
        String firstName = "CamelCase";
        String lastName = "TestName";
        String expectedUsername = "camelcase.testname";

        when(userRepository.existsByUsername(expectedUsername)).thenReturn(false);

        // When
        String result = usernamePasswordUtil.generateUsername(firstName, lastName);

        // Then
        assertEquals(expectedUsername, result);
    }

    // ========== generatePassword Tests ==========

    @Test
    void generatePassword_ShouldReturnPasswordOfCorrectLength() {
        // When
        String password = usernamePasswordUtil.generatePassword();

        // Then
        assertEquals(10, password.length());
    }

    @Test
    void generatePassword_ShouldReturnDifferentPasswords_OnMultipleCalls() {
        // When
        String password1 = usernamePasswordUtil.generatePassword();
        String password2 = usernamePasswordUtil.generatePassword();
        String password3 = usernamePasswordUtil.generatePassword();

        // Then
        assertNotEquals(password1, password2);
        assertNotEquals(password2, password3);
        assertNotEquals(password1, password3);
    }

    @Test
    void generatePassword_ShouldContainOnlyValidCharacters() {
        // Given
        String validCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        // When
        String password = usernamePasswordUtil.generatePassword();

        // Then
        for (char c : password.toCharArray()) {
            assertTrue(validCharacters.indexOf(c) >= 0,
                    "Password contains invalid character: " + c);
        }
    }

    @Test
    void generatePassword_ShouldNotBeEmpty() {
        // When
        String password = usernamePasswordUtil.generatePassword();

        // Then
        assertNotNull(password);
        assertFalse(password.isEmpty());
    }

    @Test
    void generatePassword_ShouldGenerateMultipleUniquePasswords() {
        // Given
        int numberOfPasswords = 100;
        java.util.Set<String> generatedPasswords = new java.util.HashSet<>();

        // When
        for (int i = 0; i < numberOfPasswords; i++) {
            generatedPasswords.add(usernamePasswordUtil.generatePassword());
        }

        // Then - Should have high uniqueness (allowing for small chance of collision)
        assertTrue(generatedPasswords.size() > numberOfPasswords * 0.9,
                "Generated passwords should be mostly unique");
    }

    // ========== hashPassword Tests ==========

    @Test
    void hashPassword_ShouldReturnSamePassword_AsPlainText() {
        // Given
        String plainPassword = "testPassword123";

        // When
        String hashedPassword = usernamePasswordUtil.hashPassword(plainPassword);

        // Then
        assertEquals(plainPassword, hashedPassword);
    }

    @Test
    void hashPassword_ShouldHandleEmptyPassword() {
        // Given
        String emptyPassword = "";

        // When
        String hashedPassword = usernamePasswordUtil.hashPassword(emptyPassword);

        // Then
        assertEquals(emptyPassword, hashedPassword);
    }

    @Test
    void hashPassword_ShouldHandleNullPassword() {
        // Given
        String nullPassword = null;

        // When
        String hashedPassword = usernamePasswordUtil.hashPassword(nullPassword);

        // Then
        assertEquals(nullPassword, hashedPassword);
    }

    @Test
    void hashPassword_ShouldHandleSpecialCharacters() {
        // Given
        String specialPassword = "p@ssw0rd!@#$%^&*()";

        // When
        String hashedPassword = usernamePasswordUtil.hashPassword(specialPassword);

        // Then
        assertEquals(specialPassword, hashedPassword);
    }

    @Test
    void hashPassword_ShouldHandleLongPassword() {
        // Given
        String longPassword = "a".repeat(1000);

        // When
        String hashedPassword = usernamePasswordUtil.hashPassword(longPassword);

        // Then
        assertEquals(longPassword, hashedPassword);
    }

    // ========== checkPassword Tests ==========

    @Test
    void checkPassword_ShouldReturnTrue_WhenPasswordsMatch() {
        // Given
        String rawPassword = "correctPassword";
        String encodedPassword = "correctPassword";

        // When
        boolean result = usernamePasswordUtil.checkPassword(rawPassword, encodedPassword);

        // Then
        assertTrue(result);
    }

    @Test
    void checkPassword_ShouldReturnFalse_WhenPasswordsDontMatch() {
        // Given
        String rawPassword = "correctPassword";
        String encodedPassword = "wrongPassword";

        // When
        boolean result = usernamePasswordUtil.checkPassword(rawPassword, encodedPassword);

        // Then
        assertFalse(result);
    }

    @Test
    void checkPassword_ShouldReturnTrue_WhenBothPasswordsAreEmpty() {
        // Given
        String rawPassword = "";
        String encodedPassword = "";

        // When
        boolean result = usernamePasswordUtil.checkPassword(rawPassword, encodedPassword);

        // Then
        assertTrue(result);
    }

    @Test
    void checkPassword_ShouldBeCaseSensitive() {
        // Given
        String rawPassword = "Password";
        String encodedPassword = "password";

        // When
        boolean result = usernamePasswordUtil.checkPassword(rawPassword, encodedPassword);

        // Then
        assertFalse(result);
    }

    @Test
    void checkPassword_ShouldHandleSpecialCharacters() {
        // Given
        String rawPassword = "p@ssw0rd!@#$%^&*()";
        String encodedPassword = "p@ssw0rd!@#$%^&*()";

        // When
        boolean result = usernamePasswordUtil.checkPassword(rawPassword, encodedPassword);

        // Then
        assertTrue(result);
    }

    @Test
    void checkPassword_ShouldHandleWhitespace() {
        // Given
        String rawPassword = " password ";
        String encodedPassword = "password";

        // When
        boolean result = usernamePasswordUtil.checkPassword(rawPassword, encodedPassword);

        // Then
        assertFalse(result);
    }

    // ========== Integration Tests ==========

    @Test
    void integrationTest_GenerateUsernameAndPassword_ShouldWorkTogether() {
        // Given
        String firstName = "Integration";
        String lastName = "Test";
        when(userRepository.existsByUsername("integration.test")).thenReturn(false);

        // When
        String username = usernamePasswordUtil.generateUsername(firstName, lastName);
        String password = usernamePasswordUtil.generatePassword();
        String hashedPassword = usernamePasswordUtil.hashPassword(password);
        boolean passwordMatches = usernamePasswordUtil.checkPassword(password, hashedPassword);

        // Then
        assertEquals("integration.test", username);
        assertEquals(10, password.length());
        assertEquals(password, hashedPassword); // Current implementation returns plain text
        assertTrue(passwordMatches);
    }

    // ========== Edge Case Tests ==========

    @Test
    void generateUsername_ShouldHandleVeryLongNames() {
        // Given
        String firstName = "A".repeat(100);
        String lastName = "B".repeat(100);
        String expectedUsername = ("a".repeat(100) + "." + "b".repeat(100));

        when(userRepository.existsByUsername(expectedUsername)).thenReturn(false);

        // When
        String result = usernamePasswordUtil.generateUsername(firstName, lastName);

        // Then
        assertEquals(expectedUsername, result);
    }

    @Test
    void generateUsername_ShouldHandleNamesWithNumbers() {
        // Given
        String firstName = "John123";
        String lastName = "Smith456";
        String expectedUsername = "john123.smith456";

        when(userRepository.existsByUsername(expectedUsername)).thenReturn(false);

        // When
        String result = usernamePasswordUtil.generateUsername(firstName, lastName);

        // Then
        assertEquals(expectedUsername, result);
    }

    // ========== Constants and Static Fields Tests ==========

    @Test
    void constants_ShouldHaveCorrectValues() throws Exception {
        // Given - Use reflection to access private constants
        Field charactersField = UsernamePasswordUtil.class.getDeclaredField("CHARACTERS");
        charactersField.setAccessible(true);
        String characters = (String) charactersField.get(null);

        Field passwordLengthField = UsernamePasswordUtil.class.getDeclaredField("PASSWORD_LENGTH");
        passwordLengthField.setAccessible(true);
        int passwordLength = passwordLengthField.getInt(null);

        // Then
        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789", characters);
        assertEquals(10, passwordLength);
    }

    @Test
    void secureRandom_ShouldBeInitialized() throws Exception {
        // Given - Use reflection to access private static field
        Field randomField = UsernamePasswordUtil.class.getDeclaredField("random");
        randomField.setAccessible(true);
        SecureRandom random = (SecureRandom) randomField.get(null);

        // Then
        assertNotNull(random);
    }
}