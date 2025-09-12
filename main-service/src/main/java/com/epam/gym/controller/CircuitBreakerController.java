package com.epam.gym.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/circuit-breaker")
public class CircuitBreakerController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    public CircuitBreakerController(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getCircuitBreakerStatus() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("trainer-workload-service");

        Map<String, Object> status = new HashMap<>();
        status.put("name", circuitBreaker.getName());
        status.put("state", circuitBreaker.getState().toString());
        status.put("failureRate", circuitBreaker.getMetrics().getFailureRate());
        status.put("slowCallRate", circuitBreaker.getMetrics().getSlowCallRate());
        status.put("numberOfBufferedCalls", circuitBreaker.getMetrics().getNumberOfBufferedCalls());
        status.put("numberOfFailedCalls", circuitBreaker.getMetrics().getNumberOfFailedCalls());
        status.put("numberOfSuccessfulCalls", circuitBreaker.getMetrics().getNumberOfSuccessfulCalls());
        status.put("numberOfSlowCalls", circuitBreaker.getMetrics().getNumberOfSlowCalls());

        return ResponseEntity.ok(status);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Circuit Breaker Service is running");
    }
}