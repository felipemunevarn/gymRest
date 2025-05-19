package com.epam.gym.controller;

import com.epam.gym.dto.*;
import com.epam.gym.exception.InvalidTokenException;
import com.epam.gym.service.FacadeService;
import com.epam.gym.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TraineeControllerTest {

    @Mock
    private FacadeService facadeService;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private TraineeController traineeController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerTrainee_ShouldReturnCreated() {
        TraineeRegistrationRequest request = mock(TraineeRegistrationRequest.class);
        TraineeRegistrationResponse response = mock(TraineeRegistrationResponse.class);

        when(facadeService.registerTrainee(request)).thenReturn(response);

        ResponseEntity<TraineeRegistrationResponse> result = traineeController.registerTrainee(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void getTrainee_ValidToken_ShouldReturnProfile() {
        String username = "john";
        String token = "validToken";
        TraineeProfileResponse response = mock(TraineeProfileResponse.class);

        when(tokenService.isValidToken(token)).thenReturn(true);
        when(facadeService.getTraineeByUsername(username)).thenReturn(response);

        ResponseEntity<TraineeProfileResponse> result = traineeController.getTrainee(username, token);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void getTrainee_InvalidToken_ShouldThrowException() {
        String username = "john";
        String token = "invalidToken";

        when(tokenService.isValidToken(token)).thenReturn(false);

        assertThrows(InvalidTokenException.class, () ->
                traineeController.getTrainee(username, token));
    }

    @Test
    void updateTrainee_ValidToken_ShouldReturnUpdatedProfile() {
        TraineeUpdateRequest request = mock(TraineeUpdateRequest.class);
        String token = "validToken";
        TraineeProfileResponse response = mock(TraineeProfileResponse.class);

        when(tokenService.isValidToken(token)).thenReturn(true);
        when(facadeService.updateTrainee(request)).thenReturn(response);

        ResponseEntity<TraineeProfileResponse> result = traineeController.updateTrainee(request, token);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void deleteTrainee_ValidToken_ShouldReturnNoContent() {
        String username = "john";
        String token = "validToken";

        when(tokenService.isValidToken(token)).thenReturn(true);

        ResponseEntity<Void> result = traineeController.deleteTrainee(username, token);

        verify(facadeService).deleteTrainee(username);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
    }

    @Test
    void getTraineeTrainings_ValidToken_ShouldReturnList() {
        String username = "john";
        String token = "validToken";
        LocalDate from = LocalDate.now().minusDays(10);
        LocalDate to = LocalDate.now();
        String trainer = "coach";
        String spec = "CARDIO";
        List<TraineeTrainingResponse> response = List.of(mock(TraineeTrainingResponse.class));

        when(tokenService.isValidToken(token)).thenReturn(true);
        when(facadeService.findTraineeTrainings(eq(username), any())).thenReturn(response);

        ResponseEntity<List<TraineeTrainingResponse>> result = traineeController.getTraineeTrainings(username, from, to, trainer, spec, token);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void updateTraineeTrainers_ValidToken_ShouldReturnResponse() {
        String username = "john";
        String token = "validToken";
        UpdateTraineeTrainersRequest request = mock(UpdateTraineeTrainersRequest.class);
        TraineeTrainerResponse response = mock(TraineeTrainerResponse.class);

        when(tokenService.isValidToken(token)).thenReturn(true);
        when(facadeService.updateTraineeTrainers(username, request)).thenReturn(response);

        ResponseEntity<TraineeTrainerResponse> result = traineeController.updateTraineeTrainers(username, request, token);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void updateTraineeActivation_ValidToken_ShouldReturnNoContent() {
        ActivateUserRequest request = new ActivateUserRequest("john", true);
        String token = "validToken";

        when(tokenService.isValidToken(token)).thenReturn(true);

        ResponseEntity<Void> result = traineeController.updateTraineeActivation(request, token);

        verify(facadeService).changeTraineeActiveStatus("john", true);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
    }

    @Test
    void updateTraineeActivation_InvalidToken_ShouldThrowException() {
        ActivateUserRequest request = new ActivateUserRequest("john", true);
        String token = "invalidToken";

        when(tokenService.isValidToken(token)).thenReturn(false);

        assertThrows(InvalidTokenException.class, () ->
                traineeController.updateTraineeActivation(request, token));
    }
}
