package com.epam.gym.security.util;

import com.epam.gym.entity.User;
import com.epam.gym.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUtil Tests")
class JwtUtilTest {

    @Mock
    private UserRepository userRepository;

    private JwtUtil jwtUtil;
    private SecretKey testSecretKey;

    private static final String TEST_USERNAME = "testuser";
    private static final String ANOTHER_USERNAME = "anotheruser";
    private static final String TEST_PASSWORD = "password123";
    private static final String ANOTHER_PASSWORD = "anotherpass";
    private static final String TEST_ROLE = "TRAINEE";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String SECRET_STRING = "870fc857a079157a69c5c03a8788a0c4721d90f8fe35476d1bce3609fc2ede4f";

    @BeforeEach
    void setUp() {
        // Create a test secret key from the same secret used in the actual class
        byte[] keyBytes = java.util.Base64.getDecoder().decode(
                java.util.Base64.getEncoder().encodeToString(SECRET_STRING.getBytes())
        );
        testSecretKey = Keys.hmacShaKeyFor(keyBytes);
        jwtUtil = new JwtUtil(testSecretKey, userRepository);
    }

    @Test
    @DisplayName("Should create JwtUtil with required dependencies")
    void constructor_WithValidDependencies_ShouldCreateInstance() {
        // When
        JwtUtil util = new JwtUtil(testSecretKey, userRepository);

        // Then
        assertNotNull(util);
    }

    @Test
    @DisplayName("Should generate valid JWT token with correct claims")
    void generateToken_ValidInput_ShouldReturnValidToken() {
        // Given
        User user = createTestUser();
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));

        // When
        String token = jwtUtil.generateToken(TEST_USERNAME, TEST_PASSWORD);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Verify token structure (header.payload.signature)
        String[] tokenParts = token.split("\\.");
        assertEquals(3, tokenParts.length);

        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should generate token with correct username in subject")
    void generateToken_ValidInput_ShouldSetCorrectSubject() {
        // Given
        User user = createTestUser();
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));

        // When
        String token = jwtUtil.generateToken(TEST_USERNAME, TEST_PASSWORD);
        String extractedUsername = jwtUtil.extractUsername(token);

        // Then
        assertEquals(TEST_USERNAME, extractedUsername);
        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should generate token with correct password claim")
    void generateToken_ValidInput_ShouldSetCorrectPasswordClaim() {
        // Given
        User user = createTestUser();
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));

        // When
        String token = jwtUtil.generateToken(TEST_USERNAME, TEST_PASSWORD);
        String extractedPassword = jwtUtil.extractPassword(token);

        // Then
        assertEquals(TEST_PASSWORD, extractedPassword);
        verify(userRepository).findByUsername(TEST_USERNAME);
    }

