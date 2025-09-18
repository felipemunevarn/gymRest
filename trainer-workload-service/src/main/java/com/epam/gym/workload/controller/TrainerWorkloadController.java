package com.epam.gym.workload.controller;

import com.epam.gym.workload.dto.WorkloadRequest;
import com.epam.gym.workload.dto.WorkloadSummary;
import com.epam.gym.workload.service.TrainerWorkloadService;
import com.epam.gym.workload.util.TransactionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trainers")
@RequiredArgsConstructor
@Slf4j
@Validated
public class TrainerWorkloadController {

    private final TrainerWorkloadService trainerWorkloadService;

    /**
     * Add or delete trainer workload
     * POST /api/v1/trainers/workload
     */
    @PostMapping("/workload")
    public ResponseEntity<Void> updateTrainerWorkload(
            @Valid @RequestBody WorkloadRequest request,
            HttpServletRequest httpRequest) {

        String transactionId = TransactionUtil.generateTransactionId();

        // Transaction level logging - Request received
        log.info("TRANSACTION_START - Endpoint: POST /api/v1/trainers/workload, " +
                "TransactionId: {}, Request: {}", transactionId, request);

        try {
            // Process the workload
            trainerWorkloadService.processWorkload(request, transactionId);

            // Transaction level logging - Success response
            log.info("TRANSACTION_END - Endpoint: POST /api/v1/trainers/workload, " +
                            "TransactionId: {}, Status: 200 OK, Message: Workload updated successfully",
                    transactionId);

            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            // Transaction level logging - Error response
            log.error("TRANSACTION_END - Endpoint: POST /api/v1/trainers/workload, " +
                            "TransactionId: {}, Status: 400 BAD_REQUEST, Message: {}",
                    transactionId, e.getMessage());

            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            // Transaction level logging - Error response
            log.error("TRANSACTION_END - Endpoint: POST /api/v1/trainers/workload, " +
                            "TransactionId: {}, Status: 500 INTERNAL_SERVER_ERROR, Message: {}",
                    transactionId, e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get trainer workload summary
     * GET /api/v1/trainers/{username}/workload
     */
    @GetMapping("/{username}/workload")
    public ResponseEntity<WorkloadSummary> getTrainerWorkload(
            @PathVariable String username,
            HttpServletRequest httpRequest) {

        String transactionId = TransactionUtil.generateTransactionId();

        // Transaction level logging - Request received
        log.info("TRANSACTION_START - Endpoint: GET /api/v1/trainers/{}/workload, " +
                "TransactionId: {}, Username: {}", username, transactionId, username);

        try {
            // Get the workload summary
            WorkloadSummary summary = trainerWorkloadService.getTrainerWorkload(username, transactionId);

            if (summary == null) {
                // Transaction level logging - Not found response
                log.info("TRANSACTION_END - Endpoint: GET /api/v1/trainers/{}/workload, " +
                                "TransactionId: {}, Status: 404 NOT_FOUND, Message: Trainer not found",
                        transactionId, username);

                return ResponseEntity.notFound().build();
            }

            // Transaction level logging - Success response
            log.info("TRANSACTION_END - Endpoint: GET /api/v1/trainers/{}/workload, " +
                            "TransactionId: {}, Status: 200 OK, Message: Workload retrieved successfully",
                    transactionId, username);

            return ResponseEntity.ok(summary);

        } catch (Exception e) {
            // Transaction level logging - Error response
            log.error("TRANSACTION_END - Endpoint: GET /api/v1/trainers/{}/workload, " +
                            "TransactionId: {}, Status: 500 INTERNAL_SERVER_ERROR, Message: {}",
                    username, transactionId, e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get trainer workload in a specific month
     * GET /api/v1/trainers/{username}/workload/{year}&{month}
     */
    @GetMapping("/{username}/workload/{year}/{month}")
    public ResponseEntity<Integer> getTrainerWorkloadMonth(
            @PathVariable String username,
            @PathVariable int year,
            @PathVariable int month,
            HttpServletRequest httpRequest) {

        String transactionId = TransactionUtil.generateTransactionId();

        // Transaction level logging - Request received
        log.info("TRANSACTION_START - Endpoint: GET /api/v1/trainers/{}/workload/{}/{}, " +
                "TransactionId: {}, Username: {}, Year: {}, Month: {}", username, year, month, transactionId, username, year, month);

        try {
            // Get the workload summary
            Integer total = (int) trainerWorkloadService.getTrainerWorkloadMonth(username,
                    transactionId,
                    year,
                    month);

            if (total == null) {
                // Transaction level logging - Not found response
                log.info("TRANSACTION_END - Endpoint: GET /api/v1/trainers/{}/workload, " +
                                "TransactionId: {}, Status: 404 NOT_FOUND, Message: Trainer not found",
                        transactionId, username);

                return ResponseEntity.notFound().build();
            }

            // Transaction level logging - Success response
            log.info("TRANSACTION_END - Endpoint: GET /api/v1/trainers/{}/workload, " +
                            "TransactionId: {}, Status: 200 OK, Message: Workload retrieved successfully",
                    transactionId, username);

            return ResponseEntity.ok(total);

        } catch (Exception e) {
            // Transaction level logging - Error response
            log.error("TRANSACTION_END - Endpoint: GET /api/v1/trainers/{}/workload, " +
                            "TransactionId: {}, Status: 500 INTERNAL_SERVER_ERROR, Message: {}",
                    username, transactionId, e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all trainers (for debugging/testing purposes)
     * GET /api/v1/trainers
     */
    @GetMapping
    public ResponseEntity<List<String>> getAllTrainers(HttpServletRequest httpRequest) {

        String transactionId = TransactionUtil.generateTransactionId();

        // Transaction level logging - Request received
        log.info("TRANSACTION_START - Endpoint: GET /api/v1/trainers, TransactionId: {}", transactionId);

        try {
            // Operation level logging
            log.debug("OPERATION - Retrieving all trainer usernames, TransactionId: {}", transactionId);

            List<String> trainers = trainerWorkloadService.getAllTrainerUsernames();

            // Transaction level logging - Success response
            log.info("TRANSACTION_END - Endpoint: GET /api/v1/trainers, " +
                            "TransactionId: {}, Status: 200 OK, Message: Retrieved {} trainers",
                    transactionId, trainers.size());

            return ResponseEntity.ok(trainers);

        } catch (Exception e) {
            // Transaction level logging - Error response
            log.error("TRANSACTION_END - Endpoint: GET /api/v1/trainers, " +
                            "TransactionId: {}, Status: 500 INTERNAL_SERVER_ERROR, Message: {}",
                    transactionId, e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint
     * GET /api/v1/trainers/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Trainer Workload Service is running");
    }
}
