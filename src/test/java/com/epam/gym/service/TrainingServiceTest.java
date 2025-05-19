package com.epam.gym.service;

import com.epam.gym.entity.*;
import com.epam.gym.repository.TrainingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TrainingServiceTest {

    @Mock
    private TrainingRepository trainingRepository;

    @InjectMocks
    private TrainingService trainingService;

    private Trainee trainee;
    private Trainer trainer;
    private TrainingType trainingType;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        User traineeUser = User.builder()
                .username("traineeUser")
                .password("pass")
                .isActive(true)
                .build();

        User trainerUser = User.builder()
                .username("trainerUser")
                .password("pass")
                .isActive(true)
                .build();

        trainee = Trainee.builder()
                .user(traineeUser)
                .address("123 Street")
                .build();

        trainer = Trainer.builder()
                .user(trainerUser)
                .trainingType(new TrainingType(TrainingTypeEnum.CARDIO))
                .build();

        trainingType = new TrainingType(TrainingTypeEnum.CARDIO);
    }

//    @Test
//    void testCreateTraining_success() {
//        Training training = any(Training.class);
//
////        when(trainingRepository.save(any())).thenAnswer(invocation -> {
////            Training t = invocation.getArgument(0);
////            t.setId(1L);
////            return t;
////        });
//
//        when(trainingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
//
//
//        trainingService.createTraining(trainee, trainer, "Session A", trainingType,
//                LocalDate.now(), 60);
//
//        verify(trainingRepository, times(1)).save(any(Training.class));
//    }

    @Test
    void testCreateTraining_repositoryThrowsException() {
        doThrow(new RuntimeException("DB error")).when(trainingRepository).save(any());

        trainingService.createTraining(trainee, trainer, "Session A", trainingType,
                LocalDate.now(), 60);

        verify(trainingRepository, times(1)).save(any(Training.class));
        // exception is caught, so no need for assertion
    }

    @Test
    void testGetTraineeTrainings() {
        List<Training> expectedTrainings = Collections.singletonList(mock(Training.class));

        when(trainingRepository.findTraineeTrainingsByCriteria("traineeUser",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2025, 1, 1),
                "trainerName",
                trainingType)).thenReturn(expectedTrainings);

        List<Training> result = trainingService.getTraineeTrainings("traineeUser",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2025, 1, 1),
                "trainerName",
                trainingType);

        assertEquals(expectedTrainings, result);
        verify(trainingRepository).findTraineeTrainingsByCriteria(
                "traineeUser",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2025, 1, 1),
                "trainerName",
                trainingType);
    }

    @Test
    void testGetTrainerTrainings() {
        List<Training> expectedTrainings = Collections.singletonList(mock(Training.class));

        when(trainingRepository.findTrainerTrainingsByCriteria("trainerUser",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2025, 1, 1),
                "traineeName")).thenReturn(expectedTrainings);

        List<Training> result = trainingService.getTrainerTrainings("trainerUser",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2025, 1, 1),
                "traineeName");

        assertEquals(expectedTrainings, result);
        verify(trainingRepository).findTrainerTrainingsByCriteria(
                "trainerUser",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2025, 1, 1),
                "traineeName");
    }
}
