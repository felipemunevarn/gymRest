package com.epam.gym.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);
    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCK_DURATION_MINUTES = 1;

    private final Map<String, Integer> attempts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lockTimestamps = new ConcurrentHashMap<>();

    public void loginFailed(String username) {
        int newAttempts = attempts.merge(username, 1, Integer::sum);
        log.info("Rest: " + (MAX_ATTEMPTS - newAttempts) + " times more to block temporarily the user");
        if (newAttempts >= MAX_ATTEMPTS) {
            lockTimestamps.put(username, LocalDateTime.now());
        }
    }

    public void loginSucceeded(String username) {
        attempts.remove(username);
        lockTimestamps.remove(username);
    }

    public boolean isBlocked(String username) {
        return lockTimestamps.containsKey(username);
    }

    public boolean isLockExpired(String username) {
        LocalDateTime lockTime = lockTimestamps.get(username);
        return lockTime != null && lockTime.plusMinutes(LOCK_DURATION_MINUTES).isBefore(LocalDateTime.now());
    }

    public void reset(String username) {
        attempts.remove(username);
        lockTimestamps.remove(username);
    }
}
