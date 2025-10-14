package com.epam.gym.workload.controller;

import com.epam.gym.workload.document.TrainerTrainingSummary;
import com.epam.gym.workload.document.TrainerTrainingSummary.YearSummary;
import com.epam.gym.workload.document.TrainerTrainingSummary.MonthSummary;
import com.epam.gym.workload.service.TrainerTrainingSummaryService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerSummaryControllerTest {

    @Mock
    private TrainerTrainingSummaryService trainerTrainingSummaryService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private TrainerSummaryController controller;

    private TrainerTrainingSummary sampleTrainerSummary;

    @BeforeEach
    void setUp() {
        sampleTrainerSummary = createSampleTrainerSummary();
    }

    // ========== getTrainerSummary Tests ==========

    @Test
    void testGetTrainerSummary_Success() {
        // Arrange
        String username = "john.doe";
        when(trainerTrainingSummaryService.getTrainerSummary(username))
                .thenReturn(Optional.of(sampleTrainerSummary));

        // Act
        ResponseEntity<TrainerTrainingSummary> response =
                controller.getTrainerSummary(username, httpRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("john.doe", response.getBody().getTrainerUsername());
        assertEquals("John", response.getBody().getTrainerFirstName());
        assertEquals("Doe", response.getBody().getTrainerLastName());

        verify(trainerTrainingSummaryService).getTrainerSummary(username);
    }

    @Test
    void testGetTrainerSummary_NotFound() {
        // Arrange
        String username = "nonexistent";
        when(trainerTrainingSummaryService.getTrainerSummary(username))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<TrainerTrainingSummary> response =
                controller.getTrainerSummary(username, httpRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(trainerTrainingSummaryService).getTrainerSummary(username);
    }

    @Test
    void testGetTrainerSummary_ServiceThrowsException() {
        // Arrange
        String username = "john.doe";
        when(trainerTrainingSummaryService.getTrainerSummary(username))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act
        ResponseEntity<TrainerTrainingSummary> response =
                controller.getTrainerSummary(username, httpRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());

        verify(trainerTrainingSummaryService).getTrainerSummary(username);
    }

    // ========== getTrainerWorkloadMonth Tests ==========

    @Test
    void testGetTrainerWorkloadMonth_Success() {
        // Arrange
        String username = "john.doe";
        Integer year = 2024;
        Integer month = 9;

        when(trainerTrainingSummaryService.getTrainerSummary(username))
                .thenReturn(Optional.of(sampleTrainerSummary));

        // Act
        ResponseEntity<TrainerSummaryController.MonthWorkloadResponse> response =
                controller.getTrainerWorkloadMonth(username, year, month, httpRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(username, response.getBody().getTrainerUsername());
        assertEquals(year, response.getBody().getYear());
        assertEquals(month, response.getBody().getMonth());
        assertEquals(120, response.getBody().getTrainingSummaryDuration());

        verify(trainerTrainingSummaryService).getTrainerSummary(username);
    }

    @Test
    void testGetTrainerWorkloadMonth_InvalidMonthLessThanOne() {
        // Arrange
        String username = "john.doe";
        Integer year = 2024;
        Integer month = 0; // Invalid

        // Act
        ResponseEntity<TrainerSummaryController.MonthWorkloadResponse> response =
                controller.getTrainerWorkloadMonth(username, year, month, httpRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        verify(trainerTrainingSummaryService, never()).getTrainerSummary(any());
    }

    @Test
    void testGetTrainerWorkloadMonth_InvalidMonthGreaterThanTwelve() {
        // Arrange
        String username = "john.doe";
        Integer year = 2024;
        Integer month = 13; // Invalid

        // Act
        ResponseEntity<TrainerSummaryController.MonthWorkloadResponse> response =
                controller.getTrainerWorkloadMonth(username, year, month, httpRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        verify(trainerTrainingSummaryService, never()).getTrainerSummary(any());
    }

    @Test
    void testGetTrainerWorkloadMonth_TrainerNotFound() {
        // Arrange
        String username = "nonexistent";
        Integer year = 2024;
        Integer month = 9;

        when(trainerTrainingSummaryService.getTrainerSummary(username))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<TrainerSummaryController.MonthWorkloadResponse> response =
                controller.getTrainerWorkloadMonth(username, year, month, httpRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(trainerTrainingSummaryService).getTrainerSummary(username);
    }

    @Test
    void testGetTrainerWorkloadMonth_YearNotFound() {
        // Arrange
        String username = "john.doe";
        Integer year = 2023; // Year not in sample data
        Integer month = 9;

        when(trainerTrainingSummaryService.getTrainerSummary(username))
                .thenReturn(Optional.of(sampleTrainerSummary));

        // Act
        ResponseEntity<TrainerSummaryController.MonthWorkloadResponse> response =
                controller.getTrainerWorkloadMonth(username, year, month, httpRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getTrainingSummaryDuration()); // Returns 0 when not found

        verify(trainerTrainingSummaryService).getTrainerSummary(username);
    }

    @Test
    void testGetTrainerWorkloadMonth_MonthNotFound() {
        // Arrange
        String username = "john.doe";
        Integer year = 2024;
        Integer month = 8; // Month not in sample data

        when(trainerTrainingSummaryService.getTrainerSummary(username))
                .thenReturn(Optional.of(sampleTrainerSummary));

        // Act
        ResponseEntity<TrainerSummaryController.MonthWorkloadResponse> response =
                controller.getTrainerWorkloadMonth(username, year, month, httpRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getTrainingSummaryDuration()); // Returns 0 when not found

        verify(trainerTrainingSummaryService).getTrainerSummary(username);
    }

    @Test
    void testGetTrainerWorkloadMonth_ServiceThrowsException() {
        // Arrange
        String username = "john.doe";
        Integer year = 2024;
        Integer month = 9;

        when(trainerTrainingSummaryService.getTrainerSummary(username))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<TrainerSummaryController.MonthWorkloadResponse> response =
                controller.getTrainerWorkloadMonth(username, year, month, httpRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());

        verify(trainerTrainingSummaryService).getTrainerSummary(username);
    }

    @Test
    void testGetTrainerWorkloadMonth_MultipleYearsAndMonths() {
        // Arrange
        String username = "john.doe";
        Integer year = 2024;
        Integer month = 10;

        TrainerTrainingSummary multiYearSummary = createMultiYearTrainerSummary();
        when(trainerTrainingSummaryService.getTrainerSummary(username))
                .thenReturn(Optional.of(multiYearSummary));

        // Act
        ResponseEntity<TrainerSummaryController.MonthWorkloadResponse> response =
                controller.getTrainerWorkloadMonth(username, year, month, httpRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(90, response.getBody().getTrainingSummaryDuration());

        verify(trainerTrainingSummaryService).getTrainerSummary(username);
    }

    // ========== health Tests ==========

    @Test
    void testHealth_ReturnsHealthyStatus() {
        // Act
        ResponseEntity<String> response = controller.health();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Trainer Summary Service is healthy", response.getBody());
    }

    // ========== MonthWorkloadResponse Tests ==========

    @Test
    void testMonthWorkloadResponse_GettersWork() {
        // Arrange
        TrainerSummaryController.MonthWorkloadResponse response =
                new TrainerSummaryController.MonthWorkloadResponse("john.doe", 2024, 9, 120);

        // Assert
        assertEquals("john.doe", response.getTrainerUsername());
        assertEquals(2024, response.getYear());
        assertEquals(9, response.getMonth());
        assertEquals(120, response.getTrainingSummaryDuration());
    }

    // ========== Helper Methods ==========

    private TrainerTrainingSummary createSampleTrainerSummary() {
        MonthSummary september = MonthSummary.builder()
                .month(9)
                .trainingSummaryDuration(120)
                .build();

        YearSummary year2024 = YearSummary.builder()
                .year(2024)
                .months(new ArrayList<>(List.of(september)))
                .build();

        return TrainerTrainingSummary.builder()
                .id("1")
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .trainerStatus(true)
                .years(new ArrayList<>(List.of(year2024)))
                .build();
    }

    private TrainerTrainingSummary createMultiYearTrainerSummary() {
        // Create multiple months for 2024
        MonthSummary september = MonthSummary.builder()
                .month(9)
                .trainingSummaryDuration(120)
                .build();

        MonthSummary october = MonthSummary.builder()
                .month(10)
                .trainingSummaryDuration(90)
                .build();

        YearSummary year2024 = YearSummary.builder()
                .year(2024)
                .months(new ArrayList<>(List.of(september, october)))
                .build();

        // Create 2023 data
        MonthSummary december2023 = MonthSummary.builder()
                .month(12)
                .trainingSummaryDuration(150)
                .build();

        YearSummary year2023 = YearSummary.builder()
                .year(2023)
                .months(new ArrayList<>(List.of(december2023)))
                .build();

        return TrainerTrainingSummary.builder()
                .id("1")
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .trainerStatus(true)
                .years(new ArrayList<>(List.of(year2023, year2024)))
                .build();
    }
}