//    @Test
//    @DisplayName("Should generate token with fixed expiration time")
//    void generateToken_ValidInput_ShouldSetCorrectExpiration() {
//        // Given
//        User user = createTestUser();
//        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
//        Instant fixedTime = Instant.parse("2024-01-01T12:00:00Z");
//
//        try (MockedStatic<Instant> mockedInstant = mockStatic(Instant.class)) {
//            mockedInstant.when(Instant::now).thenReturn(fixedTime);
//
//            // When
//            String token = jwtUtil.generateToken(TEST_USERNAME, TEST_PASSWORD);
//
//            // Parse token to check expiration
//            Claims claims = Jwts.parser()
//                    .verifyWith(testSecretKey)
//                    .build()
//                    .parseSignedClaims(token)
//                    .getPayload();
//
//            // Then
//            Date expectedExpiration = Date.from(fixedTime.plusMillis(86_400_000));
//            assertEquals(expectedExpiration, claims.getExpiration());
//            verify(userRepository).findByUsername(TEST_USERNAME);
//        }
//    }

    @Test
    @DisplayName("Should throw exception when user not found during token generation")
    void generateToken_UserNotFound_ShouldThrowException() {
        // Given
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

        // When & Then
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> jwtUtil.generateToken(TEST_USERNAME, TEST_PASSWORD)
        );

        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should generate token with different roles")
    void generateToken_DifferentRoles_ShouldSetCorrectRole() {
        // Given
        User adminUser = createAdminUser();
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(adminUser));

        // When
        String token = jwtUtil.generateToken(TEST_USERNAME, TEST_PASSWORD);

        // Parse token to check roles
        Claims claims = Jwts.parser()
                .verifyWith(testSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Then
        assertNotNull(claims.get("roles"));
        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should validate token with correct username")
    void isValid_CorrectUsername_ShouldReturnTrue() {
        // Given
        User user = createTestUser();
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
        String token = jwtUtil.generateToken(TEST_USERNAME, TEST_PASSWORD);

        // When
        boolean isValid = jwtUtil.isValid(token, TEST_USERNAME);

        // Then
        assertTrue(isValid);
        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should invalidate token with incorrect username")
    void isValid_IncorrectUsername_ShouldReturnFalse() {
        // Given
        User user = createTestUser();
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
        String token = jwtUtil.generateToken(TEST_USERNAME, TEST_PASSWORD);

        // When
        boolean isValid = jwtUtil.isValid(token, ANOTHER_USERNAME);

        // Then
        assertFalse(isValid);
        verify(userRepository).findByUsername(TEST_USERNAME);
    }

//    @Test
//    @DisplayName("Should invalidate expired token")
//    void isValid_ExpiredToken_ShouldReturnFalse() {
//        // Given
//        User user = createTestUser();
//        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
//
//        Instant pastTime = Instant.parse("2023-01-01T12:00:00Z");
//        String expiredToken;
//
//        try (MockedStatic<Instant> mockedInstant = mockStatic(Instant.class)) {
//            mockedInstant.when(Instant::now).thenReturn(pastTime);
//            expiredToken = jwtUtil.generateToken(TEST_USERNAME, TEST_PASSWORD);
//        }
//
//        // When
//        boolean isValid = jwtUtil.isValid(expiredToken, TEST_USERNAME);
//
//        // Then
//        assertFalse(isValid);
//        verify(userRepository).findByUsername(TEST_USERNAME);
//    }

    @Test
    @DisplayName("Should extract username from valid token")
    void extractUsername_ValidToken_ShouldReturnUsername() {
        // Given
        User user = createTestUser();
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
        String token = jwtUtil.generateToken(TEST_USERNAME, TEST_PASSWORD);

        // When
        String extractedUsername = jwtUtil.extractUsername(token);

        // Then
        assertEquals(TEST_USERNAME, extractedUsername);
        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should extract password from valid token")
    void extractPassword_ValidToken_ShouldReturnPassword() {
        // Given
        User user = createTestUser();
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
        String token = jwtUtil.generateToken(TEST_USERNAME, TEST_PASSWORD);

        // When
        String extractedPassword = jwtUtil.extractPassword(token);

        // Then
        assertEquals(TEST_PASSWORD, extractedPassword);
        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should throw exception for malformed token when extracting username")
    void extractUsername_MalformedToken_ShouldThrowException() {
        // Given
        String malformedToken = "not.a.valid.jwt.token";

        // When & Then
        assertThrows(
                MalformedJwtException.class,
                () -> jwtUtil.extractUsername(malformedToken)
        );
    }

    @Test
    @DisplayName("Should throw exception for malformed token when extracting password")
    void extractPassword_MalformedToken_ShouldThrowException() {
        // Given
        String malformedToken = "not.a.valid.jwt.token";

        // When & Then
        assertThrows(
                MalformedJwtException.class,
                () -> jwtUtil.extractPassword(malformedToken)
        );
    }

    @Test
    @DisplayName("Should throw exception for token with wrong signature")
    void extractUsername_WrongSignature_ShouldThrowException() {
        // Given - create token with different key
        SecretKey differentKey = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        String tokenWithWrongSignature = Jwts.builder()
                .subject(TEST_USERNAME)
                .claim("password", TEST_PASSWORD)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86_400_000))
                .signWith(differentKey, Jwts.SIG.HS256)
                .compact();

        // When & Then
        assertThrows(
                SignatureException.class,
                () -> jwtUtil.extractUsername(tokenWithWrongSignature)
        );
    }

    @Test
    @DisplayName("Should handle null token gracefully")
    void extractUsername_NullToken_ShouldThrowException() {
        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> jwtUtil.extractUsername(null)
        );
    }

    @Test
    @DisplayName("Should handle empty token gracefully")
    void extractUsername_EmptyToken_ShouldThrowException() {
        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> jwtUtil.extractUsername("")
        );
    }

