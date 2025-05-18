package com.epam.gym.service;

import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.TrainingTypeEnum;
import com.epam.gym.entity.User;
import com.epam.gym.exception.TraineeCreationException;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.util.UsernamePasswordUtil;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UsernamePasswordUtil usernamePasswordUtil;

    @InjectMocks
    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateTrainer_shouldCreateAndReturnTrainer() {
        String firstName = "John";
        String lastName = "Doe";
        TrainingType trainingType = new TrainingType(TrainingTypeEnum.CARDIO);

        when(usernamePasswordUtil.generateUsername(firstName, lastName)).thenReturn("john.doe");
        when(usernamePasswordUtil.generatePassword()).thenReturn("password123");

        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(i -> i.getArgument(0));

        Trainer result = trainerService.createTrainer(firstName, lastName, trainingType);

        verify(trainerRepository).save(captor.capture());
        assertEquals("john.doe", captor.getValue().getUser().getUsername());
        assertEquals(trainingType, captor.getValue().getTrainingType());
        assertEquals(result, captor.getValue());
    }

    @Test
    void testCreateTrainer_shouldThrowExceptionWhenFails() {
        when(usernamePasswordUtil.generateUsername(any(), any())).thenReturn("error.user");
        when(usernamePasswordUtil.generatePassword()).thenReturn("pass");
        when(trainerRepository.save(any(Trainer.class))).thenThrow(new RuntimeException("DB Error"));

        assertThrows(TraineeCreationException.class, () ->
                trainerService.createTrainer("err",
                        "user",
                        new TrainingType(TrainingTypeEnum.STRENGTH)));
    }

    @Test
    void testFindTrainerByUsername_shouldReturnTrainer() {
        String username = "trainer1";
        Trainer trainer = Trainer.builder().user(User.builder().username(username).build()).build();
        when(trainerRepository.findByUserUsername(username)).thenReturn(Optional.of(trainer));

        Trainer result = trainerService.findTrainerByUsername(username);
        assertEquals(username, result.getUser().getUsername());
    }

    @Test
    void testFindTrainerByUsername_shouldThrowWhenNotFound() {
        when(trainerRepository.findByUserUsername("ghost"))
                .thenReturn(Optional.empty());
        assertThrows(NoResultException.class, () -> trainerService.findTrainerByUsername("ghost"));
    }

    @Test
    void testUpdateTrainer_shouldUpdateFields() {
        String username = "trainer.update";
        User user = User.builder().firstName("A").lastName("B").isActive(true).username(username).build();
        TrainingType oldType = new TrainingType(TrainingTypeEnum.STRENGTH);
        TrainingType newType = new TrainingType(TrainingTypeEnum.CARDIO);
        Trainer existing = Trainer.builder().user(user).trainingType(oldType).build();

        when(trainerRepository.findByUserUsername(username)).thenReturn(Optional.of(existing));

        trainerService.updateTrainer(username, "C", "D", newType, false);

        verify(trainerRepository).save(any(Trainer.class));
    }

    @Test
    void testUpdateTrainer_shouldSkipWhenNoChange() {
        String username = "no.change";
        User user = User.builder().firstName("A").lastName("B").isActive(true).username(username).build();
        TrainingType type = new TrainingType(TrainingTypeEnum.CARDIO);
        Trainer existing = Trainer.builder().user(user).trainingType(type).build();

        when(trainerRepository.findByUserUsername(username)).thenReturn(Optional.of(existing));

        trainerService.updateTrainer(username, "A", "B", type, true);
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void testGetAvailableTrainersForTrainee_shouldReturnList() {
        String username = "trainer1";
        Trainer trainer = Trainer.builder().user(User.builder().username(username).build()).build();
        when(trainerRepository.findActiveTrainersNotAssignedToTrainee("trainee1"))
                .thenReturn(List.of(trainer));
        List<Trainer> result = trainerService.getAvailableTrainersForTrainee("trainee1");
        assertEquals(1, result.size());
    }

    @Test
    void testGetTrainersByUsernames_shouldReturnList() {
        String username = "trainer1";
        Trainer trainer = Trainer.builder().user(User.builder().username(username).build()).build();
        when(trainerRepository.findAllByUserUsernameIn(List.of("t1", "t2")))
                .thenReturn(List.of(trainer, trainer));
        List<Trainer> result = trainerService.getTrainersByUsernames(List.of("t1", "t2"));
        assertEquals(2, result.size());
    }

    @Test
    void testChangeActiveStatus_shouldUpdate() {
        String username = "trainer.active";
        User user = User.builder().username(username).isActive(true).build();
        Trainer trainer = Trainer.builder().user(user).build();

        when(trainerRepository.findByUserUsername(username)).thenReturn(Optional.of(trainer));

        trainerService.changeActiveStatus(username, false);

        verify(trainerRepository).save(any(Trainer.class));
    }

    @Test
    void testChangeActiveStatus_shouldSkipUpdate() {
        String username = "trainer.same";
        User user = User.builder().username(username).isActive(true).build();
        Trainer trainer = Trainer.builder().user(user).build();

        when(trainerRepository.findByUserUsername(username)).thenReturn(Optional.of(trainer));

        trainerService.changeActiveStatus(username, true);
        verify(trainerRepository, never()).save(any());
    }
}
