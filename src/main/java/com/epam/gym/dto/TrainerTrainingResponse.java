package com.epam.gym.dto;

public record TrainerTrainingResponse(

        String trainingName,
        String trainingDate,
        String trainingType,
        int trainingDuration,
        String traineeName
) {}
