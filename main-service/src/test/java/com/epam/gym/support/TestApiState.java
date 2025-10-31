package com.epam.gym.support;

import io.cucumber.spring.ScenarioScope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ScenarioScope
public class TestApiState {
    private ResponseEntity<String> lastResponse;
    private final Map<String, Object> lastRequest = new HashMap<>();

    public ResponseEntity<String> getLastResponse() { return lastResponse; }
    public void setLastResponse(ResponseEntity<String> resp) { this.lastResponse = resp; }

    public Map<String, Object> getLastRequest() { return lastRequest; }
    public void put(String key, Object value) { lastRequest.put(key, value); }
    public Object get(String key) { return lastRequest.get(key); }
}