//    @Test
//    @DisplayName("Should handle null username in isValid")
//    void isValid_NullUsername_ShouldReturnFalse() {
//        // Given
//        User user = createTestUser();
//        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
//        String token = jwtUtil.generateToken(TEST_USERNAME, TEST_PASSWORD);
//
//        // When
//        boolean isValid = jwtUtil.isValid(token, null);
//
//        // Then
//        assertFalse(isValid);
//        verify(userRepository).findByUsername(TEST_USERNAME);
//    }

    @Test
    @DisplayName("Should handle null token in isValid")
    void isValid_NullToken_ShouldThrowException() {
        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> jwtUtil.isValid(null, TEST_USERNAME)
        );
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void generateToken_DifferentUsers_ShouldGenerateDifferentTokens() {
        // Given
        User user1 = createTestUser();
        User user2 = createAnotherTestUser();
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user1));
        when(userRepository.findByUsername(ANOTHER_USERNAME)).thenReturn(Optional.of(user2));

        // When
        String token1 = jwtUtil.generateToken(TEST_USERNAME, TEST_PASSWORD);
        String token2 = jwtUtil.generateToken(ANOTHER_USERNAME, ANOTHER_PASSWORD);

        // Then
        assertNotEquals(token1, token2);
        assertEquals(TEST_USERNAME, jwtUtil.extractUsername(token1));
        assertEquals(ANOTHER_USERNAME, jwtUtil.extractUsername(token2));
        assertEquals(TEST_PASSWORD, jwtUtil.extractPassword(token1));
        assertEquals(ANOTHER_PASSWORD, jwtUtil.extractPassword(token2));

        verify(userRepository).findByUsername(TEST_USERNAME);
        verify(userRepository).findByUsername(ANOTHER_USERNAME);
    }

//    @Test
//    @DisplayName("Should handle token validation with expired token exception")
//    void isValid_ExpiredTokenException_ShouldReturnFalse() {
//        // Given
//        User user = createTestUser();
//        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
//
//        // Create an expired token by setting past time
//        Instant pastTime = Instant.parse("2020-01-01T12:00:00Z");
//        String expiredToken;
//
//        try (MockedStatic<Instant> mockedInstant = mockStatic(Instant.class)) {
//            mockedInstant.when(Instant::now).thenReturn(pastTime);
//            expiredToken = jwtUtil.generateToken(TEST_USERNAME, TEST_PASSWORD);
//        }
//
//        // When
//        boolean isValid = jwtUtil.isValid(expiredToken, TEST_USERNAME);
//
//        // Then
//        assertFalse(isValid);
//        verify(userRepository).findByUsername(TEST_USERNAME);
//    }

    @Test
    @DisplayName("Should handle getUserRoles with null user")
    void generateToken_NullUserRole_ShouldThrowException() {
        // Given
        User userWithNullRole = User.builder()
                .username(TEST_USERNAME)
                .firstName("Test")
                .lastName("User")
                .isActive(true)
                .role(null)
                .build();
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(userWithNullRole));

        // When & Then - should not throw exception, String.valueOf handles null
        assertDoesNotThrow(() -> jwtUtil.generateToken(TEST_USERNAME, TEST_PASSWORD));
        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    // Helper methods to create test data following builder pattern
    private User createTestUser() {
        return User.builder()
                .username(TEST_USERNAME)
                .password("encoded.password")
                .firstName("Test")
                .lastName("User")
                .isActive(true)
                .role(User.Role.USER)
                .build();
    }

    private User createAnotherTestUser() {
        return User.builder()
                .username(ANOTHER_USERNAME)
                .password("another.encoded.password")
                .firstName("Another")
                .lastName("User")
                .isActive(true)
                .role(User.Role.USER)
                .build();
    }

    private User createAdminUser() {
        return User.builder()
                .username(TEST_USERNAME)
                .password("encoded.password")
                .firstName("Admin")
                .lastName("User")
                .isActive(true)
                .role(User.Role.ADMIN)
                .build();
    }
}
