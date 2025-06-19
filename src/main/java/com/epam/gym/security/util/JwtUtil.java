package com.epam.gym.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    // 👉 keep long, random, Base64‑encoded string in env / config
    private static final String SECRET = "Z3YlJzI+RUlEYkZORyZKJFN2a3JhT2FvN2ozTjM3Rng=";

    private SecretKey key;

    @PostConstruct
    private void initKey() {
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    }

    /** 1_day (86_400_000_ms) */
    private static final long EXP_MS = 86_400_000;

    public String generateToken(String username, String password) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("password", password)
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
}
