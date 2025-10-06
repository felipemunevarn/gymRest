package com.epam.gym.workload.controller;

import com.epam.gym.workload.document.TrainerTrainingSummary;
import com.epam.gym.workload.service.TrainerTrainingSummaryService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trainers/summary")
public class TrainerSummaryController {

    private static final Logger log = LoggerFactory.getLogger(TrainerSummaryController.class);

    @Autowired
    private TrainerTrainingSummaryService trainerTrainingSummaryService;

    /**
     * Get trainer training summary by username
     */
    @GetMapping("/{username}")
    public ResponseEntity<TrainerTrainingSummary> getTrainerSummary(
            @PathVariable String username,
            HttpServletRequest httpRequest) {

        String transactionId = UUID.randomUUID().toString();

        log.info("TRANSACTION_START - Endpoint: GET /api/v1/trainers/summary/{}, " +
                        "TransactionId: {}, Username: {}",
                username, transactionId, username);

        try {
            log.info("OPERATION - Retrieving trainer summary for username: {}, TransactionId: {}",
                    username, transactionId);

            Optional<TrainerTrainingSummary> summary =
                    trainerTrainingSummaryService.getTrainerSummary(username);

            if (summary.isEmpty()) {
                log.warn("OPERATION - Trainer summary not found for username: {}, TransactionId: {}",
                        username, transactionId);

                log.info("TRANSACTION_END - Endpoint: GET /api/v1/trainers/summary/{}, " +
                                "TransactionId: {}, Status: 404 NOT FOUND",
                        username, transactionId);

                return ResponseEntity.notFound().build();
            }

            log.info("OPERATION - Trainer summary retrieved successfully for username: {}, " +
                            "Total years: {}, TransactionId: {}",
                    username, summary.get().getYears().size(), transactionId);

            log.info("TRANSACTION_END - Endpoint: GET /api/v1/trainers/summary/{}, " +
                            "TransactionId: {}, Status: 200 OK",
                    username, transactionId);

            return ResponseEntity.ok(summary.get());

        } catch (Exception e) {
            log.error("TRANSACTION_END - Endpoint: GET /api/v1/trainers/summary/{}, " +
                            "TransactionId: {}, Status: 500 ERROR, Message: {}",
                    username, transactionId, e.getMessage(), e);

            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get trainer workload for specific year and month
     */
    @GetMapping("/{username}/workload/{year}/{month}")
    public ResponseEntity<MonthWorkloadResponse> getTrainerWorkloadMonth(
            @PathVariable String username,
            @PathVariable Integer year,
            @PathVariable Integer month,
            HttpServletRequest httpRequest) {

        String transactionId = UUID.randomUUID().toString();

        log.info("TRANSACTION_START - Endpoint: GET /api/v1/trainers/summary/{}/workload/{}/{}, " +
                        "TransactionId: {}",
                username, year, month, transactionId);

        try {
            // Validate month
            if (month < 1 || month > 12) {
                log.warn("OPERATION - Invalid month value: {}, TransactionId: {}", month, transactionId);

                log.info("TRANSACTION_END - Endpoint: GET /api/v1/trainers/summary/{}/workload/{}/{}, " +
                                "TransactionId: {}, Status: 400 BAD REQUEST",
                        username, year, month, transactionId);

                return ResponseEntity.badRequest().build();
            }

            log.info("OPERATION - Retrieving workload for trainer: {}, year: {}, month: {}, TransactionId: {}",
                    username, year, month, transactionId);

            Optional<TrainerTrainingSummary> summary =
                    trainerTrainingSummaryService.getTrainerSummary(username);

            if (summary.isEmpty()) {
                log.warn("OPERATION - Trainer not found: {}, TransactionId: {}", username, transactionId);

                log.info("TRANSACTION_END - Endpoint: GET /api/v1/trainers/summary/{}/workload/{}/{}, " +
                                "TransactionId: {}, Status: 404 NOT FOUND",
                        username, year, month, transactionId);

                return ResponseEntity.notFound().build();
            }

            // Find the specific year and month
            Integer duration = summary.get().getYears().stream()
                    .filter(y -> y.getYear().equals(year))
                    .flatMap(y -> y.getMonths().stream())
                    .filter(m -> m.getMonth().equals(month))
                    .map(TrainerTrainingSummary.MonthSummary::getTrainingSummaryDuration)
                    .findFirst()
                    .orElse(0);

            MonthWorkloadResponse response = new MonthWorkloadResponse(
                    username, year, month, duration);

            log.info("OPERATION - Workload retrieved: {} minutes for {}/{}, TransactionId: {}",
                    duration, year, month, transactionId);

            log.info("TRANSACTION_END - Endpoint: GET /api/v1/trainers/summary/{}/workload/{}/{}, " +
                            "TransactionId: {}, Status: 200 OK",
                    username, year, month, transactionId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("TRANSACTION_END - Endpoint: GET /api/v1/trainers/summary/{}/workload/{}/{}, " +
                            "TransactionId: {}, Status: 500 ERROR, Message: {}",
                    username, year, month, transactionId, e.getMessage(), e);

            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        log.debug("OPERATION - Health check called");
        return ResponseEntity.ok("Trainer Summary Service is healthy");
    }

    // Response DTO
    public static class MonthWorkloadResponse {
        private String trainerUsername;
        private Integer year;
        private Integer month;
        private Integer trainingSummaryDuration;

        public MonthWorkloadResponse(String trainerUsername, Integer year, Integer month, Integer duration) {
            this.trainerUsername = trainerUsername;
            this.year = year;
            this.month = month;
            this.trainingSummaryDuration = duration;
        }

        // Getters
        public String getTrainerUsername() { return trainerUsername; }
        public Integer getYear() { return year; }
        public Integer getMonth() { return month; }
        public Integer getTrainingSummaryDuration() { return trainingSummaryDuration; }
    }
}