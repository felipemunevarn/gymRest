package com.epam.gym.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TrainerUpdateRequest(

        @NotBlank(message = "username is required")
        String username,

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        String specialization,

        @NotNull(message = "isActive is required")
        boolean isActive
) {}
