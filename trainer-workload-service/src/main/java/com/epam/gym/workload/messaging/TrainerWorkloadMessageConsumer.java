package com.epam.gym.workload.messaging;

import com.epam.gym.workload.dto.WorkloadRequest;
import com.epam.gym.workload.security.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import com.epam.gym.workload.service.TrainerWorkloadService;

import java.time.LocalDate;

@Component
public class TrainerWorkloadMessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(TrainerWorkloadMessageConsumer.class);

    @Autowired
    private TrainerWorkloadService trainerWorkloadService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JwtUtil jwtUtil;

    @JmsListener(destination = "${app.queue.trainer-workload}")
    public void handleTrainerWorkloadUpdate(String jsonMessage) throws JsonProcessingException {
        TrainerWorkloadEvent event = null;
        String transactionId = "unknown";

        try {

            // DEBUG: Log the received JSON message
            log.info("DEBUG: Received JSON message: {}", jsonMessage);

            event = objectMapper.readValue(jsonMessage, TrainerWorkloadEvent.class);
            transactionId = event.getMessageId();

            log.info("TRANSACTION_START - Message Consumer: TrainerWorkloadUpdate, " +
                            "MessageId: {}, TrainerUsername: {}, ActionType: {}",
                    transactionId,
                    event.getPayload().getTrainerUsername(),
                    event.getPayload().getActionType());

            // DEBUG: Check authToken after parsing
            log.info("DEBUG: AuthToken after parsing: {}",
                    event.getAuthToken() != null ? "Present (" + event.getAuthToken().length() + " chars)" : "NULL");

            // SECURITY VALIDATION - Validate JWT token
            if (!validateAuthentication(event, transactionId)) {
                log.error("SECURITY_VIOLATION - Invalid or missing authentication token, MessageId: {}", transactionId);
                throw new SecurityException("Invalid authentication token");
            }

            // Validate message
            if (!isValidWorkloadEvent(event)) {
                log.error("Invalid workload event received: {}", transactionId);
                throw new IllegalArgumentException("Required fields missing in workload event");
            }

            // Convert message to your existing request format
            WorkloadRequest request = convertEventToRequest(event.getPayload());

            // Process using your existing service
            trainerWorkloadService.processWorkload(request, transactionId);

            log.info("TRANSACTION_END - Message Consumer: TrainerWorkloadUpdate, " +
                            "MessageId: {}, Status: SUCCESS, Message: Workload updated successfully via messaging",
                    transactionId);

        } catch (SecurityException e) {
            log.error("TRANSACTION_END - Message Consumer: TrainerWorkloadUpdate, " +
                            "MessageId: {}, Status: SECURITY_ERROR, Message: {}",
                    transactionId, e.getMessage(), e);
            // Security violations should not be retried
            // This message will go to DLQ
            throw e;

        } catch (Exception e) {
            log.error("TRANSACTION_END - Message Consumer: TrainerWorkloadUpdate, " +
                            "MessageId: {}, Status: ERROR, Message: {}",
                    transactionId, e.getMessage(), e);
            // This will trigger retry mechanism and eventually send to DLQ
            throw e;
        }
    }

    private boolean isValidWorkloadEvent(TrainerWorkloadEvent event) {
        return event != null &&
                event.getPayload() != null &&
                event.getPayload().getTrainerUsername() != null &&
                event.getPayload().getTrainingDate() != null &&
                event.getPayload().getTrainingDuration() != null &&
                event.getPayload().getActionType() != null;
    }

    private WorkloadRequest convertEventToRequest(TrainerWorkloadEvent.TrainerWorkloadPayload payload) {
        return WorkloadRequest.builder()
                .trainerUsername(payload.getTrainerUsername())
                .trainerFirstName(payload.getTrainerFirstName())
                .trainerLastName(payload.getTrainerLastName())
                .isActive(payload.isActive())
                .trainingDate(LocalDate.parse(payload.getTrainingDate()))
                .trainingDuration(payload.getTrainingDuration())
                .actionType(WorkloadRequest.ActionType.valueOf(payload.getActionType().name()))
                .build();
    }

    /**
     * Validates the JWT token in the message
     */
    private boolean validateAuthentication(TrainerWorkloadEvent event, String transactionId) {
        try {
            // Check if auth token is present
            if (event.getAuthToken() == null || event.getAuthToken().trim().isEmpty()) {
                log.warn("No authentication token provided in message: {}", transactionId);
                return false;
            }

            // Remove "Bearer " prefix if present
            String token = event.getAuthToken();
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // Validate JWT token
            if (!jwtUtil.validateToken(token)) {
                log.warn("Invalid JWT token in message: {}", transactionId);
                return false;
            }

            // Extract username from token for additional validation
            String username = jwtUtil.extractUsername(token);
            log.info("Message authenticated successfully - Username: {}, MessageId: {}", username, transactionId);

            // Optional: Check if the username matches expected service name
            if (!"main-service".equals(username)) {
                log.warn("Unexpected service username in token: {} for message: {}", username, transactionId);
                // You can decide if this should fail or just log warning
            }

            return true;

        } catch (Exception e) {
            log.error("Error validating authentication token for message: {}, Error: {}",
                    transactionId, e.getMessage());
            return false;
        }
    }
}