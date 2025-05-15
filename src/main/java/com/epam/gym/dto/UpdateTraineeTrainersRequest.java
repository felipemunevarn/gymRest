package com.epam.gym.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdateTraineeTrainersRequest(

        @NotEmpty
        List<@NotBlank String> trainerUsernames
) {}
