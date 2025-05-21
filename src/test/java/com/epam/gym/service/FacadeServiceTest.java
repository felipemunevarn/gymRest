package com.epam.gym.service;

import com.epam.gym.dto.*;
import com.epam.gym.entity.*;
import com.epam.gym.entity.TrainingTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FacadeServiceTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @Mock
    private TrainingTypeService trainingTypeService;

    @InjectMocks
    private FacadeService facadeService;

    private Trainee mockTrainee;
    private Trainer mockTrainer;
    private TrainingType mockTrainingType;
    private User mockUserTrainee;
    private User mockUserTrainer;
    private Training mockTraining;

    @BeforeEach
    void setUp() {
        // Setup common mock objects using builder pattern
        mockUserTrainee = User.builder()
                .username("trainee.user")
                .password("password")
                .firstName("Trainee")
                .lastName("User")
                .isActive(true)
                .build();

        mockTrainee = Trainee.builder()
                .user(mockUserTrainee)
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .address("Trainee Address")
                // Assuming builder initializes collections or they are added later
//                .trainers(Collections.emptyList()) // Initialize to avoid NullPointerException
                .trainers(Collections.emptySet())
                .build();

        mockUserTrainer = User.builder()
                .username("trainer.user")
                .password("password")
                .firstName("Trainer")
                .lastName("User")
                .isActive(true)
                .build();

        mockTrainingType = new TrainingType(TrainingTypeEnum.CARDIO);

        mockTrainer = Trainer.builder()
                .user(mockUserTrainer)
                .trainingType(mockTrainingType)
                // Assuming builder initializes collections or they are added later
//                .trainees(Collections.emptyList()) // Initialize to avoid NullPointerException
                .trainees(Collections.emptySet())
                .build();

        mockTraining = Training.builder()
                .id(1L)
                .name("Test Training")
                .date(LocalDate.now())
                .duration(60)
                .trainingType(mockTrainingType)
                .trainee(mockTrainee)
                .trainer(mockTrainer)
                .build();
    }

    ////////////////////////////////////////////////
    //////////// TRAINEE TESTS /////////////////////
    ////////////////////////////////////////////////

