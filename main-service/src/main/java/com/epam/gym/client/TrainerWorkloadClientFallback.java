package com.epam.gym.client;

import com.epam.gym.dto.TrainerWorkloadRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TrainerWorkloadClientFallback implements TrainerWorkloadClient {

    private static final Logger log = LoggerFactory.getLogger(TrainerWorkloadClientFallback.class);

    @Override
    public ResponseEntity<Void> updateTrainerWorkload(TrainerWorkloadRequest request, String authToken) {
        log.error("Circuit breaker activated - Trainer Workload Service is unavailable. " +
                        "Failed to update workload for trainer: {} with action: {}",
                request.getTrainerUsername(), request.getActionType());

        // Implement additional fallback logic here, such as:
        // - Store the request in a queue for retry later
        // - Send notification to administrators
        // - Return a specific error response

        return ResponseEntity.ok().build();
    }
}