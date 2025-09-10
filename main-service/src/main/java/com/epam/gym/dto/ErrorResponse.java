package com.epam.gym.dto;

public record ErrorResponse(
        int status,
        String error,
        String path,
        String message
) {}
