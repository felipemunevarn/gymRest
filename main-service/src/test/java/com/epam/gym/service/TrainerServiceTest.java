package com.epam.gym.service;

import com.epam.gym.dto.*;
import com.epam.gym.entity.*;
import com.epam.gym.exception.TraineeCreationException;
import com.epam.gym.mapper.TraineeMapper;
import com.epam.gym.mapper.TrainerMapper;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingTypeRepository;
import com.epam.gym.security.util.JwtUtil;
import com.epam.gym.util.UsernamePasswordUtil;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository; // Added missing mock

    @Mock
    private UsernamePasswordUtil usernamePasswordUtil;

    @Mock
    private TrainerMapper trainerMapper;

    @Mock
    private TraineeMapper traineeMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TrainerService trainerService;

    private User testUser;
    private Trainer testTrainer;
    private TrainingType testTrainingType;
    private TrainerRegistrationRequest registrationRequest;
    private TrainerUpdateRequest updateRequest;
    private TrainerRegistrationResponse registrationResponse;
    private TrainerProfileResponse profileResponse;
    private TrainerRegistrationRequest request;

    @BeforeEach
    void setUp() {
        testUser = new User.Builder()
                .firstName("John")
                .lastName("Trainer")
                .username("john.trainer")
                .password("password123")
                .isActive(true)
                .build();

        testTrainingType = new TrainingType(TrainingTypeEnum.FLEXIBILITY);

        testTrainer = new Trainer.Builder()
                .id(1L)
                .user(testUser)
                .trainingType(testTrainingType)
                .trainees(new HashSet<>())
                .build();

        registrationRequest = new TrainerRegistrationRequest(
                "John",
                "Trainer",
                "FLEXIBILITY"
        );

        updateRequest = new TrainerUpdateRequest(
                "john.trainer",
                "Jane",
                "Smith",
                "STRENGTH",
                false
        );

        registrationResponse = new TrainerRegistrationResponse(
                "john.trainer",
                "password123",
                "jwt");

        profileResponse = new TrainerProfileResponse(
                "Jane",
                "Smith",
                new TrainingType(TrainingTypeEnum.STRENGTH),
                false,
                Collections.emptyList()
        );

        request = new TrainerRegistrationRequest(
                "John",
                "Doe",
                "STRENGTH");

        passwordEncoder = mock(PasswordEncoder.class);
    }

    @Test
    void createTrainer_Success() {
        // given
        TrainerRegistrationRequest request = new TrainerRegistrationRequest(
                "John",
                "Doe",
                "STRENGTH");
        String username = "john.doe";
        String rawPassword = "plainPass";
        String encodedPassword = "encodedPass";
        String token = "jwt-token";

        TrainingType trainingType = new TrainingType();

        User user = new User.Builder()
                .firstName("John")
                .lastName("Doe")
                .username(username)
                .password(encodedPassword)
                .isActive(true)
                .role(User.Role.TRAINER)
                .build();

        Trainer trainer = new Trainer.Builder()
                .trainingType(trainingType)
                .user(user)
                .build();

        // mocks
        when(usernamePasswordUtil.generateUsername("John", "Doe")).thenReturn(username);
        when(usernamePasswordUtil.generatePassword()).thenReturn(rawPassword);
        when(trainingTypeRepository.findByType(TrainingTypeEnum.STRENGTH))
                .thenReturn(Optional.of(trainingType));
        when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);
        when(jwtUtil.generateToken(username, rawPassword)).thenReturn(token);

        TrainerRegistrationResponse expectedResponse =
                new TrainerRegistrationResponse(
                        "John",
                        "Doe",
                        token);
        when(trainerMapper.toTrainerRegistrationResponse(
                any(Trainer.class),
                eq("plainPass"),
                eq("jwt-token")
        )).thenReturn(expectedResponse);

        // when
        TrainerRegistrationResponse actualResponse = trainerService.createTrainer(request);

        // then
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);

        verify(trainerRepository).save(any(Trainer.class));
        verify(jwtUtil).generateToken(username, rawPassword);
        verify(trainerMapper).toTrainerRegistrationResponse(
                any(Trainer.class),
                eq("plainPass"),
                eq("jwt-token")
        );
    }

    @Test
    void createTrainer_TrainingTypeNotFound() {
        // Arrange
        when(usernamePasswordUtil.generateUsername("John", "Doe")).thenReturn("john.doe");
        when(usernamePasswordUtil.generatePassword()).thenReturn("raw-pass");
        when(trainingTypeRepository.findByType(any())).thenReturn(Optional.empty());

        // Act + Assert
        TraineeCreationException ex = assertThrows(
                TraineeCreationException.class,
                () -> trainerService.createTrainer(request)
        );
        assertEquals("Type not found", ex.getMessage());

        verify(trainingTypeRepository).findByType(TrainingTypeEnum.STRENGTH);
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void createTrainer_SaveFails() {
        // Arrange
        String username = "john.doe";
        String password = "raw-pass";

        TrainingType type = new TrainingType();

        when(usernamePasswordUtil.generateUsername("John", "Doe")).thenReturn(username);
        when(usernamePasswordUtil.generatePassword()).thenReturn(password);
        when(trainingTypeRepository.findByType(TrainingTypeEnum.STRENGTH)).thenReturn(Optional.of(type));
        when(trainerRepository.save(any(Trainer.class))).thenThrow(new RuntimeException("DB error"));

        // Act + Assert
        TraineeCreationException ex = assertThrows(
                TraineeCreationException.class,
                () -> trainerService.createTrainer(request)
        );
        assertEquals("Failed to create trainer", ex.getMessage());
    }

    @Test
    void findTrainerByUsername_Success() {
        // Arrange
        when(trainerRepository.findByUserUsername("john.trainer")).thenReturn(Optional.of(testTrainer));
        when(trainerMapper.toTrainerProfileResponse(testTrainer)).thenReturn(profileResponse);

        // Act
        TrainerProfileResponse result = trainerService.findTrainerByUsername("john.trainer");

        // Assert
        assertNotNull(result);
        assertEquals(profileResponse, result);
        verify(trainerRepository).findByUserUsername("john.trainer");
        verify(trainerMapper).toTrainerProfileResponse(testTrainer);
    }

    @Test
    void findTrainerByUsername_ThrowsNoResultException() {
        // Arrange
        when(trainerRepository.findByUserUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        NoResultException exception = assertThrows(
                NoResultException.class,
                () -> trainerService.findTrainerByUsername("nonexistent")
        );

        assertEquals("Trainer not found", exception.getMessage());
        verify(trainerRepository).findByUserUsername("nonexistent");
        verify(trainerMapper, never()).toTrainerProfileResponse(any(Trainer.class));
    }

    @Test
    void updateTrainer_WithAllFieldsChanged_Success() {
        // Arrange
        when(trainerRepository.findByUserUsername("john.trainer")).thenReturn(Optional.of(testTrainer));
        when(trainingTypeRepository.findByType(TrainingTypeEnum.STRENGTH))
                .thenReturn(Optional.of(new TrainingType(TrainingTypeEnum.STRENGTH))); // Added mock

        Trainer updatedTrainer = createUpdatedTrainer();
        when(trainerRepository.save(any(Trainer.class))).thenReturn(updatedTrainer);
        when(trainerMapper.toTrainerProfileResponse(updatedTrainer)).thenReturn(profileResponse);

        // Act
        TrainerProfileResponse result = trainerService.updateTrainer(updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(profileResponse, result);
        verify(trainerRepository).findByUserUsername("john.trainer");
        verify(trainingTypeRepository).findByType(TrainingTypeEnum.STRENGTH);
        verify(trainerRepository).save(any(Trainer.class));
        verify(trainerMapper).toTrainerProfileResponse(updatedTrainer);
    }

    @Test
    void updateTrainer_WithOnlyFirstNameChanged_Success() {
        // Arrange
        TrainerUpdateRequest partialUpdateRequest = new TrainerUpdateRequest(
                "john.trainer",
                "Jane", // Only first name changed
                "Trainer",
                null,
                true
        );

        when(trainerRepository.findByUserUsername("john.trainer")).thenReturn(Optional.of(testTrainer));

        Trainer updatedTrainer = createUpdatedTrainer();
        when(trainerRepository.save(any(Trainer.class))).thenReturn(updatedTrainer);
        when(trainerMapper.toTrainerProfileResponse(updatedTrainer)).thenReturn(profileResponse);

        // Act
        TrainerProfileResponse result = trainerService.updateTrainer(partialUpdateRequest);

        // Assert
        assertNotNull(result);
        verify(trainerRepository).save(any(Trainer.class));
    }

    @Test
    void updateTrainer_WithOnlyLastNameChanged_Success() {
        // Arrange
        TrainerUpdateRequest partialUpdateRequest = new TrainerUpdateRequest(
                "john.trainer",
                "John",
                "Smith", // Only last name changed
                null,
                true
        );

        when(trainerRepository.findByUserUsername("john.trainer")).thenReturn(Optional.of(testTrainer));

        Trainer updatedTrainer = createUpdatedTrainer();
        when(trainerRepository.save(any(Trainer.class))).thenReturn(updatedTrainer);
        when(trainerMapper.toTrainerProfileResponse(updatedTrainer)).thenReturn(profileResponse);

        // Act
        TrainerProfileResponse result = trainerService.updateTrainer(partialUpdateRequest);

        // Assert
        assertNotNull(result);
        verify(trainerRepository).save(any(Trainer.class));
    }

    @Test
    void updateTrainer_WithOnlyActiveStatusChanged_Success() {
        // Arrange
        TrainerUpdateRequest partialUpdateRequest = new TrainerUpdateRequest(
                "john.trainer",
                "John",
                "Trainer",
                null,
                false // Only active status changed
        );

        when(trainerRepository.findByUserUsername("john.trainer")).thenReturn(Optional.of(testTrainer));

        Trainer updatedTrainer = createUpdatedTrainer();
        when(trainerRepository.save(any(Trainer.class))).thenReturn(updatedTrainer);
        when(trainerMapper.toTrainerProfileResponse(updatedTrainer)).thenReturn(profileResponse);

        // Act
        TrainerProfileResponse result = trainerService.updateTrainer(partialUpdateRequest);

        // Assert
        assertNotNull(result);
        verify(trainerRepository).save(any(Trainer.class));
    }

    @Test
    void updateTrainer_WithSpecializationOnly_Success() {
        // Arrange
        TrainerUpdateRequest partialUpdateRequest = new TrainerUpdateRequest(
                "john.trainer",
                "John",
                "Trainer",
                "STRENGTH", // Only specialization changed
                true
        );

        when(trainerRepository.findByUserUsername("john.trainer")).thenReturn(Optional.of(testTrainer));
        when(trainingTypeRepository.findByType(TrainingTypeEnum.STRENGTH))
                .thenReturn(Optional.of(new TrainingType(TrainingTypeEnum.STRENGTH))); // Added mock

        Trainer updatedTrainer = createUpdatedTrainer();
        when(trainerRepository.save(any(Trainer.class))).thenReturn(updatedTrainer);
        when(trainerMapper.toTrainerProfileResponse(updatedTrainer)).thenReturn(profileResponse);

        // Act
        TrainerProfileResponse result = trainerService.updateTrainer(partialUpdateRequest);

        // Assert
        assertNotNull(result);
        verify(trainerRepository).save(any(Trainer.class));
        verify(trainingTypeRepository).findByType(TrainingTypeEnum.STRENGTH);
    }

    @Test
    void updateTrainer_NoChanges_DoesNotSave() {
        // Arrange
        TrainerUpdateRequest noChangeRequest = new TrainerUpdateRequest(
                "john.trainer",
                "John",    // Same as original
                "Trainer", // Same as original
                null,      // No specialization change
                true       // Same as original
        );

        when(trainerRepository.findByUserUsername("john.trainer")).thenReturn(Optional.of(testTrainer));
        when(trainerMapper.toTrainerProfileResponse(testTrainer)).thenReturn(profileResponse);

        // Act
        TrainerProfileResponse result = trainerService.updateTrainer(noChangeRequest);

        // Assert
        assertNotNull(result);
        verify(trainerRepository).findByUserUsername("john.trainer");
        verify(trainerRepository, never()).save(any(Trainer.class)); // Should not save if no changes
        verify(trainerMapper).toTrainerProfileResponse(testTrainer);
    }

    @Test
    void updateTrainer_TrainerNotFound_ThrowsNoResultException() {
        // Arrange
        when(trainerRepository.findByUserUsername("nonexistent")).thenReturn(Optional.empty());

        TrainerUpdateRequest requestWithNonexistentUser = new TrainerUpdateRequest(
                "nonexistent",
                "Jane",
                "Smith",
                "STRENGTH",
                false
        );

        // Act & Assert
        NoResultException exception = assertThrows(
                NoResultException.class,
                () -> trainerService.updateTrainer(requestWithNonexistentUser)
        );

        assertEquals("Trainer not found", exception.getMessage());
        verify(trainerRepository).findByUserUsername("nonexistent");
        verify(trainerRepository, never()).save(any(Trainer.class));
    }

    @Test
    void getAvailableTrainersForTrainee_Success() {
        // Arrange
        String traineeUsername = "trainee.user";
        List<Trainer> trainerList = Arrays.asList(testTrainer, createSecondTrainer());
        List<TrainerDto> expectedTrainerDtos = Arrays.asList(
                new TrainerDto("john.trainer", "John", "Trainer", "FITNESS"),
                new TrainerDto("jane.trainer", "Jane", "Trainer", "STRENGTH")
        );

        when(trainerRepository.findActiveTrainersNotAssignedToTrainee(traineeUsername)).thenReturn(trainerList);
        when(traineeMapper.mapTrainersToTrainerDtoList(any(Set.class))).thenReturn(expectedTrainerDtos);

        // Act
        List<TrainerDto> result = trainerService.getAvailableTrainersForTrainee(traineeUsername);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedTrainerDtos, result);
        verify(trainerRepository).findActiveTrainersNotAssignedToTrainee(traineeUsername);
        verify(traineeMapper).mapTrainersToTrainerDtoList(any(Set.class));
    }

    @Test
    void getAvailableTrainersForTrainee_EmptyList_Success() {
        // Arrange
        String traineeUsername = "trainee.user";
        List<Trainer> emptyTrainerList = Collections.emptyList();
        List<TrainerDto> emptyTrainerDtos = Collections.emptyList();

        when(trainerRepository.findActiveTrainersNotAssignedToTrainee(traineeUsername)).thenReturn(emptyTrainerList);
        when(traineeMapper.mapTrainersToTrainerDtoList(any(Set.class))).thenReturn(emptyTrainerDtos);

        // Act
        List<TrainerDto> result = trainerService.getAvailableTrainersForTrainee(traineeUsername);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(trainerRepository).findActiveTrainersNotAssignedToTrainee(traineeUsername);
        verify(traineeMapper).mapTrainersToTrainerDtoList(any(Set.class));
    }

    @Test
    void getTrainersByUsernames_Success() {
        // Arrange
        List<String> usernames = Arrays.asList("john.trainer", "jane.trainer");
        List<Trainer> expectedTrainers = Arrays.asList(testTrainer, createSecondTrainer());

        when(trainerRepository.findAllByUserUsernameIn(usernames)).thenReturn(expectedTrainers);

        // Act
        List<Trainer> result = trainerService.getTrainersByUsernames(usernames);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedTrainers, result);
        verify(trainerRepository).findAllByUserUsernameIn(usernames);
    }

    @Test
    void getTrainersByUsernames_EmptyList_Success() {
        // Arrange
        List<String> emptyUsernames = Collections.emptyList();
        List<Trainer> emptyTrainers = Collections.emptyList();

        when(trainerRepository.findAllByUserUsernameIn(emptyUsernames)).thenReturn(emptyTrainers);

        // Act
        List<Trainer> result = trainerService.getTrainersByUsernames(emptyUsernames);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(trainerRepository).findAllByUserUsernameIn(emptyUsernames);
    }

    @Test
    void changeActiveStatus_FromActiveToInactive_Success() {
        // Arrange
        when(trainerRepository.findByUserUsername("john.trainer")).thenReturn(Optional.of(testTrainer));

        User updatedUser = testUser.toBuilder().isActive(false).build();
        Trainer updatedTrainer = testTrainer.toBuilder().user(updatedUser).build();
        when(trainerRepository.save(any(Trainer.class))).thenReturn(updatedTrainer);

        // Act
        trainerService.changeActiveStatus("john.trainer", false);

        // Assert
        verify(trainerRepository).findByUserUsername("john.trainer");
        verify(trainerRepository).save(any(Trainer.class));
    }

    @Test
    void changeActiveStatus_FromInactiveToActive_Success() {
        // Arrange
        User inactiveUser = testUser.toBuilder().isActive(false).build();
        Trainer inactiveTrainer = testTrainer.toBuilder().user(inactiveUser).build();

        when(trainerRepository.findByUserUsername("john.trainer")).thenReturn(Optional.of(inactiveTrainer));

        User activatedUser = inactiveUser.toBuilder().isActive(true).build();
        Trainer activatedTrainer = inactiveTrainer.toBuilder().user(activatedUser).build();
        when(trainerRepository.save(any(Trainer.class))).thenReturn(activatedTrainer);

        // Act
        trainerService.changeActiveStatus("john.trainer", true);

        // Assert
        verify(trainerRepository).findByUserUsername("john.trainer");
        verify(trainerRepository).save(any(Trainer.class));
    }

    @Test
    void changeActiveStatus_NoChange_DoesNotSave() {
        // Arrange
        when(trainerRepository.findByUserUsername("john.trainer")).thenReturn(Optional.of(testTrainer));

        // Act - trying to set active status to the same value (true)
        trainerService.changeActiveStatus("john.trainer", true);

        // Assert
        verify(trainerRepository).findByUserUsername("john.trainer");
        verify(trainerRepository, never()).save(any(Trainer.class)); // Should not save if no change
    }

    @Test
    void changeActiveStatus_TrainerNotFound_ThrowsNoResultException() {
        // Arrange
        when(trainerRepository.findByUserUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        NoResultException exception = assertThrows(
                NoResultException.class,
                () -> trainerService.changeActiveStatus("nonexistent", false)
        );

        assertEquals("Trainer not found", exception.getMessage());
        verify(trainerRepository).findByUserUsername("nonexistent");
        verify(trainerRepository, never()).save(any(Trainer.class));
    }

    // Helper methods
    private Trainer createUpdatedTrainer() {
        User updatedUser = new User.Builder()
                .firstName("Jane")
                .lastName("Smith")
                .username("john.trainer")
                .password("password123")
                .isActive(false)
                .build();

        TrainingType strengthType = new TrainingType(TrainingTypeEnum.STRENGTH);

        return new Trainer.Builder()
                .id(1L)
                .user(updatedUser)
                .trainingType(strengthType)
                .trainees(new HashSet<>())
                .build();
    }

    private Trainer createSecondTrainer() {
        User secondUser = new User.Builder()
                .firstName("Jane")
                .lastName("Trainer")
                .username("jane.trainer")
                .password("password456")
                .isActive(true)
                .build();

        TrainingType strengthType = new TrainingType(TrainingTypeEnum.STRENGTH);

        return new Trainer.Builder()
                .id(2L)
                .user(secondUser)
                .trainingType(strengthType)
                .trainees(new HashSet<>())
                .build();
    }
}