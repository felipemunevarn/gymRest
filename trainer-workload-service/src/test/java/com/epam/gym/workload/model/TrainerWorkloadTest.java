package com.epam.gym.workload.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrainerWorkloadTest {

    private TrainerWorkload workload;

    @BeforeEach
    void setUp() {
        workload = TrainerWorkload.builder()
                .username("john.doe")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .build();
    }

    @Test
    void testAddTraining_newMonth() {
        workload.addTraining(2025, 10, 60);
        assertEquals(60, workload.getMonthlyDuration(2025, 10));
    }

    @Test
    void testAddTraining_existingMonth() {
        workload.addTraining(2025, 10, 30);
        workload.addTraining(2025, 10, 45);
        assertEquals(75, workload.getMonthlyDuration(2025, 10));
    }

    @Test
    void testRemoveTraining_partialReduction() {
        workload.addTraining(2025, 10, 90);
        workload.removeTraining(2025, 10, 30);
        assertEquals(60, workload.getMonthlyDuration(2025, 10));
    }

    @Test
    void testRemoveTraining_fullReduction() {
        workload.addTraining(2025, 10, 60);
        workload.removeTraining(2025, 10, 60);
        assertEquals(0, workload.getMonthlyDuration(2025, 10));
        assertFalse(workload.getYearlyWorkload().containsKey(2025));
    }

    @Test
    void testRemoveTraining_overReduction() {
        workload.addTraining(2025, 10, 40);
        workload.removeTraining(2025, 10, 100);
        assertEquals(0, workload.getMonthlyDuration(2025, 10));
        assertFalse(workload.getYearlyWorkload().containsKey(2025));
    }

    @Test
    void testRemoveTraining_monthNotPresent() {
        workload.addTraining(2025, 10, 60);
        workload.removeTraining(2025, 11, 30); // month not present
        assertEquals(60, workload.getMonthlyDuration(2025, 10));
    }

    @Test
    void testRemoveTraining_yearNotPresent() {
        workload.removeTraining(2024, 9, 30); // year not present
        assertTrue(workload.getYearlyWorkload().isEmpty());
    }

    @Test
    void testGetMonthlyDuration_missingMonth() {
        workload.addTraining(2025, 10, 60);
        assertEquals(0, workload.getMonthlyDuration(2025, 11));
    }

    @Test
    void testGetMonthlyDuration_missingYear() {
        assertEquals(0, workload.getMonthlyDuration(2024, 9));
    }

    @Test
    void testBuilderAndAccessors() {
        assertEquals("john.doe", workload.getUsername());
        assertEquals("John", workload.getFirstName());
        assertEquals("Doe", workload.getLastName());
        assertTrue(workload.isActive());
    }
}
