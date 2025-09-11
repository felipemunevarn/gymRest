package com.epam.gym.service;

import com.epam.gym.client.TrainerWorkloadClient;
import com.epam.gym.dto.TrainerWorkloadRequest;
import com.epam.gym.security.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class TrainerWorkloadService {

    private static final Logger log = LoggerFactory.getLogger(TrainerWorkloadService.class);

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
     * Add training workload for trainer
     */
    public void addTrainingWorkload(String trainerUsername, String trainerFirstName,
                                    String trainerLastName, Boolean isActive,
                                    LocalDate trainingDate, Integer trainingDuration) {

        log.info("Adding training workload for trainer: {} on date: {} with duration: {} minutes",
                trainerUsername, trainingDate, trainingDuration);

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
            workloadClient.updateTrainerWorkload(request, authToken);

            log.info("Successfully added training workload for trainer: {}", trainerUsername);

        } catch (Exception e) {
            log.error("Failed to add training workload for trainer: {} - Error: {}",
                    trainerUsername, e.getMessage(), e);
        }
    }

    /**
     * Delete training workload for trainer
     */
    public void deleteTrainingWorkload(String trainerUsername, String trainerFirstName,
                                       String trainerLastName, Boolean isActive,
                                       LocalDate trainingDate, Integer trainingDuration) {

        log.info("Deleting training workload for trainer: {} on date: {} with duration: {} minutes",
                trainerUsername, trainingDate, trainingDuration);

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
            workloadClient.updateTrainerWorkload(request, authToken);

            log.info("Successfully deleted training workload for trainer: {}", trainerUsername);

        } catch (Exception e) {
            log.error("Failed to delete training workload for trainer: {} - Error: {}",
                    trainerUsername, e.getMessage(), e);
        }
    }
}