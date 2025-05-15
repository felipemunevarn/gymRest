package com.epam.gym.dto;

import java.time.LocalDate;

public record TrainerTrainingRequest(

        LocalDate from,

        LocalDate to,

        String traineeName
) {}
