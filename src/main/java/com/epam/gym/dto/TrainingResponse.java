package com.epam.gym.dto;

public record TrainingResponse(

        String trainingName,
        String trainingDate,
        String trainingType,
        int trainingDuration,
        String trainerName
) {}
