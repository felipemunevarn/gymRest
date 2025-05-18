package com.epam.gym.service;

import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TraineeServiceTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private UsernamePasswordUtil usernamePasswordUtil;

    @Spy
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
        assertInstanceOf(RuntimeException.class, exception.getCause());
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

    @Test
    void testUpdateTraineeTrainers_shouldUpdateAndReturnUpdatedTrainee() {

        String username = "carol.trainee";
        User user = User.builder().username(username).build();
        Trainee originalTrainee = Trainee.builder().user(user).trainers(Set.of()).build();

        User trainerUser1 = User.builder().username("mary.trainer").build();
        User trainerUser2 = User.builder().username("john.trainer").build();
        Trainer trainer1 = Trainer.builder().user(trainerUser1).build();
        Trainer trainer2 = Trainer.builder().user(trainerUser2).build();
        List<Trainer> newTrainers = List.of(trainer1, trainer2);

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(originalTrainee));
        when(traineeRepository.save(any(Trainee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Trainee result = traineeService.updateTraineeTrainers(username, newTrainers);

        assertEquals(username, result.getUser().getUsername());
        assertEquals(2, result.getTrainers().size());
        assertTrue(result.getTrainers().stream()
                .anyMatch(t -> t.getUser().getUsername().equals("mary.trainer")));
        assertTrue(result.getTrainers().stream()
                .anyMatch(t -> t.getUser().getUsername().equals("john.trainer")));

        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeRepository).save(captor.capture());

        Trainee savedTrainee = captor.getValue();
        assertEquals(new HashSet<>(newTrainers), savedTrainee.getTrainers());
    }

    @Test
    void testChangeActiveStatus_shouldUpdateStatusWhenDifferent() {
        String username = "trainee.username";
        boolean newStatus = false;

        User user = User.builder().username(username).isActive(true).build();
        Trainee trainee = Trainee.builder().user(user).build();

        when(traineeRepository.findByUserUsername(user.getUsername())).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        traineeService.changeActiveStatus(username, newStatus);

        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeRepository).save(captor.capture());

        Trainee saved = captor.getValue();
        assertFalse(saved.getUser().isActive());
        assertEquals(username, saved.getUser().getUsername());
    }

    @Test
    void testChangeActiveStatus_shouldNotUpdateIfStatusIsSame() {
        String username = "john";
        User user = User.builder().username(username).isActive(true).build();
        Trainee trainee = Trainee.builder().user(user).build();

        Mockito.doReturn(trainee).when(traineeService).findTraineeByUsername(username);

        traineeService.changeActiveStatus(username, true);

        verify(traineeRepository, never()).save(any());
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }
}

