package com.epam.gym.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {
    private final Map<String, String> tokenStore = new ConcurrentHashMap<>();

    public String generateToken(String username) {
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, username);
        return token;
    }

    public boolean isValidToken(String username, String token) {
        return tokenStore.get(token).equals(username);
    }

    public String getUsername(String token) {
        return tokenStore.get(token);
    }

    public void invalidateToken(String token) {
        tokenStore.remove(token);
    }
}

