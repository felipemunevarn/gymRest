package com.epam.gym.controller;

import com.epam.gym.dto.*;
import com.epam.gym.exception.InvalidTokenException;
import com.epam.gym.service.TraineeService;
import com.epam.gym.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private TraineeController traineeController;

    private TraineeRegistrationRequest registrationRequest;
    private TraineeRegistrationResponse registrationResponse;
    private TraineeProfileResponse profileResponse;
    private TraineeUpdateRequest updateRequest;
    private TraineeTrainingRequest trainingRequest;
    private List<TraineeTrainingResponse> trainingResponses;
    private UpdateTraineeTrainersRequest updateTrainersRequest;
    private TraineeTrainerResponse trainerResponse;
    private ActivateUserRequest activateRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        registrationRequest = new TraineeRegistrationRequest(
                "John", "Doe", LocalDate.of(1990, 1, 1), "123 Main St"
        );


        profileResponse = new TraineeProfileResponse(
                "John", "Doe", LocalDate.of(1990, 1, 1),
                "123 Main St", true, Arrays.asList()
        );

        updateRequest = new TraineeUpdateRequest(
                "john.doe", "John", "Doe", LocalDate.of(1990, 1, 1),
                "456 Oak St", true
        );

        trainingRequest = new TraineeTrainingRequest(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31),
                "trainer1", "FITNESS"
        );

        trainingResponses = Arrays.asList(
                new TraineeTrainingResponse("Training1", LocalDate.now().toString(),
                        "FITNESS", 60, "trainer1"),
                new TraineeTrainingResponse("Training2", LocalDate.now().toString(),
                        "CARDIO", 45, "trainer2")
        );

        updateTrainersRequest = new UpdateTraineeTrainersRequest(
                Arrays.asList("trainer1", "trainer2")
        );

        trainerResponse = new TraineeTrainerResponse(
                Arrays.asList(
                        new TrainerDto("trainer1", "John", "Smith", "FITNESS"),
                        new TrainerDto("trainer2", "Jane", "Wilson", "CARDIO")
                )
        );

        activateRequest = new ActivateUserRequest("john.doe", true);
    }

    @Test
    void testRegisterTrainee_Success() {
        // Given
        when(traineeService.createTrainee(registrationRequest)).thenReturn(registrationResponse);

        // When
        ResponseEntity<TraineeRegistrationResponse> result =
                traineeController.registerTrainee(registrationRequest);

        // Then
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(registrationResponse, result.getBody());
        verify(traineeService).createTrainee(registrationRequest);
    }

    @Test
    void testGetTraineeTrainings_Success_AllParameters() {
        // Given
        String username = "john.doe";
        String token = "valid-token";
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        String trainerName = "trainer1";
        String specialization = "FITNESS";

        when(trainingService.getTraineeTrainings(eq(username), any(TraineeTrainingRequest.class)))
                .thenReturn(trainingResponses);

        // When
        ResponseEntity<List<TraineeTrainingResponse>> result =
                traineeController.getTraineeTrainings(username, from, to, trainerName, specialization);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(trainingResponses, result.getBody());
        verify(trainingService).getTraineeTrainings(eq(username), any(TraineeTrainingRequest.class));
    }

    @Test
    void testGetTraineeTrainings_Success_NullParameters() {
        // Given
        String username = "john.doe";
        String token = "valid-token";

        when(trainingService.getTraineeTrainings(eq(username), any(TraineeTrainingRequest.class)))
                .thenReturn(trainingResponses);

        // When
        ResponseEntity<List<TraineeTrainingResponse>> result =
                traineeController.getTraineeTrainings(username, null, null, null, null);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(trainingResponses, result.getBody());
        verify(trainingService).getTraineeTrainings(eq(username), any(TraineeTrainingRequest.class));
    }

