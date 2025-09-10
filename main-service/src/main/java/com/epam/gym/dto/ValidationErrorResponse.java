package com.epam.gym.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a structured response for validation errors.
 */
public class ValidationErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String path;
    private List<ErrorDetail> details;

    public ValidationErrorResponse(int status, String error, String path, List<ErrorDetail> details) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.path = path;
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getPath() {
        return path;
    }

    public List<ErrorDetail> getDetails() {
        return details;
    }
}
