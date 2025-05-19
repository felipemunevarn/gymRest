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

class TrainerControllerTest {

    @Mock
    private FacadeService facadeService;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private TrainerController trainerController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerTrainer_ShouldReturnCreated() {
        TrainerRegistrationRequest request = mock(TrainerRegistrationRequest.class);
        TrainerRegistrationResponse response = mock(TrainerRegistrationResponse.class);

        when(facadeService.registerTrainer(request)).thenReturn(response);

        ResponseEntity<TrainerRegistrationResponse> result = trainerController.registerTrainer(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void getTrainer_ValidToken_ShouldReturnProfile() {
        String username = "alice";
        String token = "validToken";
        TrainerProfileResponse response = mock(TrainerProfileResponse.class);

        when(tokenService.isValidToken(token)).thenReturn(true);
        when(facadeService.getTrainerByUsername(username)).thenReturn(response);

        ResponseEntity<TrainerProfileResponse> result = trainerController.getTrainer(username, token);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void getTrainer_InvalidToken_ShouldThrowException() {
        String username = "alice";
        String token = "invalidToken";

        when(tokenService.isValidToken(token)).thenReturn(false);

        assertThrows(InvalidTokenException.class, () ->
                trainerController.getTrainer(username, token));
    }

    @Test
    void updateTrainer_ValidToken_ShouldReturnUpdatedProfile() {
        TrainerUpdateRequest request = mock(TrainerUpdateRequest.class);
        String token = "validToken";
        TrainerProfileResponse response = mock(TrainerProfileResponse.class);

        when(tokenService.isValidToken(token)).thenReturn(true);
        when(facadeService.updateTrainer(request)).thenReturn(response);

        ResponseEntity<TrainerProfileResponse> result = trainerController.updateTrainer(request, token);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void getAvailableTrainers_ValidToken_ShouldReturnList() {
        String traineeUsername = "trainee";
        String token = "validToken";
        List<TrainerDto> response = List.of(mock(TrainerDto.class));

        when(tokenService.isValidToken(token)).thenReturn(true);
        when(facadeService.getAvailableTrainersForTrainee(traineeUsername)).thenReturn(response);

        ResponseEntity<List<TrainerDto>> result = trainerController.getAvailableTrainers(traineeUsername, token);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void getTrainerTrainings_ValidToken_ShouldReturnList() {
        String username = "alice";
        String token = "validToken";
        LocalDate from = LocalDate.now().minusDays(5);
        LocalDate to = LocalDate.now();
        String traineeName = "bob";
        List<TrainerTrainingResponse> response = List.of(mock(TrainerTrainingResponse.class));

        when(tokenService.isValidToken(token)).thenReturn(true);
        when(facadeService.findTrainerTrainings(eq(username), any())).thenReturn(response);

        ResponseEntity<List<TrainerTrainingResponse>> result = trainerController.getTrainerTrainings(username, from, to, traineeName, token);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
    }

    @Test
    void updateTrainerActivation_ValidToken_ShouldReturnNoContent() {
        ActivateUserRequest request = new ActivateUserRequest("alice", true);
        String token = "validToken";

        when(tokenService.isValidToken(token)).thenReturn(true);

        ResponseEntity<Void> result = trainerController.updateTrainerActivation(request, token);

        verify(facadeService).changeTrainerActiveStatus("alice", true);
        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
    }

    @Test
    void updateTrainerActivation_InvalidToken_ShouldThrowException() {
        ActivateUserRequest request = new ActivateUserRequest("alice", true);
        String token = "invalidToken";

        when(tokenService.isValidToken(token)).thenReturn(false);

        assertThrows(InvalidTokenException.class, () ->
                trainerController.updateTrainerActivation(request, token));
    }
}