//    @Test
//    void testGetTraineeTrainings_InvalidToken_ThrowsException() {
//        // Given
//        String username = "john.doe";
//        String token = "invalid-token";
//
//        // When & Then
//        InvalidTokenException exception = assertThrows(InvalidTokenException.class, () ->
//                traineeController.getTraineeTrainings(username, null, null, null, null));
//
//        assertEquals("Token not authenticated", exception.getMessage());
//        verify(trainingService, never()).getTraineeTrainings(any(), any());
//    }

    @Test
    void testUpdateTraineeTrainers_Success_ValidToken() {
        // Given
        String username = "john.doe";
        String token = "valid-token";
        when(traineeService.updateTraineeTrainers(username, updateTrainersRequest))
                .thenReturn(trainerResponse);

        // When
        ResponseEntity<TraineeTrainerResponse> result =
                traineeController.updateTraineeTrainers(username, updateTrainersRequest);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(trainerResponse, result.getBody());
        verify(traineeService).updateTraineeTrainers(username, updateTrainersRequest);
    }

//    @Test
//    void testUpdateTraineeTrainers_InvalidToken_ThrowsException() {
//        // Given
//        String username = "john.doe";
//        String token = "invalid-token";
//
//        // When & Then
//        InvalidTokenException exception = assertThrows(InvalidTokenException.class, () ->
//                traineeController.updateTraineeTrainers(username, updateTrainersRequest));
//
//        assertEquals("Token not authenticated", exception.getMessage());
//        verify(traineeService, never()).updateTraineeTrainers(any(), any());
//    }

    @Test
    void testUpdateTraineeActivation_Success_ValidToken() {
        // Given
        String token = "valid-token";

        // When
        ResponseEntity<Void> result =
                traineeController.updateTraineeActivation(activateRequest);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(traineeService).changeActiveStatus(activateRequest.username(), activateRequest.isActive());
    }

    @Test
    void testUpdateTraineeActivation_Success_DeactivateUser() {
        // Given
        String token = "valid-token";
        ActivateUserRequest deactivateRequest = new ActivateUserRequest("john.doe", false);

        // When
        ResponseEntity<Void> result =
                traineeController.updateTraineeActivation(deactivateRequest);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(traineeService).changeActiveStatus(deactivateRequest.username(), false);
    }

//    @Test
//    void testUpdateTraineeActivation_InvalidToken_ThrowsException() {
//        // Given
//        String token = "invalid-token";
//
//        // When & Then
//        InvalidTokenException exception = assertThrows(InvalidTokenException.class, () ->
//                traineeController.updateTraineeActivation(activateRequest));
//
//        assertEquals("Token not authenticated", exception.getMessage());
//        verify(traineeService, never()).changeActiveStatus(any(), anyBoolean());
//    }

    // Additional edge case tests for better coverage
    @Test
    void testGetTraineeTrainings_Success_PartialParameters() {
        // Given
        String username = "john.doe";
        String token = "valid-token";
        LocalDate from = LocalDate.of(2024, 1, 1);
        String trainerName = "trainer1";

        when(trainingService.getTraineeTrainings(eq(username), any(TraineeTrainingRequest.class)))
                .thenReturn(trainingResponses);

        // When
        ResponseEntity<List<TraineeTrainingResponse>> result =
                traineeController.getTraineeTrainings(username, from, null, trainerName, null);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(trainingResponses, result.getBody());
        verify(trainingService).getTraineeTrainings(eq(username), any(TraineeTrainingRequest.class));
    }

    @Test
    void testGetTraineeTrainings_Success_EmptyResponse() {
        // Given
        String username = "john.doe";
        String token = "valid-token";
        List<TraineeTrainingResponse> emptyResponse = Arrays.asList();

        when(trainingService.getTraineeTrainings(eq(username), any(TraineeTrainingRequest.class)))
                .thenReturn(emptyResponse);

        // When
        ResponseEntity<List<TraineeTrainingResponse>> result =
                traineeController.getTraineeTrainings(username, null, null, null, null);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(emptyResponse, result.getBody());
        assertTrue(result.getBody().isEmpty());
    }
}