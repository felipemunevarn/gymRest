package com.epam.gym.service;

import com.epam.gym.dto.*;
import com.epam.gym.entity.*;
import com.epam.gym.mapper.TrainingMapper;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingRepository;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingService Unit Tests")
class TrainingServiceTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TrainingMapper trainingMapper;

    @InjectMocks
    private TrainingService trainingService;

    private Trainee mockTrainee;
    private Trainer mockTrainer;
    private Training mockTraining;
    private TrainingRegistrationRequest mockRequest;
    private TrainingType mockTrainingType;
    private User mockTraineeUser;
    private User mockTrainerUser;

    @BeforeEach
    void setUp() {
        // Setup mock users
        mockTraineeUser = new User.Builder()
                .username("trainee.user")
                .firstName("John")
                .lastName("Doe")
                .build();

        mockTrainerUser = new User.Builder()
                .username("trainer.user")
                .firstName("Jane")
                .lastName("Smith")
                .build();

        // Setup mock training type
        mockTrainingType = new TrainingType(TrainingTypeEnum.STRENGTH);

        // Setup mock trainee
        mockTrainee = new Trainee.Builder()
                .id(1L)
                .user(mockTraineeUser)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Main St")
                .build();

        // Setup mock trainer
        mockTrainer = new Trainer.Builder()
                .id(1L)
                .user(mockTrainerUser)
                .trainingType(mockTrainingType)
                .build();

        // Setup mock training
        mockTraining = new Training.Builder()
                .id(1L)
                .trainee(mockTrainee)
                .trainer(mockTrainer)
                .name("Morning Workout")
                .trainingType(mockTrainingType)
                .date(LocalDate.now())
                .duration(60)
                .build();

        // Setup mock request
        mockRequest = new TrainingRegistrationRequest(
                "trainee.user",
                "trainer.user",
                "Morning Workout",
                LocalDate.now(),
                60
        );
    }

    @Test
    @DisplayName("Should create training successfully")
    void createTraining_Success() {
        // Given
        when(traineeRepository.findByUserUsername("trainee.user")).thenReturn(Optional.of(mockTrainee));
        when(trainerRepository.findByUserUsername("trainer.user")).thenReturn(Optional.of(mockTrainer));
        when(trainingRepository.save(any(Training.class))).thenReturn(mockTraining);

        // When
        assertDoesNotThrow(() -> trainingService.createTraining(mockRequest));

        // Then
        verify(traineeRepository).findByUserUsername("trainee.user");
        verify(trainerRepository).findByUserUsername("trainer.user");
        verify(trainingRepository).save(any(Training.class));
    }

    @Test
    @DisplayName("Should throw NoResultException when trainee not found")
    void createTraining_TraineeNotFound() {
        // Given
        when(traineeRepository.findByUserUsername("trainee.user")).thenReturn(Optional.empty());

        // When & Then
        NoResultException exception = assertThrows(NoResultException.class,
                () -> trainingService.createTraining(mockRequest));

        assertEquals("Trainee not found", exception.getMessage());
        verify(traineeRepository).findByUserUsername("trainee.user");
        verify(trainerRepository, never()).findByUserUsername(anyString());
        verify(trainingRepository, never()).save(any(Training.class));
    }

    @Test
    @DisplayName("Should throw NoResultException when trainer not found")
    void createTraining_TrainerNotFound() {
        // Given
        when(traineeRepository.findByUserUsername("trainee.user")).thenReturn(Optional.of(mockTrainee));
        when(trainerRepository.findByUserUsername("trainer.user")).thenReturn(Optional.empty());

        // When & Then
        NoResultException exception = assertThrows(NoResultException.class,
                () -> trainingService.createTraining(mockRequest));

        assertEquals("Trainee not found", exception.getMessage());
        verify(traineeRepository).findByUserUsername("trainee.user");
        verify(trainerRepository).findByUserUsername("trainer.user");
        verify(trainingRepository, never()).save(any(Training.class));
    }

    @Test
    @DisplayName("Should handle save exception gracefully")
    void createTraining_SaveException() {
        // Given
        when(traineeRepository.findByUserUsername("trainee.user")).thenReturn(Optional.of(mockTrainee));
        when(trainerRepository.findByUserUsername("trainer.user")).thenReturn(Optional.of(mockTrainer));
        when(trainingRepository.save(any(Training.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertDoesNotThrow(() -> trainingService.createTraining(mockRequest));

        verify(traineeRepository).findByUserUsername("trainee.user");
        verify(trainerRepository).findByUserUsername("trainer.user");
        verify(trainingRepository).save(any(Training.class));
    }

    @Test
    @DisplayName("Should create training with correct parameters")
    void createTraining_CorrectParameters() {
        // Given
        when(traineeRepository.findByUserUsername("trainee.user")).thenReturn(Optional.of(mockTrainee));
        when(trainerRepository.findByUserUsername("trainer.user")).thenReturn(Optional.of(mockTrainer));
        when(trainingRepository.save(any(Training.class))).thenReturn(mockTraining);

        // When
        trainingService.createTraining(mockRequest);

        // Then
        verify(trainingRepository).save(argThat(training ->
                training.getTrainee().equals(mockTrainee) &&
                training.getTrainer().equals(mockTrainer) &&
                training.getName().equals("Morning Workout") &&
                training.getTrainingType().equals(mockTrainer.getTrainingType()) &&
                training.getDate().equals(mockRequest.date()) &&
                training.getDuration() == mockRequest.duration()
        ));
    }

    @Test
    @DisplayName("Should get trainee trainings successfully")
    void getTraineeTrainings_Success() {
        // Given
        String username = "trainee.user";
        TraineeTrainingRequest request = new TraineeTrainingRequest(
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 12, 31),
                "Jane Smith",
                "CARDIO"
        );

        List<Training> mockTrainings = Arrays.asList(mockTraining);
        List<TraineeTrainingResponse> expectedResponse = Arrays.asList(
                new TraineeTrainingResponse("Morning Workout", LocalDate.now().toString(), "CARDIO", 60, "Jane Smith")
        );

        when(trainingRepository.findTraineeTrainingsByCriteria(
                eq(username),
                eq(request.from()),
                eq(request.to()),
                eq(request.trainerName()),
                any(TrainingType.class)
        )).thenReturn(mockTrainings);
        when(trainingMapper.mapTrainingsToTraineeDtoResponseList(mockTrainings)).thenReturn(expectedResponse);

        // When
        List<TraineeTrainingResponse> result = trainingService.getTraineeTrainings(username, request);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedResponse, result);
        verify(trainingRepository).findTraineeTrainingsByCriteria(
                eq(username),
                eq(request.from()),
                eq(request.to()),
                eq(request.trainerName()),
                any(TrainingType.class)
        );
        verify(trainingMapper).mapTrainingsToTraineeDtoResponseList(mockTrainings);
    }

    @Test
    @DisplayName("Should get trainee trainings with empty result")
    void getTraineeTrainings_EmptyResult() {
        // Given
        String username = "trainee.user";
        TraineeTrainingRequest request = new TraineeTrainingRequest(
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 12, 31),
                "Jane Smith",
                "CARDIO"
        );

        List<Training> emptyTrainings = Collections.emptyList();
        List<TraineeTrainingResponse> emptyResponse = Collections.emptyList();

        when(trainingRepository.findTraineeTrainingsByCriteria(
                eq(username),
                eq(request.from()),
                eq(request.to()),
                eq(request.trainerName()),
                any(TrainingType.class)
        )).thenReturn(emptyTrainings);
        when(trainingMapper.mapTrainingsToTraineeDtoResponseList(emptyTrainings)).thenReturn(emptyResponse);

        // When
        List<TraineeTrainingResponse> result = trainingService.getTraineeTrainings(username, request);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(trainingRepository).findTraineeTrainingsByCriteria(
                eq(username),
                eq(request.from()),
                eq(request.to()),
                eq(request.trainerName()),
                any(TrainingType.class)
        );
        verify(trainingMapper).mapTrainingsToTraineeDtoResponseList(emptyTrainings);
    }

    @Test
    @DisplayName("Should handle invalid training type enum in trainee trainings")
    void getTraineeTrainings_InvalidTrainingType() {
        // Given
        String username = "trainee.user";
        TraineeTrainingRequest request = new TraineeTrainingRequest(
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 12, 31),
                "Jane Smith",
                "INVALID_TYPE"
        );

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> trainingService.getTraineeTrainings(username, request));

        verify(trainingRepository, never()).findTraineeTrainingsByCriteria(
                anyString(), any(), any(), anyString(), any(TrainingType.class)
        );
        verify(trainingMapper, never()).mapTrainingsToTraineeDtoResponseList(any());
    }

    @Test
    @DisplayName("Should get trainer trainings successfully")
    void getTrainerTrainings_Success() {
        // Given
        String username = "trainer.user";
        TrainerTrainingRequest request = new TrainerTrainingRequest(
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 12, 31),
                "John Doe"
        );

        List<Training> mockTrainings = Arrays.asList(mockTraining);
        List<TrainerTrainingResponse> expectedResponse = Arrays.asList(
                new TrainerTrainingResponse("Morning Workout", LocalDate.now().toString(), "CARDIO", 60, "John Doe")
        );

        when(trainingRepository.findTrainerTrainingsByCriteria(
                username,
                request.from(),
                request.to(),
                request.traineeName()
        )).thenReturn(mockTrainings);
        when(trainingMapper.mapTrainingsToTrainerDtoResponseList(mockTrainings)).thenReturn(expectedResponse);

        // When
        List<TrainerTrainingResponse> result = trainingService.getTrainerTrainings(username, request);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedResponse, result);
        verify(trainingRepository).findTrainerTrainingsByCriteria(
                username,
                request.from(),
                request.to(),
                request.traineeName()
        );
        verify(trainingMapper).mapTrainingsToTrainerDtoResponseList(mockTrainings);
    }

    @Test
    @DisplayName("Should get trainer trainings with empty result")
    void getTrainerTrainings_EmptyResult() {
        // Given
        String username = "trainer.user";
        TrainerTrainingRequest request = new TrainerTrainingRequest(
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 12, 31),
                "John Doe"
        );

        List<Training> emptyTrainings = Collections.emptyList();
        List<TrainerTrainingResponse> emptyResponse = Collections.emptyList();

        when(trainingRepository.findTrainerTrainingsByCriteria(
                username,
                request.from(),
                request.to(),
                request.traineeName()
        )).thenReturn(emptyTrainings);
        when(trainingMapper.mapTrainingsToTrainerDtoResponseList(emptyTrainings)).thenReturn(emptyResponse);

        // When
        List<TrainerTrainingResponse> result = trainingService.getTrainerTrainings(username, request);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(trainingRepository).findTrainerTrainingsByCriteria(
                username,
                request.from(),
                request.to(),
                request.traineeName()
        );
        verify(trainingMapper).mapTrainingsToTrainerDtoResponseList(emptyTrainings);
    }

    @Test
    @DisplayName("Should handle null parameters in trainee trainings")
    void getTraineeTrainings_NullParameters() {
        // Given
        String username = "trainee.user";
        TraineeTrainingRequest request = new TraineeTrainingRequest(
                null, null, null, "CARDIO"
        );

        List<Training> mockTrainings = Arrays.asList(mockTraining);
        List<TraineeTrainingResponse> expectedResponse = Arrays.asList(
                new TraineeTrainingResponse("Morning Workout", LocalDate.now().toString(), "CARDIO", 60, "Jane Smith")
        );

        when(trainingRepository.findTraineeTrainingsByCriteria(
                eq(username), isNull(), isNull(), isNull(), any(TrainingType.class)
        )).thenReturn(mockTrainings);
        when(trainingMapper.mapTrainingsToTraineeDtoResponseList(mockTrainings)).thenReturn(expectedResponse);

        // When
        List<TraineeTrainingResponse> result = trainingService.getTraineeTrainings(username, request);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(trainingRepository).findTraineeTrainingsByCriteria(
                eq(username), isNull(), isNull(), isNull(), any(TrainingType.class)
        );
    }

    @Test
    @DisplayName("Should handle null parameters in trainer trainings")
    void getTrainerTrainings_NullParameters() {
        // Given
        String username = "trainer.user";
        TrainerTrainingRequest request = new TrainerTrainingRequest(null, null, null);

        List<Training> mockTrainings = Arrays.asList(mockTraining);
        List<TrainerTrainingResponse> expectedResponse = Arrays.asList(
                new TrainerTrainingResponse("Morning Workout", LocalDate.now().toString(), "CARDIO", 60, "John Doe")
        );

        when(trainingRepository.findTrainerTrainingsByCriteria(
                username, null, null, null
        )).thenReturn(mockTrainings);
        when(trainingMapper.mapTrainingsToTrainerDtoResponseList(mockTrainings)).thenReturn(expectedResponse);

        // When
        List<TrainerTrainingResponse> result = trainingService.getTrainerTrainings(username, request);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(trainingRepository).findTrainerTrainingsByCriteria(username, null, null, null);
    }