//    @Test
//    void registerTrainee_Success() {
//        TraineeRegistrationRequest request = new TraineeRegistrationRequest(
//                "New", "Trainee", LocalDate.of(2001, 2, 2), "New Address");
//
//        when(traineeService.createTrainee(anyString(), anyString(), any(LocalDate.class), anyString()))
//                .thenReturn(mockTrainee);
//
//        TraineeRegistrationResponse response = facadeService.registerTrainee(request);
//
//        assertNotNull(response);
//        assertEquals(mockUserTrainee.getUsername(), response.username());
//        assertEquals(mockUserTrainee.getPassword(), response.password());
//
//        verify(traineeService).createTrainee(
//                eq(request.firstName()),
//                eq(request.lastName()),
//                eq(request.dateOfBirth()),
//                eq(request.address())
//        );
//    }

    @Test
    void getTraineeByUsername_Success() {
        // Setup a trainee with a trainer for this test using builder
        User trainerUser = User.builder()
                .username("trainer.linked")
                .firstName("Linked")
                .lastName("Trainer")
                .build();
        TrainingType linkedTrainingType = new TrainingType(TrainingTypeEnum.STRENGTH);
        Trainer linkedTrainer = Trainer.builder()
                .user(trainerUser)
                .trainingType(linkedTrainingType)
                .build();

        // Create a new mockTrainee with the linked trainer using builder
        Trainee traineeWithTrainer = Trainee.builder()
                .user(mockUserTrainee)
                .dateOfBirth(mockTrainee.getDateOfBirth())
                .address(mockTrainee.getAddress())
                .trainers(Set.of(linkedTrainer))
                .build();

        when(traineeService.findTraineeByUsername(anyString())).thenReturn(traineeWithTrainer);

        TraineeProfileResponse response = facadeService.getTraineeByUsername("trainee.user");

        assertNotNull(response);
        assertEquals(mockUserTrainee.getFirstName(), response.firstName());
        assertEquals(mockUserTrainee.getLastName(), response.lastName());
        assertEquals(mockTrainee.getDateOfBirth(), response.dateOfBirth());
        assertEquals(mockTrainee.getAddress(), response.address());
        assertEquals(mockUserTrainee.isActive(), response.isActive());
        assertNotNull(response.trainers());
        assertEquals(1, response.trainers().size());
        assertEquals("trainer.linked", response.trainers().getFirst().username());
        assertEquals("Linked", response.trainers().getFirst().firstName());
        assertEquals("Trainer", response.trainers().getFirst().lastName());
        assertEquals("STRENGTH", response.trainers().getFirst().specialization());

        verify(traineeService).findTraineeByUsername(eq("trainee.user"));
    }

    // Note: Testing the case where findTraineeByUsername returns null would require
    // the underlying service to potentially return Optional or throw an exception,
    // which isn't explicitly handled in the original FacadeService code.
    // The current tests cover the path where the trainee is found.

    @Test
    void updateTrainee_Success() {
        TraineeUpdateRequest request = new TraineeUpdateRequest(
                "trainee.user", "Updated", "Trainee", LocalDate.of(2001, 2, 2), "Updated Address", false);

        // Mock the update call (void method)
        doNothing().when(traineeService).updateTrainee(
                anyString(), anyString(), anyString(), any(LocalDate.class), anyString(), anyBoolean());

        // Mock the subsequent get call using builder
        User updatedUser = User.builder()
                .username("trainee.user")
                .firstName("Updated")
                .lastName("Trainee")
                .isActive(false)
                .build();
        Trainee updatedTrainee = Trainee.builder()
                .user(updatedUser)
                .dateOfBirth(LocalDate.of(2001, 2, 2))
                .address("Updated Address")
//                .trainers(Collections.emptyList())
                .trainers(Collections.emptySet())
                .build();

        when(traineeService.findTraineeByUsername(anyString())).thenReturn(updatedTrainee);

        TraineeProfileResponse response = facadeService.updateTrainee(request);

        assertNotNull(response);
        assertEquals("Updated", response.firstName());
        assertEquals("Trainee", response.lastName());
        assertEquals(LocalDate.of(2001, 2, 2), response.dateOfBirth());
        assertEquals("Updated Address", response.address());
        assertFalse(response.isActive());

        verify(traineeService).updateTrainee(
                eq(request.username()),
                eq(request.firstName()),
                eq(request.lastName()),
                eq(request.dateOfBirth()),
                eq(request.address()),
                eq(request.isActive())
        );
        verify(traineeService).findTraineeByUsername(eq(request.username()));
    }

    @Test
    void deleteTrainee_Success() {
        doNothing().when(traineeService).deleteTrainee(anyString());

        facadeService.deleteTrainee("trainee.user");

        verify(traineeService).deleteTrainee(eq("trainee.user"));
    }

    @Test
    void updateTraineeTrainers_Success() {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(List.of("trainer.user"));

        // Mock the trainer service call
        when(trainerService.getTrainersByUsernames(anyList())).thenReturn(List.of(mockTrainer));

        // Mock the trainee service call - assuming it returns the updated Trainee
        when(traineeService.updateTraineeTrainers(anyString(), anyList())).thenReturn(mockTrainee);

        TraineeTrainerResponse response = facadeService.updateTraineeTrainers("trainee.user", request);

        assertNotNull(response);
        assertNotNull(response.trainers());
        assertEquals(1, response.trainers().size());
        assertEquals(mockUserTrainer.getUsername(), response.trainers().getFirst().username());
        assertEquals(mockUserTrainer.getFirstName(), response.trainers().getFirst().firstName());
        assertEquals(mockUserTrainer.getLastName(), response.trainers().getFirst().lastName());
        assertEquals(mockTrainingType.getType().toString(), response.trainers().getFirst().specialization());

        verify(trainerService).getTrainersByUsernames(eq(request.trainerUsernames()));
        verify(traineeService).updateTraineeTrainers(eq("trainee.user"), eq(List.of(mockTrainer)));
    }

    @Test
    void changeTraineeActiveStatus_Success() {
        doNothing().when(traineeService).changeActiveStatus(anyString(), anyBoolean());

        facadeService.changeTraineeActiveStatus("trainee.user", false);

        verify(traineeService).changeActiveStatus(eq("trainee.user"), eq(false));
    }

    ////////////////////////////////////////////////
    //////////// TRAINER TESTS /////////////////////
    ////////////////////////////////////////////////

    @Test
    void registerTrainer_Success() {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest(
                "New", "Trainer", "FLEXIBILITY");

        when(trainingTypeService.findByType("FLEXIBILITY")).thenReturn(mockTrainingType);
        when(trainerService.createTrainer(anyString(), anyString(), any(TrainingType.class)))
                .thenReturn(mockTrainer);

        TrainerRegistrationResponse response = facadeService.registerTrainer(request);

        assertNotNull(response);
        assertEquals(mockUserTrainer.getUsername(), response.username());
        assertEquals(mockUserTrainer.getPassword(), response.password());

        verify(trainingTypeService).findByType(eq(request.specialization()));
        verify(trainerService).createTrainer(
                eq(request.firstName()),
                eq(request.lastName()),
                eq(mockTrainingType)
        );
    }

    @Test
    void getTrainerByUsername_Success() {
        // Setup a trainer with a trainee for this test using builder
        User traineeUser = User.builder()
                .username("trainee.linked")
                .firstName("Linked")
                .lastName("Trainee")
                .build();
        Trainee linkedTrainee = Trainee.builder()
                .user(traineeUser)
                .build();

        // Create a new mockTrainer with the linked trainee using builder
        Trainer trainerWithTrainee = Trainer.builder()
                .user(mockUserTrainer)
                .trainingType(mockTrainingType)
                .trainees(Set.of(linkedTrainee))
                .build();

        when(trainerService.findTrainerByUsername(anyString())).thenReturn(trainerWithTrainee);

        TrainerProfileResponse response = facadeService.getTrainerByUsername("trainer.user");

        assertNotNull(response);
        assertEquals(mockUserTrainer.getFirstName(), response.firstName());
        assertEquals(mockUserTrainer.getLastName(), response.lastName());
        assertEquals(mockTrainingType, response.specialization());
        assertEquals(mockUserTrainer.isActive(), response.isActive());
        assertNotNull(response.trainees());
        assertEquals(1, response.trainees().size());
        assertEquals("trainee.linked", response.trainees().getFirst().username());
        assertEquals("Linked", response.trainees().getFirst().firstName());
        assertEquals("Trainee", response.trainees().getFirst().lastName());

        verify(trainerService).findTrainerByUsername(eq("trainer.user"));
    }

    // Note: Similar to getTraineeByUsername, testing the null case for
    // findTrainerByUsername would require changes to the underlying service
    // and potentially the facade's error handling.

    @Test
    void updateTrainer_Success() {
        TrainerUpdateRequest request = new TrainerUpdateRequest(
                "trainer.user",
                "Updated",
                "Trainer",
                "FLEXIBILITY",
                false);

        TrainingType updatedTrainingType = new TrainingType(TrainingTypeEnum.FLEXIBILITY);

        when(trainingTypeService.findByType("FLEXIBILITY")).thenReturn(updatedTrainingType);
        doNothing().when(trainerService).updateTrainer(
                anyString(), anyString(), anyString(), any(TrainingType.class), anyBoolean());

        User updatedUser = User.builder()
                .username("trainer.user")
                .firstName("Updated")
                .lastName("Trainer")
                .isActive(false)
                .build();
        Trainer updatedTrainer = Trainer.builder()
                .user(updatedUser)
                .trainingType(updatedTrainingType)
//                .trainees(Collections.emptyList())
                .trainees(Collections.emptySet())
                .build();

        when(trainerService.findTrainerByUsername(anyString())).thenReturn(updatedTrainer);

        TrainerProfileResponse response = facadeService.updateTrainer(request);

        assertNotNull(response);
        assertEquals("Updated", response.firstName());
        assertEquals("Trainer", response.lastName());
//        assertEquals("FLEXIBILITY", response.specialization().getType());
        assertFalse(response.isActive());

        verify(trainingTypeService).findByType(eq(request.specialization()));
        verify(trainerService).updateTrainer(
                eq(request.username()),
                eq(request.firstName()),
                eq(request.lastName()),
                eq(updatedTrainingType),
                eq(request.isActive())
        );
        verify(trainerService).findTrainerByUsername(eq(request.username()));
    }

    @Test
    void getAvailableTrainersForTrainee_Success() {
        when(trainerService.getAvailableTrainersForTrainee(anyString()))
                .thenReturn(List.of(mockTrainer));

        List<TrainerDto> response = facadeService.getAvailableTrainersForTrainee("trainee.user");

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(mockUserTrainer.getUsername(), response.getFirst().username());
        assertEquals(mockUserTrainer.getFirstName(), response.getFirst().firstName());
        assertEquals(mockUserTrainer.getLastName(), response.getFirst().lastName());
        assertEquals(mockTrainingType.getType().toString(), response.getFirst().specialization());

        verify(trainerService).getAvailableTrainersForTrainee(eq("trainee.user"));
    }

    @Test
    void changeTrainerActiveStatus_Success() {
        doNothing().when(trainerService).changeActiveStatus(anyString(), anyBoolean());

        facadeService.changeTrainerActiveStatus("trainer.user", false);

        verify(trainerService).changeActiveStatus(eq("trainer.user"), eq(false));
    }

    ////////////////////////////////////////////////
    //////////// TRAINING TESTS ////////////////////
    ////////////////////////////////////////////////

    @Test
    void registerTraining_Success() {
        TrainingRegistrationRequest request = new TrainingRegistrationRequest(
                "trainee.user", "trainer.user", "New Training", LocalDate.now(), 90);

        when(traineeService.findTraineeByUsername(anyString())).thenReturn(mockTrainee);
        when(trainerService.findTrainerByUsername(anyString())).thenReturn(mockTrainer);
        doNothing().when(trainingService).createTraining(
                any(Trainee.class), any(Trainer.class), anyString(), any(TrainingType.class), any(LocalDate.class), anyInt());

        facadeService.registerTraining(request);

        verify(traineeService).findTraineeByUsername(eq(request.traineeUsername()));
        verify(trainerService).findTrainerByUsername(eq(request.trainerUsername()));
        verify(trainingService).createTraining(
                eq(mockTrainee),
                eq(mockTrainer),
                eq(request.name()),
                eq(mockTrainer.getTrainingType()), // Note: Uses trainer's training type
                eq(request.date()),
                eq(request.duration())
        );
    }

    // Note: Testing null trainee or trainer in registerTraining would require
    // the underlying service calls to potentially return Optional or throw exceptions.

    @Test
    void findTraineeTrainings_Success_WithSpecialization() {
        TraineeTrainingRequest request = new TraineeTrainingRequest(
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 12, 31),
                "trainer.user",
                "CARDIO");

        when(trainingTypeService.findByType("CARDIO")).thenReturn(mockTrainingType);
        when(trainingService.getTraineeTrainings(
                anyString(), any(LocalDate.class), any(LocalDate.class), anyString(), any(TrainingType.class)))
                .thenReturn(List.of(mockTraining));

        List<TraineeTrainingResponse> response = facadeService.findTraineeTrainings("trainee.user", request);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(mockTraining.getName(), response.getFirst().trainingName());
        // Note: Date toString() behavior can vary, asserting part of it or a specific format is better
        assertTrue(response.getFirst().trainingDate().contains(mockTraining.getDate().toString()));
        assertEquals(mockTraining.getTrainingType().getType().toString(), response.getFirst().trainingType());
        assertEquals(mockTraining.getDuration(), response.getFirst().trainingDuration());
        assertEquals(mockTraining.getTrainer().getUser().getUsername(), response.getFirst().trainerName());

        verify(trainingTypeService).findByType(eq(request.specialization()));
        verify(trainingService).getTraineeTrainings(
                eq("trainee.user"),
                eq(request.from()),
                eq(request.to()),
                eq(request.trainerName()),
                eq(mockTrainingType)
        );
    }

    @Test
    void findTraineeTrainings_Success_WithoutSpecialization() {
        TraineeTrainingRequest request = new TraineeTrainingRequest(
                LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31), "trainer.user", null);

        // No call to trainingTypeService.findByType when specialization is null
        when(trainingService.getTraineeTrainings(
                anyString(), any(LocalDate.class), any(LocalDate.class), anyString(), eq(null)))
                .thenReturn(List.of(mockTraining));

        List<TraineeTrainingResponse> response = facadeService.findTraineeTrainings("trainee.user", request);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(mockTraining.getName(), response.getFirst().trainingName());
        assertTrue(response.getFirst().trainingDate().contains(mockTraining.getDate().toString()));
        assertEquals(mockTraining.getTrainingType().getType().toString(), response.getFirst().trainingType());
        assertEquals(mockTraining.getDuration(), response.getFirst().trainingDuration());
        assertEquals(mockTraining.getTrainer().getUser().getUsername(), response.getFirst().trainerName());

        verifyNoInteractions(trainingTypeService); // Verify findByType was NOT called
        verify(trainingService).getTraineeTrainings(
                eq("trainee.user"),
                eq(request.from()),
                eq(request.to()),
                eq(request.trainerName()),
                eq(null) // Ensure null is passed
        );
    }


    @Test
    void findTrainerTrainings_Success() {
        TrainerTrainingRequest request = new TrainerTrainingRequest(
                LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31), "trainee.user");

        when(trainingService.getTrainerTrainings(
                anyString(), any(LocalDate.class), any(LocalDate.class), anyString()))
                .thenReturn(List.of(mockTraining));

        List<TrainerTrainingResponse> response = facadeService.findTrainerTrainings("trainer.user", request);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(mockTraining.getName(), response.getFirst().trainingName());
        assertTrue(response.getFirst().trainingDate().contains(mockTraining.getDate().toString()));
        assertEquals(mockTraining.getTrainingType().getType().toString(), response.getFirst().trainingType());
        assertEquals(mockTraining.getDuration(), response.getFirst().trainingDuration());
        assertEquals(mockTraining.getTrainee().getUser().getUsername(), response.getFirst().traineeName());

        verify(trainingService).getTrainerTrainings(
                eq("trainer.user"),
                eq(request.from()),
                eq(request.to()),
                eq(request.traineeName())
        );
    }

    ////////////////////////////////////////////////
    //////////// TRAINING TYPE TESTS ///////////////
    ////////////////////////////////////////////////

    @Test
    void findAllTrainingTypes_Success() {
        TrainingType type1 = new TrainingType(TrainingTypeEnum.CARDIO);

        TrainingType type2 = new TrainingType(TrainingTypeEnum.STRENGTH);

        when(trainingTypeService.findAllTrainingTypes()).thenReturn(List.of(type1, type2));

        List<TrainingTypeResponse> response = facadeService.findAllTrainingTypes();

        assertNotNull(response);
        assertEquals(2, response.size());
//        assertEquals(1L, response.get(0).trainingTypeId());
        assertEquals("CARDIO", response.get(0).trainingType());
//        assertEquals(2L, response.get(1).trainingTypeId());
        assertEquals("STRENGTH", response.get(1).trainingType());

        verify(trainingTypeService).findAllTrainingTypes();
    }
}
