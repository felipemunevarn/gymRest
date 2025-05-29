package com.epam.gym.service;

import com.epam.gym.dto.TrainingTypeResponse;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.TrainingTypeEnum;
import com.epam.gym.repository.TrainingTypeRepository;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceTest {

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @InjectMocks
    private TrainingTypeService trainingTypeService;

    @Test
    void findAllTrainingTypes_withMultipleTrainingTypes_shouldReturnAllTrainingTypeResponses() {
        // Given
        TrainingType fitnessType = new TrainingType(TrainingTypeEnum.FLEXIBILITY);

        TrainingType yogaType = new TrainingType(TrainingTypeEnum.CARDIO);

        TrainingType zumbaType = new TrainingType(TrainingTypeEnum.STRENGTH);

        List<TrainingType> trainingTypes = List.of(fitnessType, yogaType, zumbaType);
        when(trainingTypeRepository.findAll()).thenReturn(trainingTypes);

        // When
        List<TrainingTypeResponse> result = trainingTypeService.findAllTrainingTypes();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());

        TrainingTypeResponse fitnessResponse = result.get(0);
//        assertEquals(1L, fitnessResponse.trainingTypeId());
        assertEquals("FLEXIBILITY", fitnessResponse.trainingType());

        TrainingTypeResponse yogaResponse = result.get(1);
//        assertEquals(2L, yogaResponse.trainingTypeId());
        assertEquals("CARDIO", yogaResponse.trainingType());

        TrainingTypeResponse zumbaResponse = result.get(2);
//        assertEquals(3L, zumbaResponse.trainingTypeId());
        assertEquals("STRENGTH", zumbaResponse.trainingType());

        verify(trainingTypeRepository).findAll();
    }

    @Test
    void findAllTrainingTypes_withEmptyList_shouldReturnEmptyList() {
        // Given
        List<TrainingType> emptyList = List.of();
        when(trainingTypeRepository.findAll()).thenReturn(emptyList);

        // When
        List<TrainingTypeResponse> result = trainingTypeService.findAllTrainingTypes();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(trainingTypeRepository).findAll();
    }

    @Test
    void findAllTrainingTypes_withSingleTrainingType_shouldReturnSingleResponse() {
        // Given
        TrainingType pilatesType = new TrainingType(TrainingTypeEnum.FLEXIBILITY);

        List<TrainingType> trainingTypes = List.of(pilatesType);
        when(trainingTypeRepository.findAll()).thenReturn(trainingTypes);

        // When
        List<TrainingTypeResponse> result = trainingTypeService.findAllTrainingTypes();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        TrainingTypeResponse response = result.get(0);
        assertEquals("FLEXIBILITY", response.trainingType());

        verify(trainingTypeRepository).findAll();
    }

    @Test
    void findByType_withValidSpecialization_shouldReturnTrainingType() {
        // Given
        String specialization = "FLEXIBILITY";
        TrainingType expectedType = new TrainingType(TrainingTypeEnum.FLEXIBILITY);

        when(trainingTypeRepository.findByType(TrainingTypeEnum.FLEXIBILITY))
                .thenReturn(Optional.of(expectedType));

        // When
        TrainingType result = trainingTypeService.findByType(specialization);

        // Then
        assertNotNull(result);
//        assertEquals(1L, result.getId());
        assertEquals(TrainingTypeEnum.FLEXIBILITY, result.getType());
        verify(trainingTypeRepository).findByType(TrainingTypeEnum.FLEXIBILITY);
    }

    @Test
    void findByType_withNonExistentSpecialization_shouldThrowNoResultException() {
        // Given
        String specialization = "CARDIO";
        when(trainingTypeRepository.findByType(TrainingTypeEnum.CARDIO))
                .thenReturn(Optional.empty());

        // When & Then
        NoResultException exception = assertThrows(NoResultException.class,
                () -> trainingTypeService.findByType(specialization));

        assertEquals("TrainingType not found", exception.getMessage());
        verify(trainingTypeRepository).findByType(TrainingTypeEnum.CARDIO);
    }

    @Test
    void findByType_withInvalidSpecialization_shouldThrowIllegalArgumentException() {
        // Given
        String invalidSpecialization = "INVALID_TYPE";

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainingTypeService.findByType(invalidSpecialization));

        assertTrue(exception.getMessage().contains("No enum constant"));
        verify(trainingTypeRepository, never()).findByType(any());
    }

    @Test
    void findByType_withNullSpecialization_shouldThrowNullPointerException() {
        // Given
        String nullSpecialization = null;

        // When & Then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> trainingTypeService.findByType(nullSpecialization));

        verify(trainingTypeRepository, never()).findByType(any());
    }

    @Test
    void findByType_withEmptySpecialization_shouldThrowIllegalArgumentException() {
        // Given
        String emptySpecialization = "";

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainingTypeService.findByType(emptySpecialization));

        assertTrue(exception.getMessage().contains("No enum constant"));
        verify(trainingTypeRepository, never()).findByType(any());
    }

    @Test
    void findByType_withLowerCaseSpecialization_shouldThrowIllegalArgumentException() {
        // Given
        String lowerCaseSpecialization = "fitness";

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainingTypeService.findByType(lowerCaseSpecialization));

        assertTrue(exception.getMessage().contains("No enum constant"));
        verify(trainingTypeRepository, never()).findByType(any());
    }

    @Test
    void findByType_withMixedCaseSpecialization_shouldThrowIllegalArgumentException() {
        // Given
        String mixedCaseSpecialization = "Fitness";

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainingTypeService.findByType(mixedCaseSpecialization));

        assertTrue(exception.getMessage().contains("No enum constant"));
        verify(trainingTypeRepository, never()).findByType(any());
    }

    @Test
    void findByType_withAllValidEnumValues_shouldReturnCorrectTrainingTypes() {
        // Test each valid enum value
        TrainingTypeEnum[] allTypes = TrainingTypeEnum.values();

        for (TrainingTypeEnum enumType : allTypes) {
            // Given
            TrainingType trainingType = new TrainingType(enumType);

            when(trainingTypeRepository.findByType(enumType))
                    .thenReturn(Optional.of(trainingType));

            // When
            TrainingType result = trainingTypeService.findByType(enumType.name());

            // Then
            assertNotNull(result);
            assertEquals(enumType, result.getType());

            // Reset mock for next iteration
            reset(trainingTypeRepository);
        }
    }

    @Test
    void findByType_withWhitespaceSpecialization_shouldThrowIllegalArgumentException() {
        // Given
        String whitespaceSpecialization = "   ";

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainingTypeService.findByType(whitespaceSpecialization));

        assertTrue(exception.getMessage().contains("No enum constant"));
        verify(trainingTypeRepository, never()).findByType(any());
    }

    @Test
    void findByType_withSpecialCharacters_shouldThrowIllegalArgumentException() {
        // Given
        String specialCharSpecialization = "FITNESS!@#";

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainingTypeService.findByType(specialCharSpecialization));

        assertTrue(exception.getMessage().contains("No enum constant"));
        verify(trainingTypeRepository, never()).findByType(any());
    }
}
