package com.epam.gym.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record TrainingRegistrationRequest(

        @NotBlank(message = "trainee name is required")
        String traineeUsername,

        @NotBlank(message = "trainer name is required")
        String trainerUsername,

        @NotBlank(message = "name is required")
        String name,

        @Past(message = "training date must be past")
        LocalDate date,

        @Positive(message = "duration must be positive")
        int duration

) {}
