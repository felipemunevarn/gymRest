package com.epam.gym.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreaker.Metrics;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CircuitBreakerControllerTest {

    private CircuitBreakerRegistry circuitBreakerRegistry;
    private CircuitBreakerController controller;

    @BeforeEach
    void setUp() {
        circuitBreakerRegistry = mock(CircuitBreakerRegistry.class);
        controller = new CircuitBreakerController(circuitBreakerRegistry);
    }

    @Test
    void testHealthEndpoint() {
        ResponseEntity<String> response = controller.health();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Circuit Breaker Service is running", response.getBody());
    }

    @Test
    void testStatusEndpoint() {
        CircuitBreaker circuitBreaker = mock(CircuitBreaker.class);
        Metrics metrics = mock(Metrics.class);

        when(circuitBreakerRegistry.circuitBreaker("trainer-workload-service")).thenReturn(circuitBreaker);
        when(circuitBreaker.getName()).thenReturn("trainer-workload-service");
        when(circuitBreaker.getState()).thenReturn(CircuitBreaker.State.CLOSED);
        when(circuitBreaker.getMetrics()).thenReturn(metrics);

        when(metrics.getFailureRate()).thenReturn(10.0f);
        when(metrics.getSlowCallRate()).thenReturn(5.0f);
        when(metrics.getNumberOfBufferedCalls()).thenReturn(100);
        when(metrics.getNumberOfFailedCalls()).thenReturn(10);
        when(metrics.getNumberOfSuccessfulCalls()).thenReturn(90);
        when(metrics.getNumberOfSlowCalls()).thenReturn(3);

        ResponseEntity<Map<String, Object>> response = controller.getCircuitBreakerStatus();
        Map<String, Object> body = response.getBody();

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(body);
        assertEquals("trainer-workload-service", body.get("name"));
        assertEquals("CLOSED", body.get("state"));
        assertEquals(10.0f, body.get("failureRate"));
        assertEquals(5.0f, body.get("slowCallRate"));
        assertEquals(100, body.get("numberOfBufferedCalls"));
        assertEquals(10, body.get("numberOfFailedCalls"));
        assertEquals(90, body.get("numberOfSuccessfulCalls"));
        assertEquals(3, body.get("numberOfSlowCalls"));
    }
}
