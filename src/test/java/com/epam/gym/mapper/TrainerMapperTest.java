package com.epam.gym.mapper;

import com.epam.gym.dto.TraineeDto;
import com.epam.gym.dto.TrainerProfileResponse;
import com.epam.gym.dto.TrainerRegistrationResponse;
import com.epam.gym.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TrainerMapperTest {

    @InjectMocks
    private TrainerMapper trainerMapper;

    @Test
    void toTrainerProfileResponse_withValidTrainer_shouldReturnValidResponse() {
        // Given
        User trainerUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .username("john.doe")
                .isActive(true)
                .build();

        User traineeUser1 = User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .username("jane.smith")
                .build();

        User traineeUser2 = User.builder()
                .firstName("Bob")
                .lastName("Johnson")
                .username("bob.johnson")
                .build();

        Trainee trainee1 = Trainee.builder()
                .user(traineeUser1)
                .build();

        Trainee trainee2 = Trainee.builder()
                .user(traineeUser2)
                .build();

        Trainer trainer = Trainer.builder()
                .user(trainerUser)
                .trainingType(new TrainingType(TrainingTypeEnum.HIIT))
                .trainees(Set.of(trainee1, trainee2))
                .build();

        // When
        TrainerProfileResponse response = trainerMapper.toTrainerProfileResponse(trainer);

        // Then
        assertNotNull(response);
        assertEquals("John", response.firstName());
        assertEquals("Doe", response.lastName());
        assertEquals("HIIT", response.specialization().getType().toString());
        assertTrue(response.isActive());

        List<TraineeDto> trainees = response.trainees();
        assertNotNull(trainees);
        assertEquals(2, trainees.size());

        // Verify trainee data (order might vary due to Set)
        assertTrue(trainees.stream().anyMatch(t ->
                "jane.smith".equals(t.username()) &&
                        "Jane".equals(t.firstName()) &&
                        "Smith".equals(t.lastName())
        ));

        assertTrue(trainees.stream().anyMatch(t ->
                "bob.johnson".equals(t.username()) &&
                        "Bob".equals(t.firstName()) &&
                        "Johnson".equals(t.lastName())
        ));
    }

    @Test
    void toTrainerProfileResponse_withNullTrainer_shouldReturnNull() {
        // When
        TrainerProfileResponse response = trainerMapper.toTrainerProfileResponse(null);

        // Then
        assertNull(response);
    }

    @Test
    void toTrainerProfileResponse_withEmptyTrainees_shouldReturnEmptyList() {
        // Given
        User trainerUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .isActive(false)
                .build();

        Trainer trainer = Trainer.builder()
                .user(trainerUser)
                .trainingType(new TrainingType(TrainingTypeEnum.CARDIO))
                .trainees(Set.of())
                .build();

        // When
        TrainerProfileResponse response = trainerMapper.toTrainerProfileResponse(trainer);

        // Then
        assertNotNull(response);
        assertEquals("John", response.firstName());
        assertEquals("Doe", response.lastName());
        assertEquals("CARDIO", response.specialization().getType().toString());
        assertFalse(response.isActive());

        List<TraineeDto> trainees = response.trainees();
        assertNotNull(trainees);
        assertTrue(trainees.isEmpty());
    }

    @Test
    void toTrainerProfileResponse_withNullTrainees_shouldReturnEmptyList() {
        // Given
        User trainerUser = User.builder()
                .firstName("Alice")
                .lastName("Wonder")
                .isActive(true)
                .build();

        Trainer trainer = Trainer.builder()
                .user(trainerUser)
                .trainingType(new TrainingType(TrainingTypeEnum.FLEXIBILITY))
                .trainees(null)
                .build();

        // When
        TrainerProfileResponse response = trainerMapper.toTrainerProfileResponse(trainer);

        // Then
        assertNotNull(response);
        assertEquals("Alice", response.firstName());
        assertEquals("Wonder", response.lastName());
        assertEquals("FLEXIBILITY", response.specialization().getType().toString());
        assertTrue(response.isActive());

        List<TraineeDto> trainees = response.trainees();
        assertNotNull(trainees);
        assertTrue(trainees.isEmpty());
    }

    @Test
    void toTrainerRegistrationResponse_withValidTrainer_shouldReturnValidResponse() {
        // Given
        User user = User.builder()
                .username("trainer.user")
                .password("password123")
                .build();

        Trainer trainer = Trainer.builder()
                .user(user)
                .build();

        // When
        TrainerRegistrationResponse response = trainerMapper.toTrainerRegistrationResponse(trainer,"password","jwt");

        // Then
        assertNotNull(response);
        assertEquals("trainer.user", response.username());
        assertEquals("password123", response.password());
    }

    @Test
    void toTrainerRegistrationResponse_withNullTrainer_shouldReturnNull() {
        // When
        TrainerRegistrationResponse response = trainerMapper.toTrainerRegistrationResponse(null,"password","jwt");

        // Then
        assertNull(response);
    }

    @Test
    void toTrainerProfileResponse_withSingleTrainee_shouldReturnCorrectResponse() {
        // Given
        User trainerUser = User.builder()
                .firstName("Mike")
                .lastName("Coach")
                .isActive(true)
                .build();

        User traineeUser = User.builder()
                .firstName("Sarah")
                .lastName("Athlete")
                .username("sarah.athlete")
                .build();

        Trainee trainee = Trainee.builder()
                .user(traineeUser)
                .build();

        Trainer trainer = Trainer.builder()
                .user(trainerUser)
                .trainingType(new TrainingType(TrainingTypeEnum.STRENGTH))
                .trainees(Set.of(trainee))
                .build();

        // When
        TrainerProfileResponse response = trainerMapper.toTrainerProfileResponse(trainer);

        // Then
        assertNotNull(response);
        assertEquals("Mike", response.firstName());
        assertEquals("Coach", response.lastName());
        assertEquals("STRENGTH", response.specialization().getType().toString());
        assertTrue(response.isActive());

        List<TraineeDto> trainees = response.trainees();
        assertNotNull(trainees);
        assertEquals(1, trainees.size());

        TraineeDto traineeDto = trainees.get(0);
        assertEquals("sarah.athlete", traineeDto.username());
        assertEquals("Sarah", traineeDto.firstName());
        assertEquals("Athlete", traineeDto.lastName());
    }
}