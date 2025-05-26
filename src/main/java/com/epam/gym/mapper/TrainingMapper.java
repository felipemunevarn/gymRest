package com.epam.gym.mapper;

import com.epam.gym.dto.TraineeTrainingResponse;
import com.epam.gym.dto.TrainerTrainingResponse;
import com.epam.gym.entity.Training;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TrainingMapper {

    public List<TraineeTrainingResponse> mapTrainingsToTraineeDtoResponseList(List<Training> trainings) {
        if (trainings == null) {
            return List.of();
        }
        return trainings.stream()
                .map(training -> new TraineeTrainingResponse(
                        training.getName(),
                        training.getDate().toString(),
                        training.getTrainingType().getType().toString(),
                        training.getDuration(),
                        training.getTrainer().getUser().getFirstName()
                ))
                .collect(Collectors.toList());
    }

    public List<TrainerTrainingResponse> mapTrainingsToTrainerDtoResponseList(List<Training> trainings) {
        if (trainings == null) {
            return List.of();
        }
        return trainings.stream()
                .map(training -> new TrainerTrainingResponse(
                        training.getName(),
                        training.getDate().toString(),
                        training.getTrainingType().getType().toString(),
                        training.getDuration(),
                        training.getTrainee().getUser().getFirstName()
                ))
                .collect(Collectors.toList());
    }

}
