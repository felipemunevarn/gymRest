package com.epam.gym.service;

import com.epam.gym.dto.TraineeTrainingRequest;
import com.epam.gym.dto.TraineeTrainingResponse;
import com.epam.gym.entity.*;
import com.epam.gym.mapper.TrainingMapper;
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

    private final TrainingRepository trainingRepository;
    private final TrainingMapper trainingMapper;

    @Autowired
    public TrainingService(
            TrainingRepository trainingRepository,
            TrainingMapper trainingMapper
    ) {
        this.trainingRepository = trainingRepository;
        this.trainingMapper = trainingMapper;
    }

    @Transactional
    public void createTraining(Trainee trainee,
                               Trainer trainer,
                               String name,
                               TrainingType trainingType,
                               LocalDate date,
                               int duration) {

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

    @Transactional
    public List<TraineeTrainingResponse> getTraineeTrainings(
            String username,
            TraineeTrainingRequest request
//            LocalDate from,
//            LocalDate to,
//            String trainerName,
//            TrainingType type
    ) {
        TrainingType specialization = new TrainingType(TrainingTypeEnum.valueOf(request.specialization()));
        List<Training> trainings = trainingRepository.findTraineeTrainingsByCriteria(
                username,
                request.from(),
                request.to(),
                request.trainerName(),
                specialization
        );
        return trainingMapper.mapTrainingsToDtoResponseList(trainings);
    }

    @Transactional
    public List<Training> getTrainerTrainings(String username,
                                              LocalDate from,
                                              LocalDate to,
                                              String traineeName) {
        return trainingRepository.findTrainerTrainingsByCriteria(username,
                from,
                to,
                traineeName);
    }

}
