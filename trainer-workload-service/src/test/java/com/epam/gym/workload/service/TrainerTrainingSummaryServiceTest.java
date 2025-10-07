package com.epam.gym.workload.service;

import com.epam.gym.workload.document.TrainerTrainingSummary;
import com.epam.gym.workload.document.TrainerTrainingSummary.YearSummary;
import com.epam.gym.workload.document.TrainerTrainingSummary.MonthSummary;
import com.epam.gym.workload.messaging.TrainerWorkloadEvent;
import com.epam.gym.workload.repository.TrainerTrainingSummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerTrainingSummaryServiceTest {

    @Mock
    private TrainerTrainingSummaryRepository repository;

    @InjectMocks
    private TrainerTrainingSummaryService service;

    private TrainerWorkloadEvent.TrainerWorkloadPayload testPayload;
    private String transactionId;

    @BeforeEach
    void setUp() {
        transactionId = "test-transaction-123";

        testPayload = TrainerWorkloadEvent.TrainerWorkloadPayload.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate("2024-09-15")
                .trainingDuration(60)
                .actionType(TrainerWorkloadEvent.TrainerWorkloadPayload.ActionType.ADD)
                .build();
    }

    @Test
    void testProcessTrainingEvent_NewTrainer_CreatesNewDocument() {
        // Arrange
        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.empty());
        when(repository.save(any(TrainerTrainingSummary.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        service.processTrainingEvent(testPayload, transactionId);

        // Assert
        ArgumentCaptor<TrainerTrainingSummary> captor = ArgumentCaptor.forClass(TrainerTrainingSummary.class);
        verify(repository).save(captor.capture());

        TrainerTrainingSummary saved = captor.getValue();
        assertEquals("john.doe", saved.getTrainerUsername());
        assertEquals("John", saved.getTrainerFirstName());
        assertEquals("Doe", saved.getTrainerLastName());
        assertTrue(saved.getTrainerStatus());
        assertEquals(1, saved.getYears().size());
        assertEquals(2024, saved.getYears().get(0).getYear());
        assertEquals(1, saved.getYears().get(0).getMonths().size());
        assertEquals(9, saved.getYears().get(0).getMonths().get(0).getMonth());
        assertEquals(60, saved.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration());
    }

    @Test
    void testProcessTrainingEvent_ExistingTrainerSameMonth_AddsDuration() {
        // Arrange
        TrainerTrainingSummary existing = createExistingTrainer(2024, 9, 30);

        // Verify initial state
        System.out.println(existing);
//        System.out.println("Initial duration: " + existing.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration());

        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(existing));

        // Capture what gets saved
        ArgumentCaptor<TrainerTrainingSummary> captor = ArgumentCaptor.forClass(TrainerTrainingSummary.class);
        when(repository.save(captor.capture())).thenReturn(null);

        // Act
        service.processTrainingEvent(testPayload, transactionId);

        // Assert - Get the captured value
        TrainerTrainingSummary saved = captor.getValue();

        // Debug output
        System.out.println("Saved object: " + saved);
        System.out.println("Years size: " + saved.getYears().size());
        if (!saved.getYears().isEmpty()) {
            System.out.println("First year: " + saved.getYears().get(0).getYear());
            System.out.println("Months size: " + saved.getYears().get(0).getMonths().size());
            if (!saved.getYears().get(0).getMonths().isEmpty()) {
                System.out.println("First month: " + saved.getYears().get(0).getMonths().get(0).getMonth());
                System.out.println("Duration: " + saved.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration());
            }
        }

        // Assertions
        assertNotNull(saved);
        assertEquals(90, saved.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration());

        verify(repository).findByTrainerUsername("john.doe");
        verify(repository).save(any(TrainerTrainingSummary.class));
    }

    @Test
    void testProcessTrainingEvent_ExistingTrainerNewMonth_CreatesNewMonth() {
        // Arrange
        TrainerTrainingSummary existing = createExistingTrainer(2024, 8, 120);
        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(existing));
        when(repository.save(any(TrainerTrainingSummary.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        service.processTrainingEvent(testPayload, transactionId);

        // Assert
        ArgumentCaptor<TrainerTrainingSummary> captor = ArgumentCaptor.forClass(TrainerTrainingSummary.class);
        verify(repository).save(captor.capture());

        TrainerTrainingSummary saved = captor.getValue();
        assertEquals(2, saved.getYears().get(0).getMonths().size());

        // Verify September was added
        Optional<MonthSummary> septemberMonth = saved.getYears().get(0).getMonths().stream()
                .filter(m -> m.getMonth().equals(9))
                .findFirst();

        assertTrue(septemberMonth.isPresent());
        assertEquals(60, septemberMonth.get().getTrainingSummaryDuration());
    }

    @Test
    void testProcessTrainingEvent_ExistingTrainerNewYear_CreatesNewYear() {
        // Arrange
        TrainerTrainingSummary existing = createExistingTrainer(2023, 12, 200);
        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(existing));
        when(repository.save(any(TrainerTrainingSummary.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        service.processTrainingEvent(testPayload, transactionId);

        // Assert
        ArgumentCaptor<TrainerTrainingSummary> captor = ArgumentCaptor.forClass(TrainerTrainingSummary.class);
        verify(repository).save(captor.capture());

        TrainerTrainingSummary saved = captor.getValue();
        assertEquals(2, saved.getYears().size());

        // Verify 2024 was added
        Optional<YearSummary> year2024 = saved.getYears().stream()
                .filter(y -> y.getYear().equals(2024))
                .findFirst();

        assertTrue(year2024.isPresent());
        assertEquals(1, year2024.get().getMonths().size());
        assertEquals(60, year2024.get().getMonths().get(0).getTrainingSummaryDuration());
    }

    @Test
    void testProcessTrainingEvent_DeleteAction_SubtractsDuration() {
        // Arrange
        TrainerTrainingSummary existing = createExistingTrainer(2024, 9, 120);
        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(existing));
        when(repository.save(any(TrainerTrainingSummary.class))).thenAnswer(i -> i.getArguments()[0]);

        testPayload.setActionType(TrainerWorkloadEvent.TrainerWorkloadPayload.ActionType.DELETE);
        testPayload.setTrainingDuration(50);

        // Act
        service.processTrainingEvent(testPayload, transactionId);

        // Assert
        ArgumentCaptor<TrainerTrainingSummary> captor = ArgumentCaptor.forClass(TrainerTrainingSummary.class);
        verify(repository).save(captor.capture());

        TrainerTrainingSummary saved = captor.getValue();
        assertEquals(70, saved.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration());
    }

    @Test
    void testProcessTrainingEvent_DeleteAction_DoesNotGoBelowZero() {
        // Arrange
        TrainerTrainingSummary existing = createExistingTrainer(2024, 9, 30);
        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(existing));
        when(repository.save(any(TrainerTrainingSummary.class))).thenAnswer(i -> i.getArguments()[0]);

        testPayload.setActionType(TrainerWorkloadEvent.TrainerWorkloadPayload.ActionType.DELETE);
        testPayload.setTrainingDuration(100);

        // Act
        service.processTrainingEvent(testPayload, transactionId);

        // Assert
        ArgumentCaptor<TrainerTrainingSummary> captor = ArgumentCaptor.forClass(TrainerTrainingSummary.class);
        verify(repository).save(captor.capture());

        TrainerTrainingSummary saved = captor.getValue();
        assertEquals(0, saved.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration());
    }

    @Test
    void testProcessTrainingEvent_UpdatesTrainerStatus() {
        // Arrange
        TrainerTrainingSummary existing = createExistingTrainer(2024, 9, 60);
        existing.setTrainerStatus(false);
        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(existing));
        when(repository.save(any(TrainerTrainingSummary.class))).thenAnswer(i -> i.getArguments()[0]);

        testPayload.setActive(true);

        // Act
        service.processTrainingEvent(testPayload, transactionId);

        // Assert
        ArgumentCaptor<TrainerTrainingSummary> captor = ArgumentCaptor.forClass(TrainerTrainingSummary.class);
        verify(repository).save(captor.capture());

        TrainerTrainingSummary saved = captor.getValue();
        assertTrue(saved.getTrainerStatus());
    }

    @Test
    void testGetTrainerSummary_ReturnsOptional() {
        // Arrange
        TrainerTrainingSummary existing = createExistingTrainer(2024, 9, 60);
        when(repository.findByTrainerUsername("john.doe")).thenReturn(Optional.of(existing));

        // Act
        Optional<TrainerTrainingSummary> result = service.getTrainerSummary("john.doe");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("john.doe", result.get().getTrainerUsername());
        verify(repository).findByTrainerUsername("john.doe");
    }

    @Test
    void testGetTrainerSummary_ReturnsEmptyWhenNotFound() {
        // Arrange
        when(repository.findByTrainerUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<TrainerTrainingSummary> result = service.getTrainerSummary("nonexistent");

        // Assert
        assertFalse(result.isPresent());
        verify(repository).findByTrainerUsername("nonexistent");
    }

    // Helper method to create existing trainer
    private TrainerTrainingSummary createExistingTrainer(int year, int month, int duration) {
        MonthSummary monthSummary = MonthSummary.builder()
                .month(month)
                .trainingSummaryDuration(duration)
                .build();

        YearSummary yearSummary = YearSummary.builder()
                .year(year)
                .months(new ArrayList<>())
                .build();
        yearSummary.getMonths().add(monthSummary);

        TrainerTrainingSummary trainerTrainingSummary = TrainerTrainingSummary.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .trainerStatus(true)
                .years(new ArrayList<>())
                .build();

        trainerTrainingSummary.getYears().add(yearSummary);

        return trainerTrainingSummary;

    }

}