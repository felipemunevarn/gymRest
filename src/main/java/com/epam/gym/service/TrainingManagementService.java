package com.epam.gym.service;

import com.epam.gym.entity.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TrainingManagementService {

    private final TrainingService trainingService;
    private final TraineeService traineeService;
    private final TrainerService trainerService;

    public TrainingManagementService(TrainingService trainingService,
                                     TraineeService traineeService,
                                     TrainerService trainerService) {
        this.trainingService = trainingService;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    public void createTraining(String traineeUsername,
                               String trainerUsername,
                               String name,
                               TrainingType trainingType,
                               LocalDate date,
                               int duration) {
        Trainee trainee = traineeService.findTraineeByUsername(traineeUsername);
        Optional<Trainer> optTrainer = trainerService.findByUsername(trainerUsername);
        trainingService.createTraining(trainee, optTrainer.get(), name, trainingType, date, duration);
    }

    public List<Training> getTraineeTrainings(String username,
                                              LocalDate from,
                                              LocalDate to,
                                              String trainerName,
                                              TrainingTypeEnum type) {
        return trainingService.getTraineeTrainings(username, from, to, trainerName, type);
    }

    public List<Training> getTrainerTrainings(String username,
                                              LocalDate from,
                                              LocalDate to,
                                              String traineeName) {
        return trainingService.getTrainerTrainings(username, from, to, traineeName);
    }

    public void deleteTrainings(List<Training> trainings) {
        trainingService.deleteTrainings(trainings);
    }

}
