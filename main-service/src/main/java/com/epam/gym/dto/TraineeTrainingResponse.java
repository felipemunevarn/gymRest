package com.epam.gym.dto;

public record TraineeTrainingResponse(

        String trainingName,
        String trainingDate,
        String trainingType,
        int trainingDuration,
        String trainerName
) {}
