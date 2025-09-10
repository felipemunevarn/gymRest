package com.epam.gym.security.util;

import com.epam.gym.entity.User;
import com.epam.gym.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtUtil {

    // 👉 keep long, random, Base64‑encoded string in env / config
    private static final String SECRET = "870fc857a079157a69c5c03a8788a0c4721d90f8fe35476d1bce3609fc2ede4f";

    private final SecretKey key;
    private final UserRepository userRepository;

    public JwtUtil(SecretKey jwtSecretKey, UserRepository userRepository){
        this.key = jwtSecretKey;
        this.userRepository = userRepository;
    }

    /** 1_day (86_400_000_ms) */
    private static final long EXP_MS = 86_400_000;

    public String generateToken(String username, String password) {
        Instant now = Instant.now();

        String role = getUserRoles(username);

        return Jwts.builder()
                .subject(username)
                .claim("password", password)
                .claim("roles", Arrays.asList(role))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(EXP_MS)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public boolean isValid(String token, String username) {
        return username.equals(extractUsername(token)) && !isExpired(token);
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractPassword(String token) {
        return parseClaims(token).get("password", String.class);
    }

    private boolean isExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String getUserRoles(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return String.valueOf(user.get().getRole());
    }
}
