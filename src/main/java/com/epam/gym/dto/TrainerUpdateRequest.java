package com.epam.gym.dto;

import com.epam.gym.entity.TrainingType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record TrainerUpdateRequest(

        @NotBlank(message = "username is required")
        String username,

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        String specialization,

        @NotBlank(message = "isActive is required")
        boolean isActive
) {}
