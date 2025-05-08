package com.epam.gym.dto;

import java.time.LocalDate;
import java.util.List;

public record TraineeProfileResponse(
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String address,
        boolean isActive,
        List<TrainerDto> trainers
) {}
