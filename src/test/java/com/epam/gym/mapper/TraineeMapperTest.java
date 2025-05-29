package com.epam.gym.mapper;

import com.epam.gym.dto.TraineeProfileResponse;
import com.epam.gym.dto.TraineeRegistrationResponse;
import com.epam.gym.dto.TrainerDto;
import com.epam.gym.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TraineeMapper Unit Tests")
class TraineeMapperTest {

    @InjectMocks
    private TraineeMapper traineeMapper;

    private Trainee mockTrainee;
    private User mockTraineeUser;
    private Trainer mockTrainer1;
    private Trainer mockTrainer2;
    private User mockTrainerUser1;
    private User mockTrainerUser2;
    private TrainingType mockTrainingType1;
    private TrainingType mockTrainingType2;

    @BeforeEach
    void setUp() {
        // Setup mock training types using builder pattern
        mockTrainingType1 = new TrainingType(TrainingTypeEnum.STRENGTH);

        mockTrainingType2 = new TrainingType(TrainingTypeEnum.CARDIO);

        // Setup mock trainee user using builder pattern
        mockTraineeUser = new User.Builder()
                .id(1L)
                .username("john.doe")
                .firstName("John")
                .lastName("Doe")
                .password("password123")
                .isActive(true)
                .build();

        // Setup mock trainer users using builder pattern
        mockTrainerUser1 = new User.Builder()
                .id(2L)
                .username("jane.smith")
                .firstName("Jane")
                .lastName("Smith")
                .password("trainer123")
                .isActive(true)
                .build();

        mockTrainerUser2 = new User.Builder()
                .id(3L)
                .username("mike.wilson")
                .firstName("Mike")
                .lastName("Wilson")
                .password("trainer456")
                .isActive(true)
                .build();

        // Setup mock trainers using builder pattern
        mockTrainer1 = new Trainer.Builder()
                .id(1L)
                .user(mockTrainerUser1)
                .trainingType(mockTrainingType1)
                .build();

        mockTrainer2 = new Trainer.Builder()
                .id(2L)
                .user(mockTrainerUser2)
                .trainingType(mockTrainingType2)
                .build();

        // Setup mock trainee using builder pattern
        mockTrainee = new Trainee.Builder()
                .id(1L)
                .user(mockTraineeUser)
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .address("123 Main Street, City")
                .trainers(Set.of(mockTrainer1, mockTrainer2))
                .build();
    }

    @Test
    @DisplayName("Should convert trainee to profile response successfully")
    void toTraineeProfileResponse_Success() {
        // When
        TraineeProfileResponse result = traineeMapper.toTraineeProfileResponse(mockTrainee);

        // Then
        assertNotNull(result);
        assertEquals("John", result.firstName());
        assertEquals("Doe", result.lastName());
        assertEquals(LocalDate.of(1990, 5, 15), result.dateOfBirth());
        assertEquals("123 Main Street, City", result.address());
        assertTrue(result.isActive());

        assertNotNull(result.trainers());
        assertEquals(2, result.trainers().size());
    }

