package com.epam.gym.mapper;

import com.epam.gym.dto.TraineeTrainingResponse;
import com.epam.gym.dto.TrainerTrainingResponse;
import com.epam.gym.entity.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TrainingMapperTest {

    @InjectMocks
    private TrainingMapper trainingMapper;

    @Test
    void mapTrainingsToTraineeDtoResponseList_withMultipleTrainings_shouldReturnCorrectResponseList() {
        // Given
        User trainerUser1 = User.builder()
                .firstName("John")
                .lastName("TrainerOne")
                .username("john.trainer")
                .build();

        User trainerUser2 = User.builder()
                .firstName("Jane")
                .lastName("TrainerTwo")
                .username("jane.trainer")
                .build();

        Trainer trainer1 = Trainer.builder()
                .user(trainerUser1)
                .build();

        Trainer trainer2 = Trainer.builder()
                .user(trainerUser2)
                .build();

        User traineeUser = User.builder()
                .firstName("Alice")
                .lastName("Trainee")
                .username("alice.trainee")
                .build();

        Trainee trainee = Trainee.builder()
                .user(traineeUser)
                .build();

        TrainingType fitnessType = new TrainingType(TrainingTypeEnum.FLEXIBILITY);

        TrainingType yogaType = new TrainingType(TrainingTypeEnum.CARDIO);

        Training training1 = Training.builder()
                .name("Morning Fitness")
                .date(LocalDate.of(2024, 1, 15))
                .duration(60)
                .trainer(trainer1)
                .trainee(trainee)
                .trainingType(fitnessType)
                .build();

        Training training2 = Training.builder()
                .name("Evening Yoga")
                .date(LocalDate.of(2024, 1, 16))
                .duration(45)
                .trainer(trainer2)
                .trainee(trainee)
                .trainingType(yogaType)
                .build();

        List<Training> trainings = List.of(training1, training2);

        // When
        List<TraineeTrainingResponse> result = trainingMapper.mapTrainingsToTraineeDtoResponseList(trainings);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        TraineeTrainingResponse response1 = result.get(0);
        assertEquals("Morning Fitness", response1.trainingName());
        assertEquals("2024-01-15", response1.trainingDate());
        assertEquals("FLEXIBILITY", response1.trainingType());
        assertEquals(60, response1.trainingDuration());
        assertEquals("John", response1.trainerName());

        TraineeTrainingResponse response2 = result.get(1);
        assertEquals("Evening Yoga", response2.trainingName());
        assertEquals("2024-01-16", response2.trainingDate());
        assertEquals("CARDIO", response2.trainingType());
        assertEquals(45, response2.trainingDuration());
        assertEquals("Jane", response2.trainerName());
    }

    @Test
    void mapTrainingsToTraineeDtoResponseList_withNullTrainings_shouldReturnEmptyList() {
        // When
        List<TraineeTrainingResponse> result = trainingMapper.mapTrainingsToTraineeDtoResponseList(null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void mapTrainingsToTraineeDtoResponseList_withEmptyTrainings_shouldReturnEmptyList() {
        // Given
        List<Training> emptyTrainings = List.of();

        // When
        List<TraineeTrainingResponse> result = trainingMapper.mapTrainingsToTraineeDtoResponseList(emptyTrainings);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void mapTrainingsToTraineeDtoResponseList_withSingleTraining_shouldReturnSingleResponse() {
        // Given
        User trainerUser = User.builder()
                .firstName("Mike")
                .lastName("Coach")
                .username("mike.coach")
                .build();

        Trainer trainer = Trainer.builder()
                .user(trainerUser)
                .build();

        User traineeUser = User.builder()
                .firstName("Sarah")
                .lastName("Student")
                .username("sarah.student")
                .build();

        Trainee trainee = Trainee.builder()
                .user(traineeUser)
                .build();

        TrainingType pilatesType = new TrainingType(TrainingTypeEnum.STRENGTH);

        Training training = Training.builder()
                .name("Pilates Session")
                .date(LocalDate.of(2024, 2, 20))
                .duration(90)
                .trainer(trainer)
                .trainee(trainee)
                .trainingType(pilatesType)
                .build();

        List<Training> trainings = List.of(training);

        // When
        List<TraineeTrainingResponse> result = trainingMapper.mapTrainingsToTraineeDtoResponseList(trainings);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        TraineeTrainingResponse response = result.get(0);
        assertEquals("Pilates Session", response.trainingName());
        assertEquals("2024-02-20", response.trainingDate());
        assertEquals("STRENGTH", response.trainingType());
        assertEquals(90, response.trainingDuration());
        assertEquals("Mike", response.trainerName());
    }

    @Test
    void mapTrainingsToTrainerDtoResponseList_withMultipleTrainings_shouldReturnCorrectResponseList() {
        // Given
        User traineeUser1 = User.builder()
                .firstName("Bob")
                .lastName("StudentOne")
                .username("bob.student")
                .build();

        User traineeUser2 = User.builder()
                .firstName("Carol")
                .lastName("StudentTwo")
                .username("carol.student")
                .build();

        Trainee trainee1 = Trainee.builder()
                .user(traineeUser1)
                .build();

        Trainee trainee2 = Trainee.builder()
                .user(traineeUser2)
                .build();

        User trainerUser = User.builder()
                .firstName("David")
                .lastName("Instructor")
                .username("david.instructor")
                .build();

        Trainer trainer = Trainer.builder()
                .user(trainerUser)
                .build();

        TrainingType zumbaType = new TrainingType(TrainingTypeEnum.HIIT);

        TrainingType boxingType = new TrainingType(TrainingTypeEnum.BALANCE);

        Training training1 = Training.builder()
                .name("Zumba Class")
                .date(LocalDate.of(2024, 3, 10))
                .duration(75)
                .trainer(trainer)
                .trainee(trainee1)
                .trainingType(zumbaType)
                .build();

        Training training2 = Training.builder()
                .name("Boxing Training")
                .date(LocalDate.of(2024, 3, 11))
                .duration(120)
                .trainer(trainer)
                .trainee(trainee2)
                .trainingType(boxingType)
                .build();

        List<Training> trainings = List.of(training1, training2);

        // When
        List<TrainerTrainingResponse> result = trainingMapper.mapTrainingsToTrainerDtoResponseList(trainings);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        TrainerTrainingResponse response1 = result.get(0);
        assertEquals("Zumba Class", response1.trainingName());
        assertEquals("2024-03-10", response1.trainingDate());
        assertEquals("HIIT", response1.trainingType());
        assertEquals(75, response1.trainingDuration());
        assertEquals("Bob", response1.traineeName());

        TrainerTrainingResponse response2 = result.get(1);
        assertEquals("Boxing Training", response2.trainingName());
        assertEquals("2024-03-11", response2.trainingDate());
        assertEquals("BALANCE", response2.trainingType());
        assertEquals(120, response2.trainingDuration());
        assertEquals("Carol", response2.traineeName());
    }

    @Test
    void mapTrainingsToTrainerDtoResponseList_withNullTrainings_shouldReturnEmptyList() {
        // When
        List<TrainerTrainingResponse> result = trainingMapper.mapTrainingsToTrainerDtoResponseList(null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void mapTrainingsToTrainerDtoResponseList_withEmptyTrainings_shouldReturnEmptyList() {
        // Given
        List<Training> emptyTrainings = List.of();

        // When
        List<TrainerTrainingResponse> result = trainingMapper.mapTrainingsToTrainerDtoResponseList(emptyTrainings);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void mapTrainingsToTrainerDtoResponseList_withSingleTraining_shouldReturnSingleResponse() {
        // Given
        User traineeUser = User.builder()
                .firstName("Emma")
                .lastName("Learner")
                .username("emma.learner")
                .build();

        Trainee trainee = Trainee.builder()
                .user(traineeUser)
                .build();

        User trainerUser = User.builder()
                .firstName("Frank")
                .lastName("Expert")
                .username("frank.expert")
                .build();

        Trainer trainer = Trainer.builder()
                .user(trainerUser)
                .build();

        TrainingType stretchingType = new TrainingType(TrainingTypeEnum.STRENGTH);

        Training training = Training.builder()
                .name("Flexibility Training")
                .date(LocalDate.of(2024, 4, 5))
                .duration(30)
                .trainer(trainer)
                .trainee(trainee)
                .trainingType(stretchingType)
                .build();

        List<Training> trainings = List.of(training);

        // When
        List<TrainerTrainingResponse> result = trainingMapper.mapTrainingsToTrainerDtoResponseList(trainings);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        TrainerTrainingResponse response = result.get(0);
        assertEquals("Flexibility Training", response.trainingName());
        assertEquals("2024-04-05", response.trainingDate());
        assertEquals("STRENGTH", response.trainingType());
        assertEquals(30, response.trainingDuration());
        assertEquals("Emma", response.traineeName());
    }

    @Test
    void mapTrainingsToTraineeDtoResponseList_withSpecialCharactersInName_shouldHandleCorrectly() {
        // Given
        User trainerUser = User.builder()
                .firstName("José")
                .lastName("García")
                .username("jose.garcia")
                .build();

        Trainer trainer = Trainer.builder()
                .user(trainerUser)
                .build();

        User traineeUser = User.builder()
                .firstName("Müller")
                .lastName("Schmidt")
                .username("muller.schmidt")
                .build();

        Trainee trainee = Trainee.builder()
                .user(traineeUser)
                .build();

        TrainingType resistanceType = new TrainingType(TrainingTypeEnum.FLEXIBILITY);

        Training training = Training.builder()
                .name("Special Training & Conditioning")
                .date(LocalDate.of(2024, 5, 1))
                .duration(45)
                .trainer(trainer)
                .trainee(trainee)
                .trainingType(resistanceType)
                .build();

        List<Training> trainings = List.of(training);

        // When
        List<TraineeTrainingResponse> result = trainingMapper.mapTrainingsToTraineeDtoResponseList(trainings);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        TraineeTrainingResponse response = result.get(0);
        assertEquals("Special Training & Conditioning", response.trainingName());
        assertEquals("2024-05-01", response.trainingDate());
        assertEquals("FLEXIBILITY", response.trainingType());
        assertEquals(45, response.trainingDuration());
        assertEquals("José", response.trainerName());
    }

    @Test
    void mapTrainingsToTrainerDtoResponseList_withZeroDuration_shouldHandleCorrectly() {
        // Given
        User traineeUser = User.builder()
                .firstName("Test")
                .lastName("User")
                .username("test.user")
                .build();

        Trainee trainee = Trainee.builder()
                .user(traineeUser)
                .build();

        User trainerUser = User.builder()
                .firstName("Quick")
                .lastName("Session")
                .username("quick.session")
                .build();

        Trainer trainer = Trainer.builder()
                .user(trainerUser)
                .build();

        TrainingType consultationType = new TrainingType(TrainingTypeEnum.HIIT);

        Training training = Training.builder()
                .name("Quick Consultation")
                .date(LocalDate.of(2024, 6, 1))
                .duration(0)
                .trainer(trainer)
                .trainee(trainee)
                .trainingType(consultationType)
                .build();

        List<Training> trainings = List.of(training);

        // When
        List<TrainerTrainingResponse> result = trainingMapper.mapTrainingsToTrainerDtoResponseList(trainings);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        TrainerTrainingResponse response = result.get(0);
        assertEquals("Quick Consultation", response.trainingName());
        assertEquals("2024-06-01", response.trainingDate());
        assertEquals("HIIT", response.trainingType());
        assertEquals(0, response.trainingDuration());
        assertEquals("Test", response.traineeName());
    }
}
