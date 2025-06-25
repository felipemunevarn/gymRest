package com.epam.gym.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    // Use Set for O(1) lookup performance and thread safety
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public void blacklistToken(String token) {
        blacklistedTokens.add("blacklist:" + token);
    }

    public boolean isTokenBlacklisted(String token) {
        String key = "blacklist:" + token;
        return blacklistedTokens.contains(key);
    }

    // Optional: Method to clear all blacklisted tokens (for testing or admin purposes)
    public void clearBlacklist() {
        blacklistedTokens.clear();
    }

    // Optional: Get count of blacklisted tokens
    public int getBlacklistedTokenCount() {
        return blacklistedTokens.size();
    }
}
