package com.epam.gym.workload.cucumber;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TestContext {
    private ResponseEntity<?> response;

    public ResponseEntity<?> getResponse() {
        return response;
    }

    public void setResponse(ResponseEntity<?> response) {
        this.response = response;
    }
}
