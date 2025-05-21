package com.epam.gym.mapper;

import com.epam.gym.dto.TraineeProfileResponse;
import com.epam.gym.dto.TraineeRegistrationResponse;
import com.epam.gym.dto.TrainerDto;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper component for converting Trainee entities to TraineeProfileResponse DTOs.
 */
@Component
public class TraineeMapper {

    public TraineeProfileResponse toTraineeProfileResponse(Trainee trainee) {
        if (trainee == null) {
            return null;
        }

        List<TrainerDto> trainersDto = mapTrainersToTrainerDtoList(trainee.getTrainers());

        return new TraineeProfileResponse(
                trainee.getUser().getFirstName(),
                trainee.getUser().getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.getUser().isActive(),
                trainersDto
        );
    }

    public TraineeRegistrationResponse toTraineeRegistrationResponse(Trainee trainee) {
        if (trainee == null) {
            return null;
        }

        return new TraineeRegistrationResponse(
                trainee.getUser().getUsername(),
                trainee.getUser().getPassword()
        );
    }

    private List<TrainerDto> mapTrainersToTrainerDtoList(Set<Trainer> trainers) {
        if (trainers == null) {
            return List.of();
        }
        return trainers.stream()
                .map(trainer -> new TrainerDto(
                        trainer.getUser().getUsername(),
                        trainer.getUser().getFirstName(),
                        trainer.getUser().getLastName(),
                        trainer.getTrainingType().getType().toString()
                ))
                .collect(Collectors.toList());
    }

    // public Trainee toTraineeEntity(TraineeUpdateRequest request) { ... }
    // public Trainee toTraineeEntity(TraineeRegistrationRequest request) { ... }
}

