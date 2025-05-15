package com.epam.gym.service;

import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.exception.TraineeCreationException;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.util.UsernamePasswordUtil;
import jakarta.annotation.Nullable;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainerService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final TrainerRepository trainerRepository;
    private final UsernamePasswordUtil usernamePasswordUtil;

    @Autowired
    public TrainerService(TrainerRepository trainerRepository,
                          UsernamePasswordUtil usernamePasswordUtil) {
        this.trainerRepository = trainerRepository;
        this.usernamePasswordUtil = usernamePasswordUtil;
    }

    @Transactional
    public Trainer createTrainer(String firstName,
                                 String lastName,
                                 TrainingType trainingType) {

        String username = usernamePasswordUtil.generateUsername(firstName, lastName);
        String password = usernamePasswordUtil.generatePassword();

        User user = new User.Builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .password(password)
                .isActive(true)
                .build();

        Trainer trainer = new Trainer.Builder()
                .trainingType(trainingType)
                .user(user)
                .build();
        log.info("Creating trainer: {}", user.getUsername());
        try {
            trainerRepository.save(trainer);
            log.info("Trainer {} created successfully with ID: {}", user.getUsername(), trainer.getId());
            return trainer;
        } catch (Exception e) {
            log.error("Failed to save trainer: {}", e.getMessage(), e);
            throw new TraineeCreationException("Failed to create trainer", e);
        }

    }

    @Transactional
    public Trainer findTrainerByUsername(String username) {
        log.debug("Finding trainer by username: {}", username);
        return trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> {
                    log.error("Trainer not found with username: {}", username);
                    return new NoResultException("Trainee not found");
                });
    }

    @Transactional
    public void updateTrainer(String username,
                              String firstName,
                              String lastName,
                              @Nullable TrainingType specialization,
                              boolean isActive
    ) {
        Trainer trainer = findTrainerByUsername(username);
        Trainer.Builder trainerBuilder = trainer.toBuilder();

        User user = trainer.getUser();
        boolean updated = false;

        boolean userChanged = !firstName.equals(user.getFirstName()) ||
                !lastName.equals(user.getLastName()) ||
                (isActive != user.isActive());

        if (userChanged) {
            User userUpdated = user.toBuilder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .isActive(isActive)
                    .build();
            trainerBuilder.user(userUpdated);
            updated = true;
        }

        if (specialization != null &&
                !specialization.equals(trainer.getTrainingType().getType().toString())) {
            trainerBuilder.trainingType(specialization);
            updated = true;
        }

        if (updated) {
            trainerRepository.save(trainerBuilder.build());
            log.info("Trainer with username '{}' updated successfully!", username);
        } else {
            log.info("No updates applied for trainer with username '{}'.", username);
        }
    }

    @Transactional
    public List<Trainer> getAvailableTrainersForTrainee(String username) {
        return trainerRepository.findActiveTrainersNotAssignedToTrainee(username);
    }

    @Transactional
    public List<Trainer> getTrainersByUsernames(List<String> usernames) {
        return trainerRepository.findAllByUserUsernameIn(usernames);
    }

    @Transactional
    public void changeActiveStatus(String username, boolean isActive){
        Trainer trainer = findTrainerByUsername(username);

        if (trainer.getUser().isActive() != isActive) {
            User updatedUser = trainer.getUser().toBuilder().
                    isActive(isActive).
                    build();
            Trainer updatedTrainer = trainer.toBuilder().
                    user(updatedUser).
                    build();
            trainerRepository.save(updatedTrainer);
            log.info("Active status successfully updated for trainer with username '{}'.", username);
        } else {
            log.info("No update performed. Trainer '{}' is already in state isActive={}", username, isActive);
        }
    }

}