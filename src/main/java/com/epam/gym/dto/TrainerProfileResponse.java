package com.epam.gym.dto;

import com.epam.gym.entity.TrainingType;

import java.time.LocalDate;
import java.util.List;

public record TrainerProfileResponse(
        String firstName,
        String lastName,
        TrainingType specialization,
        boolean isActive,
        List<TraineeDto> trainees
) {}
