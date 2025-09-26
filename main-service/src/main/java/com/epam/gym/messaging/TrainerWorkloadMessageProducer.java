package com.epam.gym.messaging;

import com.epam.gym.security.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TrainerWorkloadMessageProducer {

    private static final Logger log = LoggerFactory.getLogger(TrainerWorkloadMessageProducer.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${app.queue.trainer-workload}")
    private String trainerWorkloadQueue;

    public void sendWorkloadUpdate(TrainerWorkloadEvent.TrainerWorkloadPayload payload) {
        try {
            // DEBUG: Check if JwtUtil is injected
            if (jwtUtil == null) {
                log.error("JwtUtil is NULL! Cannot generate authentication token.");
                throw new IllegalStateException("JwtUtil not properly injected");
            }

            String token = jwtUtil.generateToken("main-service");

            // DEBUG: Log the generated token
            log.info("DEBUG: Generated JWT token: {}", token != null ? "Token generated successfully" : "Token is NULL");

            TrainerWorkloadEvent event = TrainerWorkloadEvent.builder()
                    .messageId(UUID.randomUUID().toString())
                    .messageType("TRAINER_WORKLOAD_UPDATE")
                    .timestamp(LocalDateTime.now())
                    .source("main-service")
                    .authToken(token)
                    .payload(payload)
                    .build();

            String jsonMessage = objectMapper.writeValueAsString(event);

            // DEBUG: Check if authToken is in the JSON
            log.info("DEBUG: Sending JSON message: {}", jsonMessage);
            log.info("DEBUG: AuthToken in event: {}", event.getAuthToken() != null ? "Present" : "NULL");

            jmsTemplate.convertAndSend(trainerWorkloadQueue, jsonMessage);

            log.info("Trainer workload event sent successfully - MessageId: {}, TrainerUsername: {}, ActionType: {}",
                    event.getMessageId(),
                    payload.getTrainerUsername(),
                    payload.getActionType());

        } catch (Exception e) {
            log.error("Failed to send trainer workload event for trainer: {}",
                    payload.getTrainerUsername(), e);
            // Future:
            // 1. Store failed messages for retry
            // 2. Send to a dead letter queue
            // 3. Trigger an alert
        }
    }
}