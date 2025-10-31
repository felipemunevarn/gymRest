package com.epam.gym.support;

import io.cucumber.spring.ScenarioScope;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
@ScenarioScope

public class TestAuthState {
    private String username = "alice";
    private final HttpHeaders headers = new HttpHeaders();

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public HttpHeaders getHeaders() { return headers; }

    /** Merge auth headers into target headers */
    public void applyTo(org.springframework.http.HttpHeaders target) {
        this.headers.forEach((k, vals) -> vals.forEach(v -> target.add(k, v)));
    }
}
