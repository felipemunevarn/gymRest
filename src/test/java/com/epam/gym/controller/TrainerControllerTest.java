package com.epam.gym.controller;

import com.epam.gym.dto.*;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.TrainingTypeEnum;
import com.epam.gym.exception.InvalidTokenException;
import com.epam.gym.service.TokenService;
import com.epam.gym.service.TrainerService;
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
class TrainerControllerTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private TrainerController trainerController;

    private TrainerRegistrationRequest registrationRequest;
    private TrainerRegistrationResponse registrationResponse;
    private TrainerProfileResponse profileResponse;
    private TrainerUpdateRequest updateRequest;
    private List<TrainerDto> availableTrainers;
    private TrainerTrainingRequest trainingRequest;
    private List<TrainerTrainingResponse> trainingResponses;
    private ActivateUserRequest activateRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        registrationRequest = new TrainerRegistrationRequest(
                "John", "Smith", "FITNESS"
        );

        registrationResponse = new TrainerRegistrationResponse(
                "john.smith", "tempPassword123"
        );

        profileResponse = new TrainerProfileResponse(
                "John", "Smith", new TrainingType(TrainingTypeEnum.STRENGTH), true,
                Arrays.asList(new TraineeDto("jane.doe", "Jane", "Doe"))
        );

        updateRequest = new TrainerUpdateRequest(
                "john.smith", "John", "Smith", "FITNESS", true
        );

        availableTrainers = Arrays.asList(
                new TrainerDto("trainer1", "John", "Smith", "FITNESS"),
                new TrainerDto("trainer2", "Jane", "Wilson", "CARDIO"),
                new TrainerDto("trainer3", "Mike", "Johnson", "STRENGTH")
        );

        trainingRequest = new TrainerTrainingRequest(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), "jane.doe"
        );

        trainingResponses = Arrays.asList(
                new TrainerTrainingResponse("Training1", LocalDate.now().toString(),
                        "FITNESS", 60, "jane.doe"),
                new TrainerTrainingResponse("Training2", LocalDate.now().toString(),
                        "FITNESS", 45, "john.doe")
        );

        activateRequest = new ActivateUserRequest("john.smith", true);
    }

    @Test
    void testRegisterTrainer_Success() {
        // Given
        when(trainerService.createTrainer(registrationRequest)).thenReturn(registrationResponse);

        // When
        ResponseEntity<TrainerRegistrationResponse> result =
                trainerController.registerTrainer(registrationRequest);

        // Then
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(registrationResponse, result.getBody());
        verify(trainerService).createTrainer(registrationRequest);
    }

    @Test
    void testGetTrainer_Success_ValidToken() {
        // Given
        String username = "john.smith";
        String token = "valid-token";
        when(tokenService.isValidToken(username, token)).thenReturn(true);
        when(trainerService.findTrainerByUsername(username)).thenReturn(profileResponse);

        // When
        ResponseEntity<TrainerProfileResponse> result =
                trainerController.getTrainer(username, token);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(profileResponse, result.getBody());
        verify(tokenService).isValidToken(username, token);
        verify(trainerService).findTrainerByUsername(username);
    }

    @Test
    void testGetTrainer_InvalidToken_ThrowsException() {
        // Given
        String username = "john.smith";
        String token = "invalid-token";
        when(tokenService.isValidToken(username, token)).thenReturn(false);

        // When & Then
        InvalidTokenException exception = assertThrows(InvalidTokenException.class, () ->
                trainerController.getTrainer(username, token));

        assertEquals("Token not authenticated", exception.getMessage());
        verify(tokenService).isValidToken(username, token);
        verify(trainerService, never()).findTrainerByUsername(any());
    }

    @Test
    void testUpdateTrainer_Success_ValidToken() {
        // Given
        String token = "valid-token";
        when(tokenService.isValidToken(updateRequest.username(), token)).thenReturn(true);
        when(trainerService.updateTrainer(updateRequest)).thenReturn(profileResponse);

        // When
        ResponseEntity<TrainerProfileResponse> result =
                trainerController.updateTrainer(updateRequest, token);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(profileResponse, result.getBody());
        verify(tokenService).isValidToken(updateRequest.username(), token);
        verify(trainerService).updateTrainer(updateRequest);
    }

    @Test
    void testUpdateTrainer_InvalidToken_ThrowsException() {
        // Given
        String token = "invalid-token";
        when(tokenService.isValidToken(updateRequest.username(), token)).thenReturn(false);

        // When & Then
        InvalidTokenException exception = assertThrows(InvalidTokenException.class, () ->
                trainerController.updateTrainer(updateRequest, token));

        assertEquals("Token not authenticated", exception.getMessage());
        verify(tokenService).isValidToken(updateRequest.username(), token);
        verify(trainerService, never()).updateTrainer(any());
    }

    @Test
    void testGetAvailableTrainers_Success_ValidToken() {
        // Given
        String traineeUsername = "jane.doe";
        String token = "valid-token";
        when(tokenService.isValidToken(traineeUsername, token)).thenReturn(true);
        when(trainerService.getAvailableTrainersForTrainee(traineeUsername)).thenReturn(availableTrainers);

        // When
        ResponseEntity<List<TrainerDto>> result =
                trainerController.getAvailableTrainers(traineeUsername, token);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(availableTrainers, result.getBody());
        assertEquals(3, result.getBody().size());
        verify(tokenService).isValidToken(traineeUsername, token);
        verify(trainerService).getAvailableTrainersForTrainee(traineeUsername);
    }

    @Test
    void testGetAvailableTrainers_Success_EmptyList() {
        // Given
        String traineeUsername = "jane.doe";
        String token = "valid-token";
        List<TrainerDto> emptyList = Arrays.asList();
        when(tokenService.isValidToken(traineeUsername, token)).thenReturn(true);
        when(trainerService.getAvailableTrainersForTrainee(traineeUsername)).thenReturn(emptyList);

        // When
        ResponseEntity<List<TrainerDto>> result =
                trainerController.getAvailableTrainers(traineeUsername, token);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(emptyList, result.getBody());
        assertTrue(result.getBody().isEmpty());
        verify(tokenService).isValidToken(traineeUsername, token);
        verify(trainerService).getAvailableTrainersForTrainee(traineeUsername);
    }

    @Test
    void testGetAvailableTrainers_InvalidToken_ThrowsException() {
        // Given
        String traineeUsername = "jane.doe";
        String token = "invalid-token";
        when(tokenService.isValidToken(traineeUsername, token)).thenReturn(false);

        // When & Then
        InvalidTokenException exception = assertThrows(InvalidTokenException.class, () ->
                trainerController.getAvailableTrainers(traineeUsername, token));

        assertEquals("Token not authenticated", exception.getMessage());
        verify(tokenService).isValidToken(traineeUsername, token);
        verify(trainerService, never()).getAvailableTrainersForTrainee(any());
    }

    @Test
    void testGetTrainerTrainings_Success_AllParameters() {
        // Given
        String username = "john.smith";
        String token = "valid-token";
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        String traineeName = "jane.doe";

        when(tokenService.isValidToken(username, token)).thenReturn(true);
        when(trainingService.getTrainerTrainings(eq(username), any(TrainerTrainingRequest.class)))
                .thenReturn(trainingResponses);

        // When
        ResponseEntity<List<TrainerTrainingResponse>> result =
                trainerController.getTrainerTrainings(username, from, to, traineeName, token);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(trainingResponses, result.getBody());
        verify(tokenService).isValidToken(username, token);
        verify(trainingService).getTrainerTrainings(eq(username), any(TrainerTrainingRequest.class));
    }

    @Test
    void testGetTrainerTrainings_Success_NullParameters() {
        // Given
        String username = "john.smith";
        String token = "valid-token";

        when(tokenService.isValidToken(username, token)).thenReturn(true);
        when(trainingService.getTrainerTrainings(eq(username), any(TrainerTrainingRequest.class)))
                .thenReturn(trainingResponses);

        // When
        ResponseEntity<List<TrainerTrainingResponse>> result =
                trainerController.getTrainerTrainings(username, null, null, null, token);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(trainingResponses, result.getBody());
        verify(tokenService).isValidToken(username, token);
        verify(trainingService).getTrainerTrainings(eq(username), any(TrainerTrainingRequest.class));
    }

    @Test
    void testGetTrainerTrainings_Success_PartialParameters() {
        // Given
        String username = "john.smith";
        String token = "valid-token";
        LocalDate from = LocalDate.of(2024, 1, 1);
        String traineeName = "jane.doe";

        when(tokenService.isValidToken(username, token)).thenReturn(true);
        when(trainingService.getTrainerTrainings(eq(username), any(TrainerTrainingRequest.class)))
                .thenReturn(trainingResponses);

        // When
        ResponseEntity<List<TrainerTrainingResponse>> result =
                trainerController.getTrainerTrainings(username, from, null, traineeName, token);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(trainingResponses, result.getBody());
        verify(tokenService).isValidToken(username, token);
        verify(trainingService).getTrainerTrainings(eq(username), any(TrainerTrainingRequest.class));
    }

    @Test
    void testGetTrainerTrainings_Success_EmptyResponse() {
        // Given
        String username = "john.smith";
        String token = "valid-token";
        List<TrainerTrainingResponse> emptyResponse = Arrays.asList();

        when(tokenService.isValidToken(username, token)).thenReturn(true);
        when(trainingService.getTrainerTrainings(eq(username), any(TrainerTrainingRequest.class)))
                .thenReturn(emptyResponse);

        // When
        ResponseEntity<List<TrainerTrainingResponse>> result =
                trainerController.getTrainerTrainings(username, null, null, null, token);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(emptyResponse, result.getBody());
        assertTrue(result.getBody().isEmpty());
        verify(tokenService).isValidToken(username, token);
        verify(trainingService).getTrainerTrainings(eq(username), any(TrainerTrainingRequest.class));
    }

    @Test
    void testGetTrainerTrainings_InvalidToken_ThrowsException() {
        // Given
        String username = "john.smith";
        String token = "invalid-token";
        when(tokenService.isValidToken(username, token)).thenReturn(false);

        // When & Then
        InvalidTokenException exception = assertThrows(InvalidTokenException.class, () ->
                trainerController.getTrainerTrainings(username, null, null, null, token));

        assertEquals("Token not authenticated", exception.getMessage());
        verify(tokenService).isValidToken(username, token);
        verify(trainingService, never()).getTrainerTrainings(any(), any());
    }

    @Test
    void testUpdateTrainerActivation_Success_ValidToken() {
        // Given
        String token = "valid-token";
        when(tokenService.isValidToken(activateRequest.username(), token)).thenReturn(true);

        // When
        ResponseEntity<Void> result =
                trainerController.updateTrainerActivation(activateRequest, token);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(tokenService).isValidToken(activateRequest.username(), token);
        verify(trainerService).changeActiveStatus(activateRequest.username(), activateRequest.isActive());
    }

    @Test
    void testUpdateTrainerActivation_Success_DeactivateUser() {
        // Given
        String token = "valid-token";
        ActivateUserRequest deactivateRequest = new ActivateUserRequest("john.smith", false);
        when(tokenService.isValidToken(deactivateRequest.username(), token)).thenReturn(true);

        // When
        ResponseEntity<Void> result =
                trainerController.updateTrainerActivation(deactivateRequest, token);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(tokenService).isValidToken(deactivateRequest.username(), token);
        verify(trainerService).changeActiveStatus(deactivateRequest.username(), false);
    }

    @Test
    void testUpdateTrainerActivation_InvalidToken_ThrowsException() {
        // Given
        String token = "invalid-token";
        when(tokenService.isValidToken(activateRequest.username(), token)).thenReturn(false);

        // When & Then
        InvalidTokenException exception = assertThrows(InvalidTokenException.class, () ->
                trainerController.updateTrainerActivation(activateRequest, token));

        assertEquals("Token not authenticated", exception.getMessage());
        verify(tokenService).isValidToken(activateRequest.username(), token);
        verify(trainerService, never()).changeActiveStatus(any(), anyBoolean());
    }

    @Test
    void testConstructor_AllDependenciesInjected() {
        // Given & When
        TrainerController controller = new TrainerController(tokenService, trainerService, trainingService);

        // Then
        assertNotNull(controller);
        // Verify that all dependencies are properly injected by testing one method
        when(trainerService.createTrainer(registrationRequest)).thenReturn(registrationResponse);
        ResponseEntity<TrainerRegistrationResponse> result = controller.registerTrainer(registrationRequest);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
    }

    // Additional edge case tests for comprehensive coverage
    @Test
    void testGetTrainerTrainings_Success_OnlyFromDate() {
        // Given
        String username = "john.smith";
        String token = "valid-token";
        LocalDate from = LocalDate.of(2024, 6, 1);

        when(tokenService.isValidToken(username, token)).thenReturn(true);
        when(trainingService.getTrainerTrainings(eq(username), any(TrainerTrainingRequest.class)))
                .thenReturn(trainingResponses);

        // When
        ResponseEntity<List<TrainerTrainingResponse>> result =
                trainerController.getTrainerTrainings(username, from, null, null, token);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(trainingResponses, result.getBody());
        verify(tokenService).isValidToken(username, token);
        verify(trainingService).getTrainerTrainings(eq(username), any(TrainerTrainingRequest.class));
    }

    @Test
    void testGetTrainerTrainings_Success_OnlyToDate() {
        // Given
        String username = "john.smith";
        String token = "valid-token";
        LocalDate to = LocalDate.of(2024, 12, 31);

        when(tokenService.isValidToken(username, token)).thenReturn(true);
        when(trainingService.getTrainerTrainings(eq(username), any(TrainerTrainingRequest.class)))
                .thenReturn(trainingResponses);

        // When
        ResponseEntity<List<TrainerTrainingResponse>> result =
                trainerController.getTrainerTrainings(username, null, to, null, token);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(trainingResponses, result.getBody());
        verify(tokenService).isValidToken(username, token);
        verify(trainingService).getTrainerTrainings(eq(username), any(TrainerTrainingRequest.class));
    }

    @Test
    void testGetTrainerTrainings_Success_OnlyTraineeName() {
        // Given
        String username = "john.smith";
        String token = "valid-token";
        String traineeName = "jane.doe";

        when(tokenService.isValidToken(username, token)).thenReturn(true);
        when(trainingService.getTrainerTrainings(eq(username), any(TrainerTrainingRequest.class)))
                .thenReturn(trainingResponses);

        // When
        ResponseEntity<List<TrainerTrainingResponse>> result =
                trainerController.getTrainerTrainings(username, null, null, traineeName, token);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(trainingResponses, result.getBody());
        verify(tokenService).isValidToken(username, token);
        verify(trainingService).getTrainerTrainings(eq(username), any(TrainerTrainingRequest.class));
    }

    @Test
    void testGetAvailableTrainers_Success_SingleTrainer() {
        // Given
        String traineeUsername = "jane.doe";
        String token = "valid-token";
        List<TrainerDto> singleTrainer = Arrays.asList(
                new TrainerDto("trainer1", "John", "Smith", "FITNESS")
        );
        when(tokenService.isValidToken(traineeUsername, token)).thenReturn(true);
        when(trainerService.getAvailableTrainersForTrainee(traineeUsername)).thenReturn(singleTrainer);

        // When
        ResponseEntity<List<TrainerDto>> result =
                trainerController.getAvailableTrainers(traineeUsername, token);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(singleTrainer, result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals("trainer1", result.getBody().get(0).username());
        verify(tokenService).isValidToken(traineeUsername, token);
        verify(trainerService).getAvailableTrainersForTrainee(traineeUsername);
    }
}