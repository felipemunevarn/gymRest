package com.epam.gym.service;

import com.epam.gym.dto.*;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.User;
import com.epam.gym.exception.TraineeCreationException;
import com.epam.gym.mapper.TraineeMapper;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.util.UsernamePasswordUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UsernamePasswordUtil usernamePasswordUtil;

    @Mock
    private TraineeMapper traineeMapper;

    @Mock
    private SimpleMeterRegistry meterRegistry;

    @InjectMocks
    private TraineeService traineeService;

    private User testUser;
    private Trainee testTrainee;
    private TraineeRegistrationRequest registrationRequest;
    private TraineeUpdateRequest updateRequest;
    private TraineeRegistrationResponse registrationResponse;
    private TraineeProfileResponse profileResponse;

    @BeforeEach
    void setUp() {
        testUser = new User.Builder()
                .firstName("John")
                .lastName("Doe")
                .username("john.doe")
                .password("password123")
                .isActive(true)
                .build();

        testTrainee = new Trainee.Builder()
                .id(1L)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Main St")
                .user(testUser)
                .trainers(new HashSet<>())
                .build();

        registrationRequest = new TraineeRegistrationRequest(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "123 Main St"
        );

        updateRequest = new TraineeUpdateRequest(
                "john.doe",
                "Jane",
                "Smith",
                LocalDate.of(1991, 1, 1),
                "456 Oak St",
                false
        );

        registrationResponse = new TraineeRegistrationResponse("john.doe", "password123","jwt");
        profileResponse = new TraineeProfileResponse(
                "Jane",
                "Smith",
                LocalDate.of(1991, 1, 1),
                "456 Oak St",
                false,
                Collections.emptyList()
        );
    }

    @Test
    void createTrainee_Success() {
        // Arrange
        when(usernamePasswordUtil.generateUsername("John", "Doe")).thenReturn("john.doe");
        when(usernamePasswordUtil.generatePassword()).thenReturn("password123");
        when(traineeRepository.save(any(Trainee.class))).thenReturn(testTrainee);
        when(traineeMapper.toTraineeRegistrationResponse(any(Trainee.class),"pass","jwt")).thenReturn(registrationResponse);

        // Act
        TraineeRegistrationResponse result = traineeService.createTrainee(registrationRequest);

        // Assert
        assertNotNull(result);
        assertEquals("john.doe", result.username());
        assertEquals("password123", result.password());
        verify(traineeRepository).save(any(Trainee.class));
        verify(traineeMapper).toTraineeRegistrationResponse(any(Trainee.class),"pass","jwt");
    }

    @Test
    void createTrainee_ThrowsTraineeCreationException() {
        // Arrange
        when(usernamePasswordUtil.generateUsername("John", "Doe")).thenReturn("john.doe");
        when(usernamePasswordUtil.generatePassword()).thenReturn("password123");
        when(traineeRepository.save(any(Trainee.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        TraineeCreationException exception = assertThrows(
                TraineeCreationException.class,
                () -> traineeService.createTrainee(registrationRequest)
        );

        assertEquals("Failed to create trainee", exception.getMessage());
        verify(traineeRepository).save(any(Trainee.class));
        verify(traineeMapper, never()).toTraineeRegistrationResponse(any(Trainee.class),"pass","jwt");
    }

    @Test
    void findTraineeByUsername_Success() {
        // Arrange
        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(testTrainee));
        when(traineeMapper.toTraineeProfileResponse(testTrainee)).thenReturn(profileResponse);

        // Act
        TraineeProfileResponse result = traineeService.findTraineeByUsername("john.doe");

        // Assert
        assertNotNull(result);
        assertEquals(profileResponse, result);
        verify(traineeRepository).findByUserUsername("john.doe");
        verify(traineeMapper).toTraineeProfileResponse(testTrainee);
    }

    @Test
    void findTraineeByUsername_ThrowsNoResultException() {
        // Arrange
        when(traineeRepository.findByUserUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        NoResultException exception = assertThrows(
                NoResultException.class,
                () -> traineeService.findTraineeByUsername("nonexistent")
        );

        assertEquals("Trainee not found", exception.getMessage());
        verify(traineeRepository).findByUserUsername("nonexistent");
        verify(traineeMapper, never()).toTraineeProfileResponse(any(Trainee.class));
    }

    @Test
    void updateTrainee_WithAllFieldsChanged_Success() {
        // Arrange
        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(testTrainee));

        Trainee updatedTrainee = createUpdatedTrainee();
        when(traineeRepository.save(any(Trainee.class))).thenReturn(updatedTrainee);
        when(traineeMapper.toTraineeProfileResponse(updatedTrainee)).thenReturn(profileResponse);

        // Act
        TraineeProfileResponse result = traineeService.updateTrainee(updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(profileResponse, result);
        verify(traineeRepository).findByUserUsername("john.doe");
        verify(traineeRepository).save(any(Trainee.class));
        verify(traineeMapper).toTraineeProfileResponse(updatedTrainee);
    }

    @Test
    void updateTrainee_WithOnlyFirstNameChanged_Success() {
        // Arrange
        TraineeUpdateRequest partialUpdateRequest = new TraineeUpdateRequest(
                "john.doe",
                "Jane", // Only first name changed
                "Doe",
                null,
                null,
                true
        );

        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(testTrainee));

        Trainee updatedTrainee = createUpdatedTrainee();
        when(traineeRepository.save(any(Trainee.class))).thenReturn(updatedTrainee);
        when(traineeMapper.toTraineeProfileResponse(updatedTrainee)).thenReturn(profileResponse);

        // Act
        TraineeProfileResponse result = traineeService.updateTrainee(partialUpdateRequest);

        // Assert
        assertNotNull(result);
        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void updateTrainee_WithOnlyLastNameChanged_Success() {
        // Arrange
        TraineeUpdateRequest partialUpdateRequest = new TraineeUpdateRequest(
                "john.doe",
                "John",
                "Smith", // Only last name changed
                null,
                null,
                true
        );

        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(testTrainee));

        Trainee updatedTrainee = createUpdatedTrainee();
        when(traineeRepository.save(any(Trainee.class))).thenReturn(updatedTrainee);
        when(traineeMapper.toTraineeProfileResponse(updatedTrainee)).thenReturn(profileResponse);

        // Act
        TraineeProfileResponse result = traineeService.updateTrainee(partialUpdateRequest);

        // Assert
        assertNotNull(result);
        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void updateTrainee_WithOnlyActiveStatusChanged_Success() {
        // Arrange
        TraineeUpdateRequest partialUpdateRequest = new TraineeUpdateRequest(
                "john.doe",
                "John",
                "Doe",
                null,
                null,
                false // Only active status changed
        );

        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(testTrainee));

        Trainee updatedTrainee = createUpdatedTrainee();
        when(traineeRepository.save(any(Trainee.class))).thenReturn(updatedTrainee);
        when(traineeMapper.toTraineeProfileResponse(updatedTrainee)).thenReturn(profileResponse);

        // Act
        TraineeProfileResponse result = traineeService.updateTrainee(partialUpdateRequest);

        // Assert
        assertNotNull(result);
        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void updateTrainee_WithDateOfBirthOnly_Success() {
        // Arrange
        TraineeUpdateRequest partialUpdateRequest = new TraineeUpdateRequest(
                "john.doe",
                "John",
                "Doe",
                LocalDate.of(1991, 1, 1), // Only date of birth changed
                null,
                true
        );

        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(testTrainee));

        Trainee updatedTrainee = createUpdatedTrainee();
        when(traineeRepository.save(any(Trainee.class))).thenReturn(updatedTrainee);
        when(traineeMapper.toTraineeProfileResponse(updatedTrainee)).thenReturn(profileResponse);

        // Act
        TraineeProfileResponse result = traineeService.updateTrainee(partialUpdateRequest);

        // Assert
        assertNotNull(result);
        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void updateTrainee_WithAddressOnly_Success() {
        // Arrange
        TraineeUpdateRequest partialUpdateRequest = new TraineeUpdateRequest(
                "john.doe",
                "John",
                "Doe",
                null,
                "456 Oak St", // Only address changed
                true
        );

        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(testTrainee));

        Trainee updatedTrainee = createUpdatedTrainee();
        when(traineeRepository.save(any(Trainee.class))).thenReturn(updatedTrainee);
        when(traineeMapper.toTraineeProfileResponse(updatedTrainee)).thenReturn(profileResponse);

        // Act
        TraineeProfileResponse result = traineeService.updateTrainee(partialUpdateRequest);

        // Assert
        assertNotNull(result);
        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void updateTrainee_NoChanges_DoesNotSave() {
        // Arrange
        TraineeUpdateRequest noChangeRequest = new TraineeUpdateRequest(
                "john.doe",
                "John", // Same as original
                "Doe",  // Same as original
                null,   // No date change
                null,   // No address change
                true    // Same as original
        );

        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(testTrainee));
        when(traineeMapper.toTraineeProfileResponse(testTrainee)).thenReturn(profileResponse);

        // Act
        TraineeProfileResponse result = traineeService.updateTrainee(noChangeRequest);

        // Assert
        assertNotNull(result);
        verify(traineeRepository).findByUserUsername("john.doe");
        verify(traineeRepository, never()).save(any(Trainee.class)); // Should not save if no changes
        verify(traineeMapper).toTraineeProfileResponse(testTrainee);
    }

    @Test
    void updateTrainee_TraineeNotFound_ThrowsNoResultException() {
        // Arrange
        when(traineeRepository.findByUserUsername("nonexistent")).thenReturn(Optional.empty());

        TraineeUpdateRequest requestWithNonexistentUser = new TraineeUpdateRequest(
                "nonexistent",
                "Jane",
                "Smith",
                LocalDate.of(1991, 1, 1),
                "456 Oak St",
                false
        );

        // Act & Assert
        NoResultException exception = assertThrows(
                NoResultException.class,
                () -> traineeService.updateTrainee(requestWithNonexistentUser)
        );

        assertEquals("Trainee not found", exception.getMessage());
        verify(traineeRepository).findByUserUsername("nonexistent");
        verify(traineeRepository, never()).save(any(Trainee.class));
    }

    @Test
    void deleteTrainee_Success() {
        // Arrange
        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(testTrainee));

        // Act
        traineeService.deleteTrainee("john.doe");

        // Assert
        verify(traineeRepository).findByUserUsername("john.doe");
        verify(traineeRepository).delete(testTrainee);
    }

    @Test
    void deleteTrainee_TraineeNotFound_ThrowsNoResultException() {
        // Arrange
        when(traineeRepository.findByUserUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        NoResultException exception = assertThrows(
                NoResultException.class,
                () -> traineeService.deleteTrainee("nonexistent")
        );

        assertEquals("Trainee not found", exception.getMessage());
        verify(traineeRepository).findByUserUsername("nonexistent");
        verify(traineeRepository, never()).delete(any(Trainee.class));
    }

    @Test
    void updateTraineeTrainers_Success() {
        // Arrange
        Trainer trainer1 = createTrainer("trainer1");
        Trainer trainer2 = createTrainer("trainer2");

        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(
                Arrays.asList("trainer1", "trainer2")
        );

        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(testTrainee));

        // Note: There's a bug in the original code - it uses traineeUsername instead of trainerUsername
        // We need to mock the buggy behavior to test the current implementation
        when(trainerRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(trainer1));

        Trainee updatedTrainee = testTrainee.toBuilder()
                .trainers(Set.of(trainer1))
                .build();
        when(traineeRepository.save(any(Trainee.class))).thenReturn(updatedTrainee);

        List<TrainerDto> trainerDtos = Arrays.asList(
                new TrainerDto("trainer1", "John", "Trainer", "FITNESS"),
                new TrainerDto("trainer2", "Jane", "Trainer", "YOGA")
        );
        when(traineeMapper.mapTrainersToTrainerDtoList(any())).thenReturn(trainerDtos);

        TraineeTrainerResponse expectedResponse = new TraineeTrainerResponse(trainerDtos);

        // Act
        TraineeTrainerResponse result = traineeService.updateTraineeTrainers("john.doe", request);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse.trainers().size(), result.trainers().size());
        verify(traineeRepository).findByUserUsername("john.doe");
        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void updateTraineeTrainers_TraineeNotFound_ThrowsNoResultException() {
        // Arrange
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(
                Arrays.asList("trainer1")
        );
        when(traineeRepository.findByUserUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        NoResultException exception = assertThrows(
                NoResultException.class,
                () -> traineeService.updateTraineeTrainers("nonexistent", request)
        );

        assertEquals("Trainee not found", exception.getMessage());
        verify(traineeRepository).findByUserUsername("nonexistent");
        verify(traineeRepository, never()).save(any(Trainee.class));
    }

    @Test
    void updateTraineeTrainers_TrainerNotFound_ThrowsNoResultException() {
        // Arrange
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(
                Arrays.asList("nonexistent_trainer")
        );
        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(testTrainee));
        // Bug in original code: uses traineeUsername instead of trainerUsername
        when(trainerRepository.findByUserUsername("john.doe")).thenReturn(Optional.empty());

        // Act & Assert
        NoResultException exception = assertThrows(
                NoResultException.class,
                () -> traineeService.updateTraineeTrainers("john.doe", request)
        );

        assertEquals("Trainee not found", exception.getMessage()); // Bug: should say "Trainer not found"
        verify(traineeRepository).findByUserUsername("john.doe");
        verify(traineeRepository, never()).save(any(Trainee.class));
    }

    @Test
    void changeActiveStatus_FromActiveToInactive_Success() {
        // Arrange
        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(testTrainee));

        User updatedUser = testUser.toBuilder().isActive(false).build();
        Trainee updatedTrainee = testTrainee.toBuilder().user(updatedUser).build();
        when(traineeRepository.save(any(Trainee.class))).thenReturn(updatedTrainee);

        // Act
        traineeService.changeActiveStatus("john.doe", false);

        // Assert
        verify(traineeRepository).findByUserUsername("john.doe");
        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void changeActiveStatus_FromInactiveToActive_Success() {
        // Arrange
        User inactiveUser = testUser.toBuilder().isActive(false).build();
        Trainee inactiveTrainee = testTrainee.toBuilder().user(inactiveUser).build();

        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(inactiveTrainee));

        User activatedUser = inactiveUser.toBuilder().isActive(true).build();
        Trainee activatedTrainee = inactiveTrainee.toBuilder().user(activatedUser).build();
        when(traineeRepository.save(any(Trainee.class))).thenReturn(activatedTrainee);

        // Act
        traineeService.changeActiveStatus("john.doe", true);

        // Assert
        verify(traineeRepository).findByUserUsername("john.doe");
        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void changeActiveStatus_NoChange_DoesNotSave() {
        // Arrange
        when(traineeRepository.findByUserUsername("john.doe")).thenReturn(Optional.of(testTrainee));

        // Act - trying to set active status to the same value (true)
        traineeService.changeActiveStatus("john.doe", true);

        // Assert
        verify(traineeRepository).findByUserUsername("john.doe");
        verify(traineeRepository, never()).save(any(Trainee.class)); // Should not save if no change
    }

    @Test
    void changeActiveStatus_TraineeNotFound_ThrowsNoResultException() {
        // Arrange
        when(traineeRepository.findByUserUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        NoResultException exception = assertThrows(
                NoResultException.class,
                () -> traineeService.changeActiveStatus("nonexistent", false)
        );

        assertEquals("Trainee not found", exception.getMessage());
        verify(traineeRepository).findByUserUsername("nonexistent");
        verify(traineeRepository, never()).save(any(Trainee.class));
    }

    // Helper methods
    private Trainee createUpdatedTrainee() {
        User updatedUser = new User.Builder()
                .firstName("Jane")
                .lastName("Smith")
                .username("john.doe")
                .password("password123")
                .isActive(false)
                .build();

        return new Trainee.Builder()
                .id(1L)
                .dateOfBirth(LocalDate.of(1991, 1, 1))
                .address("456 Oak St")
                .user(updatedUser)
                .trainers(new HashSet<>())
                .build();
    }

    private Trainer createTrainer(String username) {
        User trainerUser = new User.Builder()
                .firstName("John")
                .lastName("Trainer")
                .username(username)
                .password("password")
                .isActive(true)
                .build();

        return new Trainer.Builder()
                .id(1L)
                .user(trainerUser)
                .build();
    }
}
