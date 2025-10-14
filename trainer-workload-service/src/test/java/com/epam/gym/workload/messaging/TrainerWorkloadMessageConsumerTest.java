package com.epam.gym.workload.messaging;

import com.epam.gym.workload.security.JwtUtil;
import com.epam.gym.workload.service.TrainerTrainingSummaryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainerWorkloadMessageConsumerTest {

    @InjectMocks
    private TrainerWorkloadMessageConsumer consumer;

    @Mock
    private TrainerTrainingSummaryService trainerTrainingSummaryService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Captor
    private ArgumentCaptor<TrainerWorkloadEvent.TrainerWorkloadPayload> payloadCaptor;

    private final String validToken = "Bearer valid.jwt.token";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private TrainerWorkloadEvent createValidEvent() {
        TrainerWorkloadEvent.TrainerWorkloadPayload payload = new TrainerWorkloadEvent.TrainerWorkloadPayload();
        payload.setTrainerUsername("john.doe");
        payload.setTrainingDate("2025-10-13");
        payload.setTrainingDuration(60);
        payload.setActionType(TrainerWorkloadEvent.TrainerWorkloadPayload.ActionType.ADD);

        TrainerWorkloadEvent event = new TrainerWorkloadEvent();
        event.setMessageId("msg-123");
        event.setPayload(payload);
        event.setAuthToken(validToken);
        return event;
    }

    @Test
    void testHandleTrainerWorkloadUpdate_success() throws Exception {
        String json = "{}";
        TrainerWorkloadEvent event = createValidEvent();

        when(objectMapper.readValue(json, TrainerWorkloadEvent.class)).thenReturn(event);
        when(jwtUtil.validateToken("valid.jwt.token")).thenReturn(true);
        when(jwtUtil.extractUsername("valid.jwt.token")).thenReturn("main-service");

        consumer.handleTrainerWorkloadUpdate(json);

        verify(trainerTrainingSummaryService).processTrainingEvent(payloadCaptor.capture(), eq("msg-123"));
        assertEquals("john.doe", payloadCaptor.getValue().getTrainerUsername());
    }

    @Test
    void testHandleTrainerWorkloadUpdate_invalidToken() throws Exception {
        TrainerWorkloadEvent event = createValidEvent();
        event.setAuthToken("Bearer invalid.token");

        when(objectMapper.readValue(anyString(), eq(TrainerWorkloadEvent.class))).thenReturn(event);
        when(jwtUtil.validateToken("invalid.token")).thenReturn(false);

        SecurityException ex = assertThrows(SecurityException.class, () ->
                consumer.handleTrainerWorkloadUpdate("{}"));

        assertEquals("Invalid authentication token", ex.getMessage());
    }

    @Test
    void testHandleTrainerWorkloadUpdate_missingToken() throws Exception {
        TrainerWorkloadEvent event = createValidEvent();
        event.setAuthToken("   "); // blank token

        when(objectMapper.readValue(anyString(), eq(TrainerWorkloadEvent.class))).thenReturn(event);

        SecurityException ex = assertThrows(SecurityException.class, () ->
                consumer.handleTrainerWorkloadUpdate("{}"));

        assertEquals("Invalid authentication token", ex.getMessage());
    }

    @Test
    void testHandleTrainerWorkloadUpdate_invalidPayload() throws Exception {
        TrainerWorkloadEvent event = createValidEvent();
        event.getPayload().setTrainingDate(null); // missing required field

        when(objectMapper.readValue(anyString(), eq(TrainerWorkloadEvent.class))).thenReturn(event);
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.extractUsername(anyString())).thenReturn("main-service");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                consumer.handleTrainerWorkloadUpdate("{}"));

        assertEquals("Required fields missing in workload event", ex.getMessage());
    }

    @Test
    void testHandleTrainerWorkloadUpdate_unexpectedUsername() throws Exception {
        TrainerWorkloadEvent event = createValidEvent();

        when(objectMapper.readValue(anyString(), eq(TrainerWorkloadEvent.class))).thenReturn(event);
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.extractUsername(anyString())).thenReturn("other-service");

        // Should still succeed, just logs a warning
        consumer.handleTrainerWorkloadUpdate("{}");

        verify(trainerTrainingSummaryService).processTrainingEvent(any(), eq("msg-123"));
    }

    @Test
    void testHandleTrainerWorkloadUpdate_jsonProcessingException() throws Exception {
        when(objectMapper.readValue(anyString(), eq(TrainerWorkloadEvent.class)))
                .thenThrow(new JsonProcessingException("bad json") {});

        assertThrows(JsonProcessingException.class, () ->
                consumer.handleTrainerWorkloadUpdate("{}"));
    }

    @Test
    void testHandleTrainerWorkloadUpdate_genericException() throws Exception {
        TrainerWorkloadEvent event = createValidEvent();

        when(objectMapper.readValue(anyString(), eq(TrainerWorkloadEvent.class))).thenReturn(event);
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.extractUsername(anyString())).thenReturn("main-service");
        doThrow(new RuntimeException("DB error")).when(trainerTrainingSummaryService)
                .processTrainingEvent(any(), anyString());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                consumer.handleTrainerWorkloadUpdate("{}"));

        assertEquals("DB error", ex.getMessage());
    }
}
