package com.epam.gym.exception;

public class TraineeCreationException extends RuntimeException {

    public TraineeCreationException(String message) {
        super(message);
    }

    public TraineeCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
