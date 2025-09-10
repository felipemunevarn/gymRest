package com.epam.gym.dto;

import com.epam.gym.entity.TrainingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record TrainerRegistrationRequest(
        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotBlank(message = "Specialization is required")
        String specialization
) {}
