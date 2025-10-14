package com.epam.gym.workload.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private MethodParameter methodParameter;

    // ========== MethodArgumentNotValidException Tests ==========

//    @Test
//    void testHandleValidationExceptions_SingleFieldError() {
//        // Arrange
//        FieldError fieldError = new FieldError(
//                "trainerWorkloadRequest",
//                "trainerUsername",
//                "Username is required"
//        );
//
//        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));
//
//        MethodArgumentNotValidException exception =
//                new MethodArgumentNotValidException(methodParameter, bindingResult);
//
//        // Act
//        ResponseEntity<Map<String, String>> response =
//                globalExceptionHandler.handleValidationExceptions(exception);
//
//        // Assert
//        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertEquals(1, response.getBody().size());
//        assertEquals("Username is required", response.getBody().get("trainerUsername"));
//    }
//
//    @Test
//    void testHandleValidationExceptions_MultipleFieldErrors() {
//        // Arrange
//        FieldError fieldError1 = new FieldError(
//                "trainerWorkloadRequest",
//                "trainerUsername",
//                "Username is required"
//        );
//
//        FieldError fieldError2 = new FieldError(
//                "trainerWorkloadRequest",
//                "trainingDate",
//                "Training date cannot be null"
//        );
//
//        FieldError fieldError3 = new FieldError(
//                "trainerWorkloadRequest",
//                "trainingDuration",
//                "Duration must be positive"
//        );
//
//        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2, fieldError3));
//
//        MethodArgumentNotValidException exception =
//                new MethodArgumentNotValidException(methodParameter, bindingResult);
//
//        // Act
//        ResponseEntity<Map<String, String>> response =
//                globalExceptionHandler.handleValidationExceptions(exception);
//
//        // Assert
//        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertEquals(3, response.getBody().size());
//        assertEquals("Username is required", response.getBody().get("trainerUsername"));
//        assertEquals("Training date cannot be null", response.getBody().get("trainingDate"));
//        assertEquals("Duration must be positive", response.getBody().get("trainingDuration"));
//    }
//
//    @Test
//    void testHandleValidationExceptions_EmptyErrors() {
//        // Arrange
//        when(bindingResult.getAllErrors()).thenReturn(List.of());
//
//        MethodArgumentNotValidException exception =
//                new MethodArgumentNotValidException(methodParameter, bindingResult);
//
//        // Act
//        ResponseEntity<Map<String, String>> response =
//                globalExceptionHandler.handleValidationExceptions(exception);
//
//        // Assert
//        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertTrue(response.getBody().isEmpty());
//    }
//
//    @Test
//    void testHandleValidationExceptions_DuplicateFieldName_LastErrorWins() {
//        // Arrange
//        FieldError fieldError1 = new FieldError(
//                "trainerWorkloadRequest",
//                "trainerUsername",
//                "Username is required"
//        );
//
//        FieldError fieldError2 = new FieldError(
//                "trainerWorkloadRequest",
//                "trainerUsername",
//                "Username must be at least 3 characters"
//        );
//
//        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));
//
//        MethodArgumentNotValidException exception =
//                new MethodArgumentNotValidException(methodParameter, bindingResult);
//
//        // Act
//        ResponseEntity<Map<String, String>> response =
//                globalExceptionHandler.handleValidationExceptions(exception);
//
//        // Assert
//        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertEquals(1, response.getBody().size());
//        // Last error overwrites first
//        assertEquals("Username must be at least 3 characters", response.getBody().get("trainerUsername"));
//    }

    // ========== IllegalArgumentException Tests ==========

    @Test
    void testHandleIllegalArgumentException_WithMessage() {
        // Arrange
        String errorMessage = "Invalid trainer username format";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        // Act
        ResponseEntity<Map<String, String>> response =
                globalExceptionHandler.handleIllegalArgumentException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(errorMessage, response.getBody().get("error"));
    }

    @Test
    void testHandleIllegalArgumentException_WithNullMessage() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException((String) null);

        // Act
        ResponseEntity<Map<String, String>> response =
                globalExceptionHandler.handleIllegalArgumentException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertNull(response.getBody().get("error"));
    }

    @Test
    void testHandleIllegalArgumentException_WithEmptyMessage() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("");

        // Act
        ResponseEntity<Map<String, String>> response =
                globalExceptionHandler.handleIllegalArgumentException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("", response.getBody().get("error"));
    }

    // ========== General Exception Tests ==========

    @Test
    void testHandleGeneralException_RuntimeException() {
        // Arrange
        RuntimeException exception = new RuntimeException("Database connection failed");

        // Act
        ResponseEntity<Map<String, String>> response =
                globalExceptionHandler.handleGeneralException(exception);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("An unexpected error occurred", response.getBody().get("error"));
    }

    @Test
    void testHandleGeneralException_NullPointerException() {
        // Arrange
        NullPointerException exception = new NullPointerException("Object is null");

        // Act
        ResponseEntity<Map<String, String>> response =
                globalExceptionHandler.handleGeneralException(exception);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("An unexpected error occurred", response.getBody().get("error"));
    }

    @Test
    void testHandleGeneralException_CheckedException() {
        // Arrange
        Exception exception = new Exception("Something went wrong");

        // Act
        ResponseEntity<Map<String, String>> response =
                globalExceptionHandler.handleGeneralException(exception);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("An unexpected error occurred", response.getBody().get("error"));
    }

    @Test
    void testHandleGeneralException_ExceptionWithCause() {
        // Arrange
        Exception cause = new RuntimeException("Root cause");
        Exception exception = new Exception("Wrapper exception", cause);

        // Act
        ResponseEntity<Map<String, String>> response =
                globalExceptionHandler.handleGeneralException(exception);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("An unexpected error occurred", response.getBody().get("error"));
    }

    @Test
    void testHandleGeneralException_WithNullMessage() {
        // Arrange
        Exception exception = new Exception((String) null);

        // Act
        ResponseEntity<Map<String, String>> response =
                globalExceptionHandler.handleGeneralException(exception);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("An unexpected error occurred", response.getBody().get("error"));
    }

    // ========== Response Structure Tests ==========

//    @Test
//    void testHandleValidationExceptions_ResponseStructure() {
//        // Arrange
//        FieldError fieldError = new FieldError("object", "field", "message");
//        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));
//        MethodArgumentNotValidException exception =
//                new MethodArgumentNotValidException(methodParameter, bindingResult);
//
//        // Act
//        ResponseEntity<Map<String, String>> response =
//                globalExceptionHandler.handleValidationExceptions(exception);
//
//        // Assert
//        assertNotNull(response);
//        assertNotNull(response.getBody());
//        assertTrue(response.getBody() instanceof Map);
//        assertFalse(response.getBody().isEmpty());
//    }

    @Test
    void testHandleIllegalArgumentException_ResponseStructure() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Error");

        // Act
        ResponseEntity<Map<String, String>> response =
                globalExceptionHandler.handleIllegalArgumentException(exception);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        assertTrue(response.getBody().containsKey("error"));
    }

    @Test
    void testHandleGeneralException_ResponseStructure() {
        // Arrange
        Exception exception = new Exception("Error");

        // Act
        ResponseEntity<Map<String, String>> response =
                globalExceptionHandler.handleGeneralException(exception);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        assertTrue(response.getBody().containsKey("error"));
    }
}