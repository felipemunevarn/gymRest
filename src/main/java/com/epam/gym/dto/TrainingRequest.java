package com.epam.gym.dto;

import java.time.LocalDate;

public record TrainingRequest(

        LocalDate from,

        LocalDate to,

        String trainerName,

        String specialization
) {}
