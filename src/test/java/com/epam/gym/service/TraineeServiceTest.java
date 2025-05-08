package com.epam.gym.service;

import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.User;
import com.epam.gym.exception.TraineeCreationException;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.util.UsernamePasswordUtil;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TraineeServiceTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private UsernamePasswordUtil usernamePasswordUtil;

    @InjectMocks
    private TraineeService traineeService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTrainee_shouldSaveAndReturnTrainee() {
        String firstName = "John";
        String lastName = "Doe";
        LocalDate dob = LocalDate.of(1990, 1, 1);
        String address = "123 Street";
        String username = "johndoe";
        String password = "pass123";

        when(usernamePasswordUtil.generateUsername(firstName, lastName)).thenReturn(username);
        when(usernamePasswordUtil.generatePassword()).thenReturn(password);

        ArgumentCaptor<Trainee> traineeCaptor = ArgumentCaptor.forClass(Trainee.class);
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trainee trainee = traineeService.createTrainee(firstName, lastName, dob, address);

        verify(traineeRepository).save(traineeCaptor.capture());
        Trainee savedTrainee = traineeCaptor.getValue();

        assertEquals(username, savedTrainee.getUser().getUsername());
        assertEquals(firstName, savedTrainee.getUser().getFirstName());
        assertEquals(address, savedTrainee.getAddress());
        assertEquals(dob, savedTrainee.getDateOfBirth());
        assertTrue(savedTrainee.getUser().isActive());
    }

    @Test
    void createTrainee_shouldThrowTraineeCreationException_whenSaveFails() {
        String firstName = "John";
        String lastName = "Doe";
        LocalDate dob = LocalDate.of(1990, 1, 1);
        String address = "123 Main St";

        String username = "jdoe";
        String password = "generatedPass";

        when(usernamePasswordUtil.generateUsername(firstName, lastName)).thenReturn(username);
        when(usernamePasswordUtil.generatePassword()).thenReturn(password);

        when(traineeRepository.save(any(Trainee.class)))
                .thenThrow(new RuntimeException("Database error"));

        TraineeCreationException exception = assertThrows(
                TraineeCreationException.class,
                () -> traineeService.createTrainee(firstName, lastName, dob, address)
        );

        assertEquals("Failed to create trainee", exception.getMessage());
        assertTrue(exception.getCause() instanceof RuntimeException);
    }


    @Test
    void findTraineeByUsername_shouldReturnTrainee() {
        String username = "johndoe";
        Trainee trainee = mock(Trainee.class);
        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(trainee));

        Trainee result = traineeService.findTraineeByUsername(username);

        assertEquals(trainee, result);
    }

    @Test
    void findTraineeByUsername_shouldThrowNoResultException() {
        String username = "missing";
        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.empty());

        NoResultException ex = assertThrows(NoResultException.class, () -> traineeService.findTraineeByUsername(username));
        assertEquals("Trainee not found", ex.getMessage());
    }

    @Test
    void updateTrainee_shouldSaveWhenDataChanges() {
        String username = "john.doe";
        LocalDate newDob = LocalDate.of(1992, 2, 2);
        String newAddress = "456 Avenue";

        User existingUser = new User.Builder()
                .firstName("John")
                .lastName("Doe")
                .username(username)
                .isActive(true)
                .build();

        Trainee existingTrainee = new Trainee.Builder()
                .user(existingUser)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Street")
                .build();

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(existingTrainee));

        traineeService.updateTrainee(username, "John", "Doe", newDob, newAddress, true);

        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void updateTrainee_shouldNotSaveWhenNoChanges() {
        String username = "johndoe";

        User existingUser = new User.Builder()
                .firstName("John")
                .lastName("Doe")
                .username(username)
                .isActive(true)
                .build();

        Trainee existingTrainee = new Trainee.Builder()
                .user(existingUser)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Street")
                .build();

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(existingTrainee));

        traineeService.updateTrainee(username, "John", "Doe", null, null, true);

        verify(traineeRepository, never()).save(any());
    }

    @Test
    void updateTrainee_shouldSaveWhenOnlyAddressChanges() {
        String username = "johndoe";

        User existingUser = new User.Builder()
                .firstName("John")
                .lastName("Doe")
                .username(username)
                .isActive(true)
                .build();

        Trainee existingTrainee = new Trainee.Builder()
                .user(existingUser)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Street")
                .build();

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(existingTrainee));

        traineeService.updateTrainee(username, "John", "Doe", null, "456 Avenue", true);

        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void updateTrainee_shouldSaveWhenOnlyDateOfBirthChanges() {
        String username = "johndoe";

        User existingUser = new User.Builder()
                .firstName("John")
                .lastName("Doe")
                .username(username)
                .isActive(true)
                .build();

        Trainee existingTrainee = new Trainee.Builder()
                .user(existingUser)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Street")
                .build();

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(existingTrainee));

        traineeService.updateTrainee(username, "John", "Doe", LocalDate.of(1995, 5, 5), null, true);

        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void updateTrainee_shouldSaveWhenIsActiveChanges() {
        String username = "johndoe";

        User existingUser = new User.Builder()
                .firstName("John")
                .lastName("Doe")
                .username(username)
                .isActive(true)
                .build();

        Trainee existingTrainee = new Trainee.Builder()
                .user(existingUser)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Street")
                .build();

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(existingTrainee));

        traineeService.updateTrainee(username, "John", "Doe", null, null, false);

        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void updateTrainee_shouldSaveWhenFirstNameChanges() {
        String username = "johndoe";

        User existingUser = new User.Builder()
                .firstName("John")
                .lastName("Doe")
                .username(username)
                .isActive(true)
                .build();

        Trainee existingTrainee = new Trainee.Builder()
                .user(existingUser)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Street")
                .build();

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(existingTrainee));

        traineeService.updateTrainee(username, "Johnny", "Doe", null, null, true);

        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void updateTrainee_shouldSaveWhenLastNameChanges() {
        String username = "johndoe";

        User existingUser = new User.Builder()
                .firstName("John")
                .lastName("Doe")
                .username(username)
                .isActive(true)
                .build();

        Trainee existingTrainee = new Trainee.Builder()
                .user(existingUser)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Street")
                .build();

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(existingTrainee));

        traineeService.updateTrainee(username, "John", "Smith", null, null, true);

        verify(traineeRepository).save(any(Trainee.class));
    }


    @Test
    void deleteTrainee_shouldDeleteTrainee() {
        String username = "johndoe";
        Trainee trainee = mock(Trainee.class);
        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(trainee));

        traineeService.deleteTrainee(username);

        verify(traineeRepository).delete(trainee);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }
}