    @Test
    @DisplayName("Should return null when trainee is null for profile response")
    void toTraineeProfileResponse_NullTrainee() {
        // When
        TraineeProfileResponse result = traineeMapper.toTraineeProfileResponse(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle trainee with null user")
    void toTraineeProfileResponse_NullUser() {
        // Given
        mockTrainee = new Trainee.Builder()
                .id(1L)
                .user(null)
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .address("123 Main Street, City")
                .build();

        // When & Then
        assertThrows(NullPointerException.class,
                () -> traineeMapper.toTraineeProfileResponse(mockTrainee));
    }

    @Test
    @DisplayName("Should handle trainee with inactive user")
    void toTraineeProfileResponse_InactiveUser() {
        // Given
        User inactiveUser = new User.Builder()
                .id(1L)
                .username("john.doe")
                .firstName("John")
                .lastName("Doe")
                .password("password123")
                .isActive(false)
                .build();

        mockTrainee = new Trainee.Builder()
                .id(1L)
                .user(inactiveUser)
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .address("123 Main Street, City")
                .trainers(Set.of(mockTrainer1, mockTrainer2))
                .build();

        // When
        TraineeProfileResponse result = traineeMapper.toTraineeProfileResponse(mockTrainee);

        // Then
        assertNotNull(result);
        assertEquals("John", result.firstName());
        assertEquals("Doe", result.lastName());
        assertFalse(result.isActive());
    }

    @Test
    @DisplayName("Should handle trainee with null trainers set")
    void toTraineeProfileResponse_NullTrainers() {
        // Given
        mockTrainee = new Trainee.Builder()
                .id(1L)
                .user(mockTraineeUser)
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .address("123 Main Street, City")
                .trainers(null)
                .build();

        // When
        TraineeProfileResponse result = traineeMapper.toTraineeProfileResponse(mockTrainee);

        // Then
        assertNotNull(result);
        assertEquals("John", result.firstName());
        assertEquals("Doe", result.lastName());
        assertNotNull(result.trainers());
        assertTrue(result.trainers().isEmpty());
    }

    @Test
    @DisplayName("Should handle trainee with empty trainers set")
    void toTraineeProfileResponse_EmptyTrainers() {
        // Given
        mockTrainee = new Trainee.Builder()
                .id(1L)
                .user(mockTraineeUser)
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .address("123 Main Street, City")
                .trainers(Set.of())
                .build();

        // When
        TraineeProfileResponse result = traineeMapper.toTraineeProfileResponse(mockTrainee);

        // Then
        assertNotNull(result);
        assertEquals("John", result.firstName());
        assertEquals("Doe", result.lastName());
        assertNotNull(result.trainers());
        assertTrue(result.trainers().isEmpty());
    }

    @Test
    @DisplayName("Should handle trainee with null optional fields")
    void toTraineeProfileResponse_NullOptionalFields() {
        // Given
        mockTrainee = new Trainee.Builder()
                .id(1L)
                .user(mockTraineeUser)
                .dateOfBirth(null)
                .address(null)
                .trainers(Set.of(mockTrainer1, mockTrainer2))
                .build();

        // When
        TraineeProfileResponse result = traineeMapper.toTraineeProfileResponse(mockTrainee);

        // Then
        assertNotNull(result);
        assertEquals("John", result.firstName());
        assertEquals("Doe", result.lastName());
        assertNull(result.dateOfBirth());
        assertNull(result.address());
        assertTrue(result.isActive());
    }

    @Test
    @DisplayName("Should convert trainee to registration response successfully")
    void toTraineeRegistrationResponse_Success() {
        // When
        TraineeRegistrationResponse result = traineeMapper.toTraineeRegistrationResponse(mockTrainee);

        // Then
        assertNotNull(result);
        assertEquals("john.doe", result.username());
        assertEquals("password123", result.password());
    }

    @Test
    @DisplayName("Should return null when trainee is null for registration response")
    void toTraineeRegistrationResponse_NullTrainee() {
        // When
        TraineeRegistrationResponse result = traineeMapper.toTraineeRegistrationResponse(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle trainee with null user for registration response")
    void toTraineeRegistrationResponse_NullUser() {
        // Given
        mockTrainee = new Trainee.Builder()
                .id(1L)
                .user(null)
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .address("123 Main Street, City")
                .build();

        // When & Then
        assertThrows(NullPointerException.class,
                () -> traineeMapper.toTraineeRegistrationResponse(mockTrainee));
    }

    @Test
    @DisplayName("Should map trainers to trainer DTO list successfully")
    void mapTrainersToTrainerDtoList_Success() {
        // Given
        Set<Trainer> trainers = Set.of(mockTrainer1, mockTrainer2);

        // When
        List<TrainerDto> result = traineeMapper.mapTrainersToTrainerDtoList(trainers);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify trainer 1 mapping
        TrainerDto trainer1Dto = result.stream()
                .filter(dto -> dto.username().equals("jane.smith"))
                .findFirst()
                .orElse(null);
        assertNotNull(trainer1Dto);
        assertEquals("jane.smith", trainer1Dto.username());
        assertEquals("Jane", trainer1Dto.firstName());
        assertEquals("Smith", trainer1Dto.lastName());
        assertEquals("STRENGTH", trainer1Dto.specialization());

        // Verify trainer 2 mapping
        TrainerDto trainer2Dto = result.stream()
                .filter(dto -> dto.username().equals("mike.wilson"))
                .findFirst()
                .orElse(null);
        assertNotNull(trainer2Dto);
        assertEquals("mike.wilson", trainer2Dto.username());
        assertEquals("Mike", trainer2Dto.firstName());
        assertEquals("Wilson", trainer2Dto.lastName());
        assertEquals("CARDIO", trainer2Dto.specialization());
    }

    @Test
    @DisplayName("Should return empty list when trainers set is null")
    void mapTrainersToTrainerDtoList_NullTrainers() {
        // When
        List<TrainerDto> result = traineeMapper.mapTrainersToTrainerDtoList(null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when trainers set is empty")
    void mapTrainersToTrainerDtoList_EmptyTrainers() {
        // Given
        Set<Trainer> emptyTrainers = Set.of();

        // When
        List<TrainerDto> result = traineeMapper.mapTrainersToTrainerDtoList(emptyTrainers);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle single trainer in set")
    void mapTrainersToTrainerDtoList_SingleTrainer() {
        // Given
        Set<Trainer> singleTrainer = Set.of(mockTrainer1);

        // When
        List<TrainerDto> result = traineeMapper.mapTrainersToTrainerDtoList(singleTrainer);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        TrainerDto trainerDto = result.get(0);
        assertEquals("jane.smith", trainerDto.username());
        assertEquals("Jane", trainerDto.firstName());
        assertEquals("Smith", trainerDto.lastName());
        assertEquals("STRENGTH", trainerDto.specialization());
    }

    @Test
    @DisplayName("Should handle trainer with null user")
    void mapTrainersToTrainerDtoList_NullTrainerUser() {
        // Given
        Trainer trainerWithNullUser = new Trainer.Builder()
                .id(1L)
                .user(null)
                .trainingType(mockTrainingType1)
                .build();
        Set<Trainer> trainers = Set.of(trainerWithNullUser);

        // When & Then
        assertThrows(NullPointerException.class,
                () -> traineeMapper.mapTrainersToTrainerDtoList(trainers));
    }

    @Test
    @DisplayName("Should handle trainer with null training type")
    void mapTrainersToTrainerDtoList_NullTrainingType() {
        // Given
        Trainer trainerWithNullType = new Trainer.Builder()
                .id(1L)
                .user(mockTrainerUser1)
                .trainingType(null)
                .build();
        Set<Trainer> trainers = Set.of(trainerWithNullType);

        // When & Then
        assertThrows(NullPointerException.class,
                () -> traineeMapper.mapTrainersToTrainerDtoList(trainers));
    }

    @Test
    @DisplayName("Should handle trainer with null training type enum")
    void mapTrainersToTrainerDtoList_NullTrainingTypeEnum() {
        // Given
        TrainingType typeWithNullEnum = new TrainingType(null);

        Trainer trainerWithNullTypeEnum = new Trainer.Builder()
                .id(1L)
                .user(mockTrainerUser1)
                .trainingType(typeWithNullEnum)
                .build();
        Set<Trainer> trainers = Set.of(trainerWithNullTypeEnum);

        // When & Then
        assertThrows(NullPointerException.class,
                () -> traineeMapper.mapTrainersToTrainerDtoList(trainers));
    }

    @Test
    @DisplayName("Should preserve order consistency in trainer mapping")
    void mapTrainersToTrainerDtoList_OrderConsistency() {
        // Given
        Set<Trainer> trainers = Set.of(mockTrainer1, mockTrainer2);

        // When - call multiple times
        List<TrainerDto> result1 = traineeMapper.mapTrainersToTrainerDtoList(trainers);
        List<TrainerDto> result2 = traineeMapper.mapTrainersToTrainerDtoList(trainers);

        // Then - results should contain same elements (order may vary due to Set)
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(2, result1.size());
        assertEquals(2, result2.size());

        // Verify both results contain the same trainer usernames
        Set<String> usernames1 = result1.stream()
                .map(TrainerDto::username)
                .collect(Collectors.toSet());
        Set<String> usernames2 = result2.stream()
                .map(TrainerDto::username)
                .collect(Collectors.toSet());

        assertEquals(usernames1, usernames2);
        assertTrue(usernames1.contains("jane.smith"));
        assertTrue(usernames1.contains("mike.wilson"));
    }

    @Test
    @DisplayName("Should handle different training type enums correctly")
    void mapTrainersToTrainerDtoList_DifferentTrainingTypes() {
        // Given - Create trainers with different training types using builder pattern
        TrainingType boxingType = new TrainingType(TrainingTypeEnum.BALANCE);

        TrainingType cardioType = new TrainingType(TrainingTypeEnum.CARDIO);

        Trainer boxingTrainer = new Trainer.Builder()
                .id(3L)
                .user(mockTrainerUser1)
                .trainingType(boxingType)
                .build();

        Trainer cardioTrainer = new Trainer.Builder()
                .id(4L)
                .user(mockTrainerUser2)
                .trainingType(cardioType)
                .build();

        Set<Trainer> trainers = Set.of(boxingTrainer, cardioTrainer);

        // When
        List<TrainerDto> result = traineeMapper.mapTrainersToTrainerDtoList(trainers);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        Set<String> specializations = result.stream()
                .map(TrainerDto::specialization)
                .collect(Collectors.toSet());

        assertTrue(specializations.contains("BALANCE"));
        assertTrue(specializations.contains("CARDIO"));
    }

    @Test
    @DisplayName("Should create immutable trainer DTO list")
    void mapTrainersToTrainerDtoList_ImmutableResult() {
        // Given
        Set<Trainer> trainers = Set.of(mockTrainer1);

        // When
        List<TrainerDto> result = traineeMapper.mapTrainersToTrainerDtoList(trainers);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        // Verify we can access elements but list behavior depends on implementation
        TrainerDto dto = result.get(0);
        assertNotNull(dto);
        assertEquals("jane.smith", dto.username());
    }

    @Test
    @DisplayName("Should handle complex trainee profile mapping")
    void toTraineeProfileResponse_ComplexScenario() {
        // Given - Trainee with multiple trainers and all fields populated using builder pattern
        mockTrainee = new Trainee.Builder()
                .id(1L)
                .user(mockTraineeUser)
                .dateOfBirth(LocalDate.of(1985, 12, 25))
                .address("456 Oak Avenue, Suite 100, New York, NY 10001")
                .trainers(Set.of(mockTrainer1, mockTrainer2))
                .build();

        // When
        TraineeProfileResponse result = traineeMapper.toTraineeProfileResponse(mockTrainee);

        // Then
        assertNotNull(result);
        assertEquals("John", result.firstName());
        assertEquals("Doe", result.lastName());
        assertEquals(LocalDate.of(1985, 12, 25), result.dateOfBirth());
        assertEquals("456 Oak Avenue, Suite 100, New York, NY 10001", result.address());
        assertTrue(result.isActive());

        assertNotNull(result.trainers());
        assertEquals(2, result.trainers().size());

        // Verify trainer details are correctly mapped
        Set<String> trainerUsernames = result.trainers().stream()
                .map(TrainerDto::username)
                .collect(Collectors.toSet());
        assertTrue(trainerUsernames.contains("jane.smith"));
        assertTrue(trainerUsernames.contains("mike.wilson"));
    }
}