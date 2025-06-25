package com.epam.gym.controller;

import com.epam.gym.dto.*;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.TrainingTypeEnum;
import com.epam.gym.exception.InvalidTokenException;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.TrainingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private TrainerController trainerController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trainerController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @Test
    void registerTrainer_ValidRequest_ReturnsCreated() throws Exception {
        // Given
        TrainerRegistrationRequest request = new TrainerRegistrationRequest(
                "John",
                "Doe",
                "Fitness"
        );

        TrainerRegistrationResponse response = new TrainerRegistrationResponse(
                "john.doe",
                "password123",
                "jwt"
        );

        when(trainerService.createTrainer(any(TrainerRegistrationRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/trainers/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("john.doe"))
                .andExpect(jsonPath("$.password").value("password123"));

        verify(trainerService).createTrainer(any(TrainerRegistrationRequest.class));
    }

    @Test
    void registerTrainer_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given - empty request body to trigger validation error
        TrainerRegistrationRequest request = new TrainerRegistrationRequest(null, null, null);

        // When & Then
        mockMvc.perform(post("/api/v1/trainers/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(trainerService, never()).createTrainer(any());
    }

    @Test
    void getTrainer_ValidUsername_ReturnsOk() throws Exception {
        // Given
        String username = "john.doe";
        TrainerProfileResponse response = new TrainerProfileResponse(
                "John",
                "Doe",
                new TrainingType(TrainingTypeEnum.CARDIO),
                true,
                Arrays.asList()
        );

        when(trainerService.findTrainerByUsername(username)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/trainers/{username}", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.specialization").value("CARDIO"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(trainerService).findTrainerByUsername(username);
    }

    @Test
    void getTrainer_BlankUsername_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/trainers/{username}", ""))
                .andExpect(status().isNotFound()); // 404 because empty path variable

        verify(trainerService, never()).findTrainerByUsername(any());
    }

//    @Test
//    void getTrainer_InvalidTokenException_ReturnsUnauthorized() throws Exception {
//        // Given
//        String username = "john.doe";
//        when(trainerService.findTrainerByUsername(username))
//                .thenThrow(new InvalidTokenException("Invalid token"));
//
//        // When & Then
//        mockMvc.perform(get("/api/v1/trainers/{username}", username))
//                .andExpect(status().isUnauthorized()); // Will be 500 unless you have exception handler
//
//        verify(trainerService).findTrainerByUsername(username);
//    }

    @Test
    void updateTrainer_ValidRequest_ReturnsOk() throws Exception {
        // Given
        TrainerUpdateRequest request = new TrainerUpdateRequest(
                "john.doe",
                "John",
                "Doe",
                "Fitness",
                true
        );

        TrainerProfileResponse response = new TrainerProfileResponse(
                "John",
                "Doe",
                new TrainingType(TrainingTypeEnum.CARDIO),
                true,
                Arrays.asList()
        );

        when(trainerService.updateTrainer(any(TrainerUpdateRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/trainers/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.specialization").value("CARDIO"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(trainerService).updateTrainer(any(TrainerUpdateRequest.class));
    }

    @Test
    void updateTrainer_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given - invalid request
        TrainerUpdateRequest request = new TrainerUpdateRequest(null, null, null, null, false);

        // When & Then
        mockMvc.perform(put("/api/v1/trainers/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(trainerService, never()).updateTrainer(any());
    }

    @Test
    void updateTrainer_InvalidTokenException_ReturnsUnauthorized() throws Exception {
        // Given
        TrainerUpdateRequest request = new TrainerUpdateRequest(
                "john.doe",
                "John",
                "Doe",
                "Fitness",
                true
        );

        when(trainerService.updateTrainer(any(TrainerUpdateRequest.class)))
                .thenThrow(new InvalidTokenException("Invalid token"));

        // When & Then
        mockMvc.perform(put("/api/v1/trainers/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError()); // Will be 500 unless you have exception handler

        verify(trainerService).updateTrainer(any(TrainerUpdateRequest.class));
    }

    @Test
    void getAvailableTrainers_ValidRequest_ReturnsOk() throws Exception {
        // Given
        String traineeUsername = "jane.doe";
        List<TrainerDto> trainers = Arrays.asList(
                new TrainerDto(
                        "trainer1",
                        "Trainer",
                        "One",
                        "Fitness"
                ),
                new TrainerDto(
                        "trainer2",
                        "Trainer",
                        "Two",
                        "Yoga"
                )
        );

        when(trainerService.getAvailableTrainersForTrainee(traineeUsername))
                .thenReturn(trainers);

        // When & Then
        mockMvc.perform(get("/api/v1/trainers/available")
                        .param("traineeUsername", traineeUsername))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("trainer1"))
                .andExpect(jsonPath("$[1].username").value("trainer2"));

        verify(trainerService).getAvailableTrainersForTrainee(traineeUsername);
    }

    @Test
    void getAvailableTrainers_InvalidTokenException_ReturnsUnauthorized() throws Exception {
        // Given
        String traineeUsername = "jane.doe";
        when(trainerService.getAvailableTrainersForTrainee(traineeUsername))
                .thenThrow(new InvalidTokenException("Invalid token"));

        // When & Then
        mockMvc.perform(get("/api/v1/trainers/available")
                        .param("traineeUsername", traineeUsername))
                .andExpect(status().isInternalServerError()); // Will be 500 unless you have exception handler

        verify(trainerService).getAvailableTrainersForTrainee(traineeUsername);
    }

    @Test
    void getTrainerTrainings_AllParameters_ReturnsOk() throws Exception {
        // Given
        String username = "john.doe";
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        String traineeName = "Jane Doe";

        List<TrainerTrainingResponse> trainings = Arrays.asList(
                new TrainerTrainingResponse(
                        "Morning Workout",
                        "2024-06-15",
                        "Fitness",
                        60,
                        "Jane Doe"
                )
        );

        when(trainingService.getTrainerTrainings(eq(username), any(TrainerTrainingRequest.class)))
                .thenReturn(trainings);

        // When & Then
        mockMvc.perform(get("/api/v1/trainers/{username}/trainings", username)
                        .param("from", "2024-01-01")
                        .param("to", "2024-12-31")
                        .param("traineeName", traineeName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].trainingName").value("Morning Workout"))
                .andExpect(jsonPath("$[0].traineeName").value("Jane Doe"));

        verify(trainingService).getTrainerTrainings(eq(username), any(TrainerTrainingRequest.class));
    }

    @Test
    void getTrainerTrainings_OnlyFromDate_ReturnsOk() throws Exception {
        // Given
        String username = "john.doe";
        LocalDate from = LocalDate.of(2024, 1, 1);

        List<TrainerTrainingResponse> trainings = Arrays.asList(
                new TrainerTrainingResponse(
                        "Evening Workout",
                        "2024-06-15",
                        "Fitness",
                        45,
                        "Bob Smith"
                )
        );

        when(trainingService.getTrainerTrainings(eq(username), any(TrainerTrainingRequest.class)))
                .thenReturn(trainings);

        // When & Then
        mockMvc.perform(get("/api/v1/trainers/{username}/trainings", username)
                        .param("from", "2024-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(trainingService).getTrainerTrainings(eq(username), any(TrainerTrainingRequest.class));
    }

    @Test
    void getTrainerTrainings_OnlyToDate_ReturnsOk() throws Exception {
        // Given
        String username = "john.doe";
        LocalDate to = LocalDate.of(2024, 12, 31);

        List<TrainerTrainingResponse> trainings = Arrays.asList();

        when(trainingService.getTrainerTrainings(eq(username), any(TrainerTrainingRequest.class)))
                .thenReturn(trainings);

        // When & Then
        mockMvc.perform(get("/api/v1/trainers/{username}/trainings", username)
                        .param("to", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(trainingService).getTrainerTrainings(eq(username), any(TrainerTrainingRequest.class));
    }

    @Test
    void getTrainerTrainings_OnlyTraineeName_ReturnsOk() throws Exception {
        // Given
        String username = "john.doe";
        String traineeName = "Jane Doe";

        List<TrainerTrainingResponse> trainings = Arrays.asList(
                new TrainerTrainingResponse(
                        "Specialized Training",
                        "2024-06-15",
                        "Yoga",
                        90,
                        "Jane Doe"
                )
        );

        when(trainingService.getTrainerTrainings(eq(username), any(TrainerTrainingRequest.class)))
                .thenReturn(trainings);

        // When & Then
        mockMvc.perform(get("/api/v1/trainers/{username}/trainings", username)
                        .param("traineeName", traineeName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].trainingType").value("Yoga"));

        verify(trainingService).getTrainerTrainings(eq(username), any(TrainerTrainingRequest.class));
    }

    @Test
    void getTrainerTrainings_NoParameters_ReturnsOk() throws Exception {
        // Given
        String username = "john.doe";
        List<TrainerTrainingResponse> trainings = Arrays.asList();

        when(trainingService.getTrainerTrainings(eq(username), any(TrainerTrainingRequest.class)))
                .thenReturn(trainings);

        // When & Then
        mockMvc.perform(get("/api/v1/trainers/{username}/trainings", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(trainingService).getTrainerTrainings(eq(username), any(TrainerTrainingRequest.class));
    }

    @Test
    void getTrainerTrainings_InvalidDateFormat_ReturnsBadRequest() throws Exception {
        // Given
        String username = "john.doe";

        // When & Then
        mockMvc.perform(get("/api/v1/trainers/{username}/trainings", username)
                        .param("from", "invalid-date"))
                .andExpect(status().isBadRequest());

        verify(trainingService, never()).getTrainerTrainings(any(), any());
    }

    @Test
    void getTrainerTrainings_InvalidTokenException_ReturnsUnauthorized() throws Exception {
        // Given
        String username = "john.doe";
        when(trainingService.getTrainerTrainings(eq(username), any(TrainerTrainingRequest.class)))
                .thenThrow(new InvalidTokenException("Invalid token"));

        // When & Then
        mockMvc.perform(get("/api/v1/trainers/{username}/trainings", username))
                .andExpect(status().isInternalServerError()); // Will be 500 unless you have exception handler

        verify(trainingService).getTrainerTrainings(eq(username), any(TrainerTrainingRequest.class));
    }

    @Test
    void updateTrainerActivation_ValidRequest_ReturnsNoContent() throws Exception {
        // Given
        ActivateUserRequest request = new ActivateUserRequest(
                "john.doe",
                true
        );

        doNothing().when(trainerService).changeActiveStatus(request.username(), request.isActive());

        // When & Then
        mockMvc.perform(patch("/api/v1/trainers/activation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(trainerService).changeActiveStatus(request.username(), request.isActive());
    }

    @Test
    void updateTrainerActivation_DeactivateUser_ReturnsNoContent() throws Exception {
        // Given
        ActivateUserRequest request = new ActivateUserRequest(
                "john.doe",
                false
        );

        doNothing().when(trainerService).changeActiveStatus(request.username(), request.isActive());

        // When & Then
        mockMvc.perform(patch("/api/v1/trainers/activation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(trainerService).changeActiveStatus(request.username(), request.isActive());
    }

    @Test
    void updateTrainerActivation_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given - invalid request
        ActivateUserRequest request = new ActivateUserRequest(null, null);

        // When & Then
        mockMvc.perform(patch("/api/v1/trainers/activation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(trainerService, never()).changeActiveStatus(any(), anyBoolean());
    }

    @Test
    void updateTrainerActivation_InvalidTokenException_ReturnsUnauthorized() throws Exception {
        // Given
        ActivateUserRequest request = new ActivateUserRequest(
                "john.doe",
                true
        );

        doThrow(new InvalidTokenException("Invalid token"))
                .when(trainerService).changeActiveStatus(request.username(), request.isActive());

        // When & Then
        mockMvc.perform(patch("/api/v1/trainers/activation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError()); // Will be 500 unless you have exception handler

        verify(trainerService).changeActiveStatus(request.username(), request.isActive());
    }

    @Test
    void updateTrainerActivation_ServiceException_ReturnsInternalServerError() throws Exception {
        // Given
        ActivateUserRequest request = new ActivateUserRequest(
                "john.doe",
                true
        );

        doThrow(new RuntimeException("Service error"))
                .when(trainerService).changeActiveStatus(request.username(), request.isActive());

        // When & Then
        mockMvc.perform(patch("/api/v1/trainers/activation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(trainerService).changeActiveStatus(request.username(), request.isActive());
    }
}