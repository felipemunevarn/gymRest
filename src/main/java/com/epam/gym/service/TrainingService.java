package com.epam.gym.service;

import com.epam.gym.entity.*;
import com.epam.gym.repository.TrainingRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

//    private final TraineeRepository traineeRepository;
//    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
//    private final UserService userService;

    @Autowired
    public TrainingService(/* TraineeRepository traineeRepository,
                           TrainerRepository trainerRepository, */
                           TrainingRepository trainingRepository /*,
                           UserService userService */) {
//        this.traineeRepository = traineeRepository;
//        this.trainerRepository = trainerRepository;
        this.trainingRepository = trainingRepository;
//        this.userService = userService;
    }

    @Transactional
    public void createTraining(/* String username,
                               String password,
                               String traineeUsername,
                               String trainerUsername, */
                               Trainee trainee,
                               Trainer trainer,
                               String name,
                               TrainingType trainingType,
                               LocalDate date,
                               int duration) {
//        if (!authenticate(username, password)) {
//            log.warn("Authentication failed for {}", username);
//            return;
//        }
//        log.info("Authentication successful for {}", username);

//        Optional<Trainee> optTrainee = traineeRepository.findByUserUsername(traineeUsername);
//        Optional<Trainer> optTrainer = trainerRepository.findByUserUsername(trainerUsername);

//        if (optTrainee.isEmpty() || optTrainer.isEmpty()) {
//            log.error("Trainee or Trainer not found for provided usernames.");
//            throw new NoResultException("Trainee or Trainer not found");
//        }

        Training training = new Training.Builder()
                .trainee(trainee)
                .trainer(trainer)
                .name(name)
                .trainingType(trainingType)
                .date(date)
                .duration(duration)
                .build();

        try {
            trainingRepository.save(training);
            log.info("Training '{}' created successfully with ID: {}", name, training.getId());
        } catch (Exception e) {
            log.error("Failed to save training: {}", e.getMessage(), e);
        }
    }

//    @Transactional
//    public boolean authenticate(String username, String password) {
//        return userService.authenticate(username, password);
//    }

    @Transactional
    public List<Training> getTraineeTrainings(String username,
                                              LocalDate from,
                                              LocalDate to,
                                              String trainerName,
                                              TrainingTypeEnum type) {
        return trainingRepository.findTraineeTrainingsByCriteria(username, from, to, trainerName, type);
    }

    @Transactional
    public List<Training> getTrainerTrainings(String username,
                                              LocalDate from,
                                              LocalDate to,
                                              String traineeName) {
        return trainingRepository.findTrainerTrainingsByCriteria(username, from, to, traineeName);
    }

    @Transactional
    public void deleteTrainings(List<Training> trainings) {
        for (Training t : trainings) {
            trainingRepository.delete(t);
        }
    }

    @Transactional
    public List<Trainer> getTrainersByTraineeUsername(String username) {
        return trainingRepository.findTrainersByTraineeUsername(username);
    }

    @Transactional
    public List<Trainee> getTraineesByTrainerUsername(String username) {
        return trainingRepository.findTraineesByTrainerUsername(username);
    }

}
