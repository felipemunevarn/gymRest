package com.epam.gym.dto;

public record TokenValidationResponse (
    boolean valid,
    String username // Null if token is invalid
){}

// Assuming you have an InvalidTokenException class (in a separate file):
// package com.epam.gym.exception;
//
// public class InvalidTokenException extends RuntimeException {
//     public InvalidTokenException(String message) {
//         super(message);
//     }
// }