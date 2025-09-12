package com.epam.gym.service;

import com.epam.gym.client.TrainerWorkloadClient;
import com.epam.gym.dto.TrainerWorkloadRequest;
import com.epam.gym.security.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceTest {

    @Mock
    private TrainerWorkloadClient workloadClient;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private TrainerWorkloadService trainerWorkloadService;

    private static final String TRAINER_USERNAME = "john.trainer";
    private static final String TRAINER_FIRST_NAME = "John";
    private static final String TRAINER_LAST_NAME = "Trainer";
    private static final Boolean IS_ACTIVE = true;
    private static final LocalDate TRAINING_DATE = LocalDate.of(2024, 12, 15);
    private static final Integer TRAINING_DURATION = 60;
    private static final String JWT_TOKEN = "test-jwt-token";
    private static final String SYSTEM_USERNAME = "system";

    @BeforeEach
    void setUp() {
        // Set the system username using reflection
        ReflectionTestUtils.setField(trainerWorkloadService, "systemUsername", SYSTEM_USERNAME);
    }

    // ========== ADD TRAINING WORKLOAD TESTS ==========

    @Test
    void addTrainingWorkload_Success() throws ExecutionException, InterruptedException {
        // Arrange
        when(jwtUtil.generateToken(SYSTEM_USERNAME)).thenReturn(JWT_TOKEN);
        when(workloadClient.updateTrainerWorkload(any(TrainerWorkloadRequest.class), anyString()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // Act
        CompletableFuture<Void> result = trainerWorkloadService.addTrainingWorkload(
                TRAINER_USERNAME, TRAINER_FIRST_NAME, TRAINER_LAST_NAME,
                IS_ACTIVE, TRAINING_DATE, TRAINING_DURATION
        );

        // Assert
        assertNotNull(result);
        assertNull(result.get()); // CompletableFuture<Void> returns null on success

        // Verify interactions
        verify(jwtUtil).generateToken(SYSTEM_USERNAME);
        ArgumentCaptor<TrainerWorkloadRequest> requestCaptor = ArgumentCaptor.forClass(TrainerWorkloadRequest.class);
        verify(workloadClient).updateTrainerWorkload(requestCaptor.capture(), eq("Bearer " + JWT_TOKEN));

        // Verify request content
        TrainerWorkloadRequest capturedRequest = requestCaptor.getValue();
        assertEquals(TRAINER_USERNAME, capturedRequest.getTrainerUsername());
        assertEquals(TRAINER_FIRST_NAME, capturedRequest.getTrainerFirstName());
        assertEquals(TRAINER_LAST_NAME, capturedRequest.getTrainerLastName());
        assertEquals(IS_ACTIVE, capturedRequest.getIsActive());
        assertEquals(TRAINING_DATE, capturedRequest.getTrainingDate());
        assertEquals(TRAINING_DURATION, capturedRequest.getTrainingDuration());
        assertEquals(TrainerWorkloadRequest.ActionType.ADD, capturedRequest.getActionType());
    }

    @Test
    void addTrainingWorkload_ClientException() {
        // Arrange
        when(jwtUtil.generateToken(SYSTEM_USERNAME)).thenReturn(JWT_TOKEN);
        when(workloadClient.updateTrainerWorkload(any(TrainerWorkloadRequest.class), anyString()))
                .thenThrow(new RuntimeException("Service unavailable"));

        // Act
        CompletableFuture<Void> result = trainerWorkloadService.addTrainingWorkload(
                TRAINER_USERNAME, TRAINER_FIRST_NAME, TRAINER_LAST_NAME,
                IS_ACTIVE, TRAINING_DATE, TRAINING_DURATION
        );

        // Assert
        ExecutionException exception = assertThrows(ExecutionException.class, result::get);
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Workload service communication failed", exception.getCause().getMessage());
    }

    @Test
    void addTrainingWorkload_JwtException() {
        // Arrange
        when(jwtUtil.generateToken(SYSTEM_USERNAME)).thenThrow(new RuntimeException("JWT generation failed"));

        // Act
        CompletableFuture<Void> result = trainerWorkloadService.addTrainingWorkload(
                TRAINER_USERNAME, TRAINER_FIRST_NAME, TRAINER_LAST_NAME,
                IS_ACTIVE, TRAINING_DATE, TRAINING_DURATION
        );

        // Assert
        ExecutionException exception = assertThrows(ExecutionException.class, result::get);
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Workload service communication failed", exception.getCause().getMessage());
    }

    // ========== DELETE TRAINING WORKLOAD TESTS ==========

    @Test
    void deleteTrainingWorkload_Success() throws ExecutionException, InterruptedException {
        // Arrange
        when(jwtUtil.generateToken(SYSTEM_USERNAME)).thenReturn(JWT_TOKEN);
        when(workloadClient.updateTrainerWorkload(any(TrainerWorkloadRequest.class), anyString()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // Act
        CompletableFuture<Void> result = trainerWorkloadService.deleteTrainingWorkload(
                TRAINER_USERNAME, TRAINER_FIRST_NAME, TRAINER_LAST_NAME,
                IS_ACTIVE, TRAINING_DATE, TRAINING_DURATION
        );

        // Assert
        assertNotNull(result);
        assertNull(result.get()); // CompletableFuture<Void> returns null on success

        // Verify interactions
        verify(jwtUtil).generateToken(SYSTEM_USERNAME);
        ArgumentCaptor<TrainerWorkloadRequest> requestCaptor = ArgumentCaptor.forClass(TrainerWorkloadRequest.class);
        verify(workloadClient).updateTrainerWorkload(requestCaptor.capture(), eq("Bearer " + JWT_TOKEN));

        // Verify request content
        TrainerWorkloadRequest capturedRequest = requestCaptor.getValue();
        assertEquals(TRAINER_USERNAME, capturedRequest.getTrainerUsername());
        assertEquals(TRAINER_FIRST_NAME, capturedRequest.getTrainerFirstName());
        assertEquals(TRAINER_LAST_NAME, capturedRequest.getTrainerLastName());
        assertEquals(IS_ACTIVE, capturedRequest.getIsActive());
        assertEquals(TRAINING_DATE, capturedRequest.getTrainingDate());
        assertEquals(TRAINING_DURATION, capturedRequest.getTrainingDuration());
        assertEquals(TrainerWorkloadRequest.ActionType.DELETE, capturedRequest.getActionType());
    }

    @Test
    void deleteTrainingWorkload_ClientException() {
        // Arrange
        when(jwtUtil.generateToken(SYSTEM_USERNAME)).thenReturn(JWT_TOKEN);
        when(workloadClient.updateTrainerWorkload(any(TrainerWorkloadRequest.class), anyString()))
                .thenThrow(new RuntimeException("Service unavailable"));

        // Act
        CompletableFuture<Void> result = trainerWorkloadService.deleteTrainingWorkload(
                TRAINER_USERNAME, TRAINER_FIRST_NAME, TRAINER_LAST_NAME,
                IS_ACTIVE, TRAINING_DATE, TRAINING_DURATION
        );

        // Assert
        ExecutionException exception = assertThrows(ExecutionException.class, result::get);
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Workload service communication failed", exception.getCause().getMessage());
    }

    @Test
    void deleteTrainingWorkload_JwtException() {
        // Arrange
        when(jwtUtil.generateToken(SYSTEM_USERNAME)).thenThrow(new RuntimeException("JWT generation failed"));

        // Act
        CompletableFuture<Void> result = trainerWorkloadService.deleteTrainingWorkload(
                TRAINER_USERNAME, TRAINER_FIRST_NAME, TRAINER_LAST_NAME,
                IS_ACTIVE, TRAINING_DATE, TRAINING_DURATION
        );

        // Assert
        ExecutionException exception = assertThrows(ExecutionException.class, result::get);
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Workload service communication failed", exception.getCause().getMessage());
    }

    // ========== SYNCHRONOUS METHODS TESTS ==========

    @Test
    void addTrainingWorkloadSync_Success() {
        // Arrange
        when(jwtUtil.generateToken(SYSTEM_USERNAME)).thenReturn(JWT_TOKEN);
        when(workloadClient.updateTrainerWorkload(any(TrainerWorkloadRequest.class), anyString()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> {
            trainerWorkloadService.addTrainingWorkloadSync(
                    TRAINER_USERNAME, TRAINER_FIRST_NAME, TRAINER_LAST_NAME,
                    IS_ACTIVE, TRAINING_DATE, TRAINING_DURATION
            );
        });

        // Verify interactions
        verify(jwtUtil).generateToken(SYSTEM_USERNAME);
        verify(workloadClient).updateTrainerWorkload(any(TrainerWorkloadRequest.class), anyString());
    }

    @Test
    void addTrainingWorkloadSync_Exception() {
        // Arrange
        when(jwtUtil.generateToken(SYSTEM_USERNAME)).thenReturn(JWT_TOKEN);
        when(workloadClient.updateTrainerWorkload(any(TrainerWorkloadRequest.class), anyString()))
                .thenThrow(new RuntimeException("Service unavailable"));

        // Act & Assert - Should not throw exception (catches and logs)
        assertDoesNotThrow(() -> {
            trainerWorkloadService.addTrainingWorkloadSync(
                    TRAINER_USERNAME, TRAINER_FIRST_NAME, TRAINER_LAST_NAME,
                    IS_ACTIVE, TRAINING_DATE, TRAINING_DURATION
            );
        });

        // Verify interactions
        verify(jwtUtil).generateToken(SYSTEM_USERNAME);
        verify(workloadClient).updateTrainerWorkload(any(TrainerWorkloadRequest.class), anyString());
    }

    @Test
    void deleteTrainingWorkloadSync_Success() {
        // Arrange
        when(jwtUtil.generateToken(SYSTEM_USERNAME)).thenReturn(JWT_TOKEN);
        when(workloadClient.updateTrainerWorkload(any(TrainerWorkloadRequest.class), anyString()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> {
            trainerWorkloadService.deleteTrainingWorkloadSync(
                    TRAINER_USERNAME, TRAINER_FIRST_NAME, TRAINER_LAST_NAME,
                    IS_ACTIVE, TRAINING_DATE, TRAINING_DURATION
            );
        });

        // Verify interactions
        verify(jwtUtil).generateToken(SYSTEM_USERNAME);
        verify(workloadClient).updateTrainerWorkload(any(TrainerWorkloadRequest.class), anyString());
    }

    @Test
    void deleteTrainingWorkloadSync_Exception() {
        // Arrange
        when(jwtUtil.generateToken(SYSTEM_USERNAME)).thenReturn(JWT_TOKEN);
        when(workloadClient.updateTrainerWorkload(any(TrainerWorkloadRequest.class), anyString()))
                .thenThrow(new RuntimeException("Service unavailable"));

        // Act & Assert - Should not throw exception (catches and logs)
        assertDoesNotThrow(() -> {
            trainerWorkloadService.deleteTrainingWorkloadSync(
                    TRAINER_USERNAME, TRAINER_FIRST_NAME, TRAINER_LAST_NAME,
                    IS_ACTIVE, TRAINING_DATE, TRAINING_DURATION
            );
        });

        // Verify interactions
        verify(jwtUtil).generateToken(SYSTEM_USERNAME);
        verify(workloadClient).updateTrainerWorkload(any(TrainerWorkloadRequest.class), anyString());
    }

    // ========== FALLBACK METHODS TESTS ==========

    @Test
    void addTrainingWorkloadFallback_Success() throws ExecutionException, InterruptedException {
        // Arrange
        Exception testException = new RuntimeException("Circuit breaker test");

        // Act
        CompletableFuture<Void> result = trainerWorkloadService.addTrainingWorkloadFallback(
                TRAINER_USERNAME, TRAINER_FIRST_NAME, TRAINER_LAST_NAME,
                IS_ACTIVE, TRAINING_DATE, TRAINING_DURATION, testException
        );

        // Assert
        assertNotNull(result);
        assertNull(result.get()); // Should complete successfully with null
        assertTrue(result.isDone());
        assertFalse(result.isCompletedExceptionally());
    }

    @Test
    void deleteTrainingWorkloadFallback_Success() throws ExecutionException, InterruptedException {
        // Arrange
        Exception testException = new RuntimeException("Circuit breaker test");

        // Act
        CompletableFuture<Void> result = trainerWorkloadService.deleteTrainingWorkloadFallback(
                TRAINER_USERNAME, TRAINER_FIRST_NAME, TRAINER_LAST_NAME,
                IS_ACTIVE, TRAINING_DATE, TRAINING_DURATION, testException
        );

        // Assert
        assertNotNull(result);
        assertNull(result.get()); // Should complete successfully with null
        assertTrue(result.isDone());
        assertFalse(result.isCompletedExceptionally());
    }

    // ========== EDGE CASES AND BOUNDARY TESTS ==========

    @Test
    void addTrainingWorkload_WithNullValues() throws ExecutionException, InterruptedException {
        // Arrange
        when(jwtUtil.generateToken(SYSTEM_USERNAME)).thenReturn(JWT_TOKEN);
        when(workloadClient.updateTrainerWorkload(any(TrainerWorkloadRequest.class), anyString()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // Act
        CompletableFuture<Void> result = trainerWorkloadService.addTrainingWorkload(
                null, null, null, null, null, null
        );

        // Assert
        assertNotNull(result);
        assertNull(result.get());

        // Verify request was created with null values
        ArgumentCaptor<TrainerWorkloadRequest> requestCaptor = ArgumentCaptor.forClass(TrainerWorkloadRequest.class);
        verify(workloadClient).updateTrainerWorkload(requestCaptor.capture(), anyString());

        TrainerWorkloadRequest capturedRequest = requestCaptor.getValue();
        assertNull(capturedRequest.getTrainerUsername());
        assertNull(capturedRequest.getTrainerFirstName());
        assertNull(capturedRequest.getTrainerLastName());
        assertNull(capturedRequest.getIsActive());
        assertNull(capturedRequest.getTrainingDate());
        assertNull(capturedRequest.getTrainingDuration());
        assertEquals(TrainerWorkloadRequest.ActionType.ADD, capturedRequest.getActionType());
    }

    @Test
    void deleteTrainingWorkload_WithNullValues() throws ExecutionException, InterruptedException {
        // Arrange
        when(jwtUtil.generateToken(SYSTEM_USERNAME)).thenReturn(JWT_TOKEN);
        when(workloadClient.updateTrainerWorkload(any(TrainerWorkloadRequest.class), anyString()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // Act
        CompletableFuture<Void> result = trainerWorkloadService.deleteTrainingWorkload(
                null, null, null, null, null, null
        );

        // Assert
        assertNotNull(result);
        assertNull(result.get());

        // Verify request was created with null values
        ArgumentCaptor<TrainerWorkloadRequest> requestCaptor = ArgumentCaptor.forClass(TrainerWorkloadRequest.class);
        verify(workloadClient).updateTrainerWorkload(requestCaptor.capture(), anyString());

        TrainerWorkloadRequest capturedRequest = requestCaptor.getValue();
        assertEquals(TrainerWorkloadRequest.ActionType.DELETE, capturedRequest.getActionType());
    }

    @Test
    void addTrainingWorkload_WithInactiveTrainer() throws ExecutionException, InterruptedException {
        // Arrange
        when(jwtUtil.generateToken(SYSTEM_USERNAME)).thenReturn(JWT_TOKEN);
        when(workloadClient.updateTrainerWorkload(any(TrainerWorkloadRequest.class), anyString()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // Act
        CompletableFuture<Void> result = trainerWorkloadService.addTrainingWorkload(
                TRAINER_USERNAME, TRAINER_FIRST_NAME, TRAINER_LAST_NAME,
                false, TRAINING_DATE, TRAINING_DURATION // isActive = false
        );

        // Assert
        assertNotNull(result);
        assertNull(result.get());

        // Verify request was created with isActive = false
        ArgumentCaptor<TrainerWorkloadRequest> requestCaptor = ArgumentCaptor.forClass(TrainerWorkloadRequest.class);
        verify(workloadClient).updateTrainerWorkload(requestCaptor.capture(), anyString());

        TrainerWorkloadRequest capturedRequest = requestCaptor.getValue();
        assertFalse(capturedRequest.getIsActive());
    }

    @Test
    void constructor_WithValidParameters() {
        // Act
        TrainerWorkloadService service = new TrainerWorkloadService(workloadClient, jwtUtil);

        // Assert
        assertNotNull(service);
    }

    // ========== INTEGRATION-STYLE TESTS ==========

    @Test
    void addTrainingWorkload_DifferentHttpStatus() throws ExecutionException, InterruptedException {
        // Arrange
        when(jwtUtil.generateToken(SYSTEM_USERNAME)).thenReturn(JWT_TOKEN);
        when(workloadClient.updateTrainerWorkload(any(TrainerWorkloadRequest.class), anyString()))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        // Act
        CompletableFuture<Void> result = trainerWorkloadService.addTrainingWorkload(
                TRAINER_USERNAME, TRAINER_FIRST_NAME, TRAINER_LAST_NAME,
                IS_ACTIVE, TRAINING_DATE, TRAINING_DURATION
        );

        // Assert
        assertNotNull(result);
        assertNull(result.get()); // Should still complete successfully
    }

    @Test
    void deleteTrainingWorkload_DifferentHttpStatus() throws ExecutionException, InterruptedException {
        // Arrange
        when(jwtUtil.generateToken(SYSTEM_USERNAME)).thenReturn(JWT_TOKEN);
        when(workloadClient.updateTrainerWorkload(any(TrainerWorkloadRequest.class), anyString()))
                .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        // Act
        CompletableFuture<Void> result = trainerWorkloadService.deleteTrainingWorkload(
                TRAINER_USERNAME, TRAINER_FIRST_NAME, TRAINER_LAST_NAME,
                IS_ACTIVE, TRAINING_DATE, TRAINING_DURATION
        );

        // Assert
        assertNotNull(result);
        assertNull(result.get()); // Should still complete successfully
    }
}