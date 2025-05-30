package com.epam.gym.service;

import com.epam.gym.dto.*;
import com.epam.gym.entity.*;
import com.epam.gym.exception.TraineeCreationException;
import com.epam.gym.mapper.TraineeMapper;
import com.epam.gym.mapper.TrainerMapper;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingTypeRepository;
import com.epam.gym.util.UsernamePasswordUtil;
import jakarta.annotation.Nullable;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TrainerService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UsernamePasswordUtil usernamePasswordUtil;
    private final TrainerMapper trainerMapper;
    private final TraineeMapper traineeMapper;

    @Autowired
    public TrainerService(
            TrainerRepository trainerRepository,
            TrainingTypeRepository trainingTypeRepository,
            UsernamePasswordUtil usernamePasswordUtil,
            TrainerMapper trainerMapper,
            TraineeMapper traineeMapper
    ) {
        this.trainerRepository = trainerRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.usernamePasswordUtil = usernamePasswordUtil;
        this.trainerMapper = trainerMapper;
        this.traineeMapper = traineeMapper;
    }

    @Transactional
    public TrainerRegistrationResponse createTrainer(
            TrainerRegistrationRequest request
    ) {

        String username = usernamePasswordUtil.generateUsername(
                request.firstName(),
                request.lastName()
        );
        String password = usernamePasswordUtil.generatePassword();

        User user = new User.Builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .username(username)
                .password(password)
                .isActive(true)
                .build();

        TrainingType type = trainingTypeRepository.findByType(TrainingTypeEnum.valueOf(request.specialization()))
                .orElseThrow(() -> {
                    log.error("Training Type not found with name: {}", request.specialization());
                    return new NoResultException("Type not found");
                });

        Trainer trainer = new Trainer.Builder()
                .trainingType(type)
                .user(user)
                .build();

        try {
            trainerRepository.save(trainer);
            log.info("Trainer {} created successfully with ID: {}", user.getUsername(), trainer.getId());
            return trainerMapper.toTrainerRegistrationResponse(trainer);
        } catch (Exception e) {
            log.error("Failed to save trainer: {}", e.getMessage(), e);
            throw new TraineeCreationException("Failed to create trainer", e);
        }
    }

    @Transactional
    public TrainerProfileResponse findTrainerByUsername(String username) {
        Trainer trainer = trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> {
                    log.error("Trainer not found with username: {}", username);
                    return new NoResultException("Trainer not found");
                });
        return trainerMapper.toTrainerProfileResponse(trainer);
    }

    @Transactional
    public TrainerProfileResponse updateTrainer(TrainerUpdateRequest request) {
        Trainer trainer = trainerRepository.findByUserUsername(request.username())
                .orElseThrow(() -> {
                    log.error("Trainer not found with username: {}", request.username());
                    return new NoResultException("Trainer not found");
                });

        boolean updated = false;

        Trainer.Builder trainerBuilder = trainer.toBuilder();

        User user = trainer.getUser();

        if (!request.firstName().equals(user.getFirstName()) ||
            !request.lastName().equals(user.getLastName()) ||
            request.isActive() != user.isActive()
        ) {
            updated = true;
        }

        User userUpdated = user.toBuilder()
                    .firstName(request.firstName())
                    .lastName(request.lastName())
                    .isActive(request.isActive())
                    .build();

            trainerBuilder.user(userUpdated);

        if (request.specialization() != null) {
            TrainingType type = trainingTypeRepository.findByType(TrainingTypeEnum.valueOf(request.specialization()))
                    .orElseThrow(() -> {
                        log.error("Training Type not found with name: {}", request.specialization());
                        return new NoResultException("Type not found");
                    });
            trainerBuilder.trainingType(type);
            updated = true;
        }

        if (updated) {
            Trainer updatedTrainer = trainerRepository.save(trainerBuilder.build());
            log.info("Trainee with username '{}' updated successfully!", request.username());
            return trainerMapper.toTrainerProfileResponse(updatedTrainer);
        } else {
            log.info("No updates applied for trainee with username '{}'.", request.username());
            return trainerMapper.toTrainerProfileResponse(trainer);
        }
    }

    @Transactional
    public List<TrainerDto> getAvailableTrainersForTrainee(String username) {
        List<Trainer> trainerList = trainerRepository.findActiveTrainersNotAssignedToTrainee(username);
        Set<Trainer> trainerSet = new HashSet<>(trainerList);
        return traineeMapper.mapTrainersToTrainerDtoList(trainerSet);
    }

    @Transactional
    public List<Trainer> getTrainersByUsernames(List<String> usernames) {
        return trainerRepository.findAllByUserUsernameIn(usernames);
    }

    @Transactional
    public void changeActiveStatus(String username, boolean isActive) {
        Trainer trainer = trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> {
                    log.error("Trainer not found with username: {}", username);
                    return new NoResultException("Trainer not found");
                });

        if (trainer.getUser().isActive() != isActive) {
            User updatedUser = trainer.getUser().toBuilder()
                    .isActive(isActive)
                    .build();
            Trainer updatedTrainer = trainer.toBuilder()
                    .user(updatedUser)
                    .build();
            trainerRepository.save(updatedTrainer);
        }
    }
}
