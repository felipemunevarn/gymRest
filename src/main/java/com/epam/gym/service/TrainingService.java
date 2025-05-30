package com.epam.gym.service;

import com.epam.gym.dto.*;
import com.epam.gym.entity.*;
import com.epam.gym.mapper.TrainingMapper;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingRepository;
import com.epam.gym.repository.TrainingTypeRepository;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TrainingMapper trainingMapper;

    @Autowired
    public TrainingService(
            TrainingRepository trainingRepository,
            TrainingTypeRepository trainingTypeRepository,
            TrainingMapper trainingMapper,
            TraineeRepository traineeRepository,
            TrainerRepository trainerRepository
    ) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.trainingRepository = trainingRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.trainingMapper = trainingMapper;
    }

    @Transactional
    public void createTraining(
            TrainingRegistrationRequest request
    ) {

        Trainee trainee = traineeRepository.findByUserUsername(request.traineeUsername())
                .orElseThrow(() -> {
                    log.error("Trainee not found with username: {}", request.traineeUsername());
                    return new NoResultException("Trainee not found");
                });

        Trainer trainer = trainerRepository.findByUserUsername(request.trainerUsername())
                .orElseThrow(() -> {
                    log.error("Trainee not found with username: {}", request.trainerUsername());
                    return new NoResultException("Trainee not found");
                });

        Training training = new Training.Builder()
                .trainee(trainee)
                .trainer(trainer)
                .name(request.name())
                .trainingType(trainer.getTrainingType())
                .date(request.date())
                .duration(request.duration())
                .build();

        try {
            trainingRepository.save(training);
            log.info("Training '{}' created successfully with ID: {}", request.name(), training.getId());
        } catch (Exception e) {
            log.error("Failed to save training: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public List<TraineeTrainingResponse> getTraineeTrainings(
            String username,
            TraineeTrainingRequest request
    ) {
        TrainingType specialization = null;
        if (request.specialization()!=null) {
            specialization = trainingTypeRepository
                    .findByType(TrainingTypeEnum.valueOf(request.specialization()))
                    .orElseThrow(() -> {
                        log.error("Training Type not found with name: {}", request.specialization());
                        return new NoResultException("Type not found");
                    });
        }
        List<Training> trainings = trainingRepository.findTraineeTrainingsByCriteria(
                username,
                request.from(),
                request.to(),
                request.trainerName(),
                specialization
        );
        return trainingMapper.mapTrainingsToTraineeDtoResponseList(trainings);
    }

    @Transactional
    public List<TrainerTrainingResponse> getTrainerTrainings(
            String username,
            TrainerTrainingRequest request
    ) {
        List<Training> trainings = trainingRepository.findTrainerTrainingsByCriteria(
                username,
                request.from(),
                request.to(),
                request.traineeName()
        );
        return trainingMapper.mapTrainingsToTrainerDtoResponseList(trainings);
    }

}
