package com.epam.gym.workload.service;

import com.epam.gym.workload.document.TrainerTrainingSummary;
import com.epam.gym.workload.document.TrainerTrainingSummary.YearSummary;
import com.epam.gym.workload.document.TrainerTrainingSummary.MonthSummary;
import com.epam.gym.workload.messaging.TrainerWorkloadEvent;
import com.epam.gym.workload.repository.TrainerTrainingSummaryRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class TrainerTrainingSummaryService {

    private static final Logger log = LoggerFactory.getLogger(TrainerTrainingSummaryService.class);

    @Autowired
    private TrainerTrainingSummaryRepository repository;

    /**
     * Process training workload event and update trainer summary
     *
     * @param event The workload event containing training information
     * @param transactionId Transaction ID for logging
     */
    @Transactional
    public void processTrainingEvent(@Valid @NotNull TrainerWorkloadEvent.TrainerWorkloadPayload event,
                                     String transactionId) {

        log.info("OPERATION_START - Processing training event for trainer: {}, TransactionId: {}",
                event.getTrainerUsername(), transactionId);

        try {
            // Extract training date components
            LocalDate trainingDate = LocalDate.parse(event.getTrainingDate());
            int year = trainingDate.getYear();
            int month = trainingDate.getMonthValue();

            log.debug("OPERATION - Extracted year: {}, month: {} from training date: {}, TransactionId: {}",
                    year, month, event.getTrainingDate(), transactionId);

            // Step a: Try to extract Trainer's record by Username
            Optional<TrainerTrainingSummary> existingSummary =
                    repository.findByTrainerUsername(event.getTrainerUsername());

            TrainerTrainingSummary summary;

            if (existingSummary.isEmpty()) {
                // Step b: Create new record if doesn't exist
                log.info("OPERATION - Trainer document not found, creating new record for: {}, TransactionId: {}",
                        event.getTrainerUsername(), transactionId);
                summary = createNewTrainerSummary(event, year, month);
            } else {
                // Step c & d: Update existing record
                log.info("OPERATION - Trainer document found, updating existing record for: {}, TransactionId: {}",
                        event.getTrainerUsername(), transactionId);
                summary = existingSummary.get();
                updateTrainerSummary(summary, event, year, month, transactionId);
            }

            // Step e: Save to MongoDB
            TrainerTrainingSummary savedSummary = repository.save(summary);

            log.info("OPERATION_END - Successfully processed training event for trainer: {}, " +
                            "Duration: {} minutes, Action: {}, TransactionId: {}",
                    event.getTrainerUsername(), event.getTrainingDuration(),
                    event.getActionType(), transactionId);

        } catch (Exception e) {
            log.error("OPERATION_ERROR - Failed to process training event for trainer: {}, " +
                            "Error: {}, TransactionId: {}",
                    event.getTrainerUsername(), e.getMessage(), transactionId, e);
            throw e;
        }
    }

    /**
     * Create new trainer summary document
     */
    private TrainerTrainingSummary createNewTrainerSummary(
            TrainerWorkloadEvent.TrainerWorkloadPayload event,
            int year,
            int month) {

        log.debug("OPERATION - Creating new trainer summary with year: {}, month: {}", year, month);

        // Create month summary with initial duration
        MonthSummary monthSummary = MonthSummary.builder()
                .month(month)
                .trainingSummaryDuration(calculateDuration(event))
                .build();

        // Create year summary with the month
        YearSummary yearSummary = YearSummary.builder()
                .year(year)
                .months(new ArrayList<>())
                .build();
        yearSummary.getMonths().add(monthSummary);

        // Create trainer summary
        TrainerTrainingSummary summary = TrainerTrainingSummary.builder()
                .trainerUsername(event.getTrainerUsername())
                .trainerFirstName(event.getTrainerFirstName())
                .trainerLastName(event.getTrainerLastName())
                .trainerStatus(event.isActive())
                .years(new ArrayList<>())
                .build();
        summary.getYears().add(yearSummary);

        log.debug("OPERATION - Created new trainer summary for username: {}", event.getTrainerUsername());

        return summary;
    }

    /**
     * Update existing trainer summary
     */
    private void updateTrainerSummary(
            TrainerTrainingSummary summary,
            TrainerWorkloadEvent.TrainerWorkloadPayload event,
            int year,
            int month,
            String transactionId) {

        log.debug("OPERATION - Updating trainer summary for year: {}, month: {}, TransactionId: {}",
                year, month, transactionId);

        // Update trainer status if changed
        summary.setTrainerStatus(event.isActive());

        // Find or create year summary
        YearSummary yearSummary = summary.getYears().stream()
                .filter(y -> y.getYear().equals(year))
                .findFirst()
                .orElseGet(() -> {
                    log.debug("OPERATION - Year {} not found, creating new year entry, TransactionId: {}",
                            year, transactionId);
                    YearSummary newYear = YearSummary.builder()
                            .year(year)
                            .months(new ArrayList<>())
                            .build();
                    summary.getYears().add(newYear);
                    return newYear;
                });

        // Find or create month summary
        MonthSummary monthSummary = yearSummary.getMonths().stream()
                .filter(m -> m.getMonth().equals(month))
                .findFirst()
                .orElseGet(() -> {
                    log.debug("OPERATION - Month {} not found in year {}, creating new month entry, TransactionId: {}",
                            month, year, transactionId);
                    MonthSummary newMonth = MonthSummary.builder()
                            .month(month)
                            .trainingSummaryDuration(0)
                            .build();
                    yearSummary.getMonths().add(newMonth);
                    return newMonth;
                });

        // Calculate new duration based on action type
        int currentDuration = monthSummary.getTrainingSummaryDuration();
        int newDuration = calculateNewDuration(currentDuration, event);

        log.debug("OPERATION - Updating training duration from {} to {} minutes, TransactionId: {}",
                currentDuration, newDuration, transactionId);

        monthSummary.setTrainingSummaryDuration(newDuration);
    }

    /**
     * Calculate duration based on action type
     */
    private int calculateDuration(TrainerWorkloadEvent.TrainerWorkloadPayload event) {
        return event.getActionType() == TrainerWorkloadEvent.TrainerWorkloadPayload.ActionType.ADD
                ? event.getTrainingDuration()
                : -event.getTrainingDuration();
    }

    /**
     * Calculate new duration: ADD increases, DELETE decreases
     */
    private int calculateNewDuration(int currentDuration, TrainerWorkloadEvent.TrainerWorkloadPayload event) {
        int trainingDuration = event.getTrainingDuration();

        if (event.getActionType() == TrainerWorkloadEvent.TrainerWorkloadPayload.ActionType.ADD) {
            return currentDuration + trainingDuration;
        } else {
            return Math.max(0, currentDuration - trainingDuration); // Prevent negative duration
        }
    }

    /**
     * Get trainer summary by username
     */
    public Optional<TrainerTrainingSummary> getTrainerSummary(String username) {
        log.info("OPERATION - Retrieving trainer summary for username: {}", username);
        return repository.findByTrainerUsername(username);
    }
}
