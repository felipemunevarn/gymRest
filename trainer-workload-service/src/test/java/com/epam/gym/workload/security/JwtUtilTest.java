package com.epam.gym.workload.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private final String username = "main-service";
    private final String secret = "ZmFrZV9zZWNyZXRfa2V5X2Zvcl90ZXN0aW5nX2p3dF91dGlsX3VuaXQ="; // base64 of 32+ chars
    private final long expiration = 1000 * 60 * 60; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", expiration);
    }

    @Test
    void testGenerateAndExtractUsername() {
        String token = jwtUtil.generateToken(username);
        String extracted = jwtUtil.extractUsername(token);
        assertEquals(username, extracted);
    }

    @Test
    void testExtractExpiration() {
        String token = jwtUtil.generateToken(username);
        Date expirationDate = jwtUtil.extractExpiration(token);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    void testExtractClaim() {
        String token = jwtUtil.generateToken(username);
        String subject = jwtUtil.extractClaim(token, Claims::getSubject);
        assertEquals(username, subject);
    }

    @Test
    void testValidateToken_withUsername_valid() {
        String token = jwtUtil.generateToken(username);
        assertTrue(jwtUtil.validateToken(token, username));
    }

    @Test
    void testValidateToken_withUsername_invalid() {
        String token = jwtUtil.generateToken("other-user");
        assertFalse(jwtUtil.validateToken(token, username));
    }

    @Test
    void testValidateToken_expired() throws InterruptedException {
        ReflectionTestUtils.setField(jwtUtil, "expiration", 1L); // 1 ms
        String token = jwtUtil.generateToken(username);
        Thread.sleep(5); // ensure expiration
        assertFalse(jwtUtil.validateToken(token));
    }

    @Test
    void testValidateToken_valid() {
        String token = jwtUtil.generateToken(username);
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testValidateToken_malformed() {
        String badToken = "not.a.jwt.token";
        assertFalse(jwtUtil.validateToken(badToken));
        assertFalse(jwtUtil.validateToken(badToken, username));
    }
}

