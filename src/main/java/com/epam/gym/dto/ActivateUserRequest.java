package com.epam.gym.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ActivateUserRequest(
        @NotBlank
        String username,

        @NotNull
        Boolean isActive
) {
}
