package com.epam.gym.service;

import com.epam.gym.client.TrainerWorkloadClient;
import com.epam.gym.dto.TrainerWorkloadRequest;
import com.epam.gym.security.util.JwtUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

@Service
public class TrainerWorkloadService {

    private static final Logger log = LoggerFactory.getLogger(TrainerWorkloadService.class);
    private static final String CIRCUIT_BREAKER_NAME = "trainer-workload-service";

    private final TrainerWorkloadClient workloadClient;
    private final JwtUtil jwtUtil;

    @Value("${app.system.username:system}")
    private String systemUsername;

    @Autowired
    public TrainerWorkloadService(TrainerWorkloadClient workloadClient, JwtUtil jwtUtil) {
        this.workloadClient = workloadClient;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Add training workload for trainer with Circuit Breaker, Retry, and Timeout
     */
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "addTrainingWorkloadFallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    @TimeLimiter(name = CIRCUIT_BREAKER_NAME)
    public CompletableFuture<Void> addTrainingWorkload(String trainerUsername, String trainerFirstName,
                                                       String trainerLastName, Boolean isActive,
                                                       LocalDate trainingDate, Integer trainingDuration) {

        log.info("Adding training workload for trainer: {} on date: {} with duration: {} minutes",
                trainerUsername, trainingDate, trainingDuration);

        return CompletableFuture.supplyAsync(() -> {
            try {
                TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                        .trainerUsername(trainerUsername)
                        .trainerFirstName(trainerFirstName)
                        .trainerLastName(trainerLastName)
                        .isActive(isActive)
                        .trainingDate(trainingDate)
                        .trainingDuration(trainingDuration)
                        .actionType(TrainerWorkloadRequest.ActionType.ADD)
                        .build();

                String authToken = "Bearer " + jwtUtil.generateToken(systemUsername);
                ResponseEntity<Void> response = workloadClient.updateTrainerWorkload(request, authToken);

                log.info("Successfully added training workload for trainer: {} - Response: {}",
                        trainerUsername, response.getStatusCode());

                return null;

            } catch (Exception e) {
                log.error("Failed to add training workload for trainer: {} - Error: {}",
                        trainerUsername, e.getMessage());
                throw new RuntimeException("Workload service communication failed", e);
            }
        });
    }

    /**
     * Delete training workload for trainer with Circuit Breaker, Retry, and Timeout
     */
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "deleteTrainingWorkloadFallback")
    @Retry(name = CIRCUIT_BREAKER_NAME)
    @TimeLimiter(name = CIRCUIT_BREAKER_NAME)
    public CompletableFuture<Void> deleteTrainingWorkload(String trainerUsername, String trainerFirstName,
                                                          String trainerLastName, Boolean isActive,
                                                          LocalDate trainingDate, Integer trainingDuration) {

        log.info("Deleting training workload for trainer: {} on date: {} with duration: {} minutes",
                trainerUsername, trainingDate, trainingDuration);

        return CompletableFuture.supplyAsync(() -> {
            try {
                TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                        .trainerUsername(trainerUsername)
                        .trainerFirstName(trainerFirstName)
                        .trainerLastName(trainerLastName)
                        .isActive(isActive)
                        .trainingDate(trainingDate)
                        .trainingDuration(trainingDuration)
                        .actionType(TrainerWorkloadRequest.ActionType.DELETE)
                        .build();

                String authToken = "Bearer " + jwtUtil.generateToken(systemUsername);
                ResponseEntity<Void> response = workloadClient.updateTrainerWorkload(request, authToken);

                log.info("Successfully deleted training workload for trainer: {} - Response: {}",
                        trainerUsername, response.getStatusCode());

                return null;

            } catch (Exception e) {
                log.error("Failed to delete training workload for trainer: {} - Error: {}",
                        trainerUsername, e.getMessage());
                throw new RuntimeException("Workload service communication failed", e);
            }
        });
    }

    /**
     * Synchronous methods for backward compatibility (if needed)
     */
    public void addTrainingWorkloadSync(String trainerUsername, String trainerFirstName,
                                        String trainerLastName, Boolean isActive,
                                        LocalDate trainingDate, Integer trainingDuration) {
        try {
            addTrainingWorkload(trainerUsername, trainerFirstName, trainerLastName,
                    isActive, trainingDate, trainingDuration).get();
        } catch (Exception e) {
            log.warn("Async workload addition failed, but main operation continues: {}", e.getMessage());
        }
    }

    public void deleteTrainingWorkloadSync(String trainerUsername, String trainerFirstName,
                                           String trainerLastName, Boolean isActive,
                                           LocalDate trainingDate, Integer trainingDuration) {
        try {
            deleteTrainingWorkload(trainerUsername, trainerFirstName, trainerLastName,
                    isActive, trainingDate, trainingDuration).get();
        } catch (Exception e) {
            log.warn("Async workload deletion failed, but main operation continues: {}", e.getMessage());
        }
    }

    // Fallback methods
    public CompletableFuture<Void> addTrainingWorkloadFallback(String trainerUsername, String trainerFirstName,
                                                               String trainerLastName, Boolean isActive,
                                                               LocalDate trainingDate, Integer trainingDuration,
                                                               Exception exception) {

        log.error("Circuit breaker OPEN - Add workload fallback triggered for trainer: {} - Reason: {}",
                trainerUsername, exception.getMessage());

        // Here you could implement additional fallback logic such as:
        // - Store the request in a queue for retry later
        // - Send notification to administrators
        // - Store in local database for later sync

        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Void> deleteTrainingWorkloadFallback(String trainerUsername, String trainerFirstName,
                                                                  String trainerLastName, Boolean isActive,
                                                                  LocalDate trainingDate, Integer trainingDuration,
                                                                  Exception exception) {

        log.error("Circuit breaker OPEN - Delete workload fallback triggered for trainer: {} - Reason: {}",
                trainerUsername, exception.getMessage());

        // Here you could implement additional fallback logic

        return CompletableFuture.completedFuture(null);
    }
}