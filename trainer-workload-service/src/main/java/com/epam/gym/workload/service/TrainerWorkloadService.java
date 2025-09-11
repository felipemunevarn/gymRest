package com.epam.gym.workload.service;

import com.epam.gym.workload.dto.WorkloadRequest;
import com.epam.gym.workload.dto.WorkloadSummary;
import com.epam.gym.workload.model.TrainerWorkload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TrainerWorkloadService {

    // In-memory database - using ConcurrentHashMap for thread safety
    private final Map<String, TrainerWorkload> trainerWorkloads = new ConcurrentHashMap<>();

    /**
     * Process trainer workload based on the request
     */
    public void processWorkload(WorkloadRequest request, String transactionId) {
        log.info("Processing workload for trainer: {} with action: {} [TransactionId: {}]",
                request.getTrainerUsername(), request.getActionType(), transactionId);

        TrainerWorkload trainerWorkload = trainerWorkloads.computeIfAbsent(
                request.getTrainerUsername(),
                username -> TrainerWorkload.builder()
                        .username(request.getTrainerUsername())
                        .firstName(request.getTrainerFirstName())
                        .lastName(request.getTrainerLastName())
                        .isActive(request.getIsActive())
                        .build()
        );

        // Update trainer information
        trainerWorkload.setFirstName(request.getTrainerFirstName());
        trainerWorkload.setLastName(request.getTrainerLastName());
        trainerWorkload.setActive(request.getIsActive());

        // Extract year and month from training date
        LocalDate trainingDate = request.getTrainingDate();
        int year = trainingDate.getYear();
        int month = trainingDate.getMonthValue();
        int duration = request.getTrainingDuration();

        // Process based on action type
        switch (request.getActionType()) {
            case ADD:
                trainerWorkload.addTraining(year, month, duration);
                log.info("Added {} minutes of training for trainer {} in {}/{} [TransactionId: {}]",
                        duration, request.getTrainerUsername(), month, year, transactionId);
                break;
            case DELETE:
                trainerWorkload.removeTraining(year, month, duration);
                log.info("Removed {} minutes of training for trainer {} in {}/{} [TransactionId: {}]",
                        duration, request.getTrainerUsername(), month, year, transactionId);
                break;
            default:
                log.warn("Unknown action type: {} [TransactionId: {}]", request.getActionType(), transactionId);
                throw new IllegalArgumentException("Invalid action type: " + request.getActionType());
        }

        log.info("Workload processing completed for trainer: {} [TransactionId: {}]",
                request.getTrainerUsername(), transactionId);
    }

    /**
     * Get trainer workload summary
     */
    public WorkloadSummary getTrainerWorkload(String trainerUsername, String transactionId) {
        log.info("Retrieving workload summary for trainer: {} [TransactionId: {}]",
                trainerUsername, transactionId);

        TrainerWorkload trainerWorkload = trainerWorkloads.get(trainerUsername);

        if (trainerWorkload == null) {
            log.warn("Trainer not found: {} [TransactionId: {}]", trainerUsername, transactionId);
            return null;
        }

        // Convert internal model to response DTO
        List<WorkloadSummary.YearSummary> years = trainerWorkload.getYearlyWorkload()
                .entrySet()
                .stream()
                .map(yearEntry -> {
                    List<WorkloadSummary.MonthSummary> months = yearEntry.getValue()
                            .entrySet()
                            .stream()
                            .map(monthEntry -> WorkloadSummary.MonthSummary.builder()
                                    .month(monthEntry.getKey())
                                    .trainingSummaryDuration(monthEntry.getValue())
                                    .build())
                            .sorted((m1, m2) -> Integer.compare(m1.getMonth(), m2.getMonth()))
                            .collect(Collectors.toList());

                    return WorkloadSummary.YearSummary.builder()
                            .year(yearEntry.getKey())
                            .months(months)
                            .build();
                })
                .sorted((y1, y2) -> Integer.compare(y1.getYear(), y2.getYear()))
                .collect(Collectors.toList());

        WorkloadSummary summary = WorkloadSummary.builder()
                .trainerUsername(trainerWorkload.getUsername())
                .trainerFirstName(trainerWorkload.getFirstName())
                .trainerLastName(trainerWorkload.getLastName())
                .trainerStatus(trainerWorkload.isActive())
                .years(years)
                .build();

        log.info("Retrieved workload summary for trainer: {} with {} years of data [TransactionId: {}]",
                trainerUsername, years.size(), transactionId);

        return summary;
    }

    /**
     * Get all trainer usernames (for testing/debugging purposes)
     */
    public List<String> getAllTrainerUsernames() {
        return new ArrayList<>(trainerWorkloads.keySet());
    }

    /**
     * Clear all data (for testing purposes)
     */
    public void clearAllData() {
        log.info("Clearing all trainer workload data");
        trainerWorkloads.clear();
    }

    /**
     * Check if trainer exists
     */
    public boolean trainerExists(String trainerUsername) {
        return trainerWorkloads.containsKey(trainerUsername);
    }
}