//    @Test
//    @DisplayName("Should create training type with correct enum value")
//    void getTraineeTrainings_CorrectTrainingTypeCreation() {
//        // Given
//        String username = "trainee.user";
//        TraineeTrainingRequest request = new TraineeTrainingRequest(
//                LocalDate.of(2023, 1, 1),
//                LocalDate.of(2023, 12, 31),
//                "Jane Smith",
//                "CARDIO"
//        );
//
//        when(trainingRepository.findTraineeTrainingsByCriteria(
//                eq(username),
//                eq(request.from()),
//                eq(request.to()),
//                eq(request.trainerName()),
//                argThat(trainingType -> trainingType.getType().toString().equals(TrainingTypeEnum.STRENGTH.toString()))
//        )).thenReturn(Collections.emptyList());
//        when(trainingMapper.mapTrainingsToTraineeDtoResponseList(any())).thenReturn(Collections.emptyList());
//
//        // When
//        trainingService.getTraineeTrainings(username, request);
//
//        // Then
//        verify(trainingRepository).findTraineeTrainingsByCriteria(
//                eq(username),
//                eq(request.from()),
//                eq(request.to()),
//                eq(request.trainerName()),
//                argThat(trainingType -> trainingType.getType().toString().equals(TrainingTypeEnum.STRENGTH.toString()))
//        );
//    }

    @Test
    @DisplayName("Should verify all repository and mapper interactions")
    void verifyAllInteractions() {
        // Given
        when(traineeRepository.findByUserUsername("trainee.user")).thenReturn(Optional.of(mockTrainee));
        when(trainerRepository.findByUserUsername("trainer.user")).thenReturn(Optional.of(mockTrainer));
        when(trainingRepository.save(any(Training.class))).thenReturn(mockTraining);

        // When
        trainingService.createTraining(mockRequest);

        // Then - verify no unexpected interactions
        verifyNoMoreInteractions(trainingMapper);
        verifyNoMoreInteractions(traineeRepository);
        verifyNoMoreInteractions(trainerRepository);
        verifyNoMoreInteractions(trainingRepository);
    }
}
