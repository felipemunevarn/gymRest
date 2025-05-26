package com.epam.gym.mapper;

import com.epam.gym.dto.*;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper component for converting Trainer entities to TrainerProfileResponse DTOs.
 */
@Component
public class TrainerMapper {

    public TrainerProfileResponse toTrainerProfileResponse(Trainer trainer) {
        if (trainer == null) {
            return null;
        }

        List<TraineeDto> traineesDto = mapTraineesToTraineeDtoList(trainer.getTrainees());

        return new TrainerProfileResponse(
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getTrainingType(),
                trainer.getUser().isActive(),
                traineesDto
        );
    }

    public TrainerRegistrationResponse toTrainerRegistrationResponse(Trainer trainer) {
        if (trainer == null) {
            return null;
        }

        return new TrainerRegistrationResponse(
                trainer.getUser().getUsername(),
                trainer.getUser().getPassword()
        );
    }

    private List<TraineeDto> mapTraineesToTraineeDtoList(Set<Trainee> trainees) {
        if (trainees == null) {
            return List.of();
        }
        return trainees.stream()
                .map(trainee -> new TraineeDto(
                        trainee.getUser().getUsername(),
                        trainee.getUser().getFirstName(),
                        trainee.getUser().getLastName()
                ))
                .collect(Collectors.toList());
    }

    // public Trainee toTraineeEntity(TraineeUpdateRequest request) { ... }
    // public Trainee toTraineeEntity(TraineeRegistrationRequest request) { ... }
}

