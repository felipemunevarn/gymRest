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
import java.util.Optional;

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
    public Optional<Trainer> findByUsername(String username) {
        return trainerRepository.findByUserUsername(username);
    }

    @Transactional
    public Optional<Trainer> getAuthenticatedTrainer(String username, String password) {
//        if (!authenticate(username, password)) {
//            log.warn("Authentication failed for {}", username);
//            return Optional.empty();
//        }
//        log.info("Authentication successful for {}", username);
        return findByUsername(username);
    }

//    @Transactional
//    public void changeTrainerPassword(String username, String oldPassword, String newPassword) {
//        Optional<Trainer> optTrainer = findByUsername(username);
//        if (optTrainer.isEmpty()) {
//            log.error("User with username: {} is not a trainer", username);
//            throw new NoResultException("Trainer not found: " + username);
//        } else if (!authenticate(username, oldPassword)) {
//            log.warn("Authentication failed for {}", username);
//            throw new SecurityException("Authentication failed");
//        } else {
//            userService.changePassword(username, oldPassword, newPassword);
//        }
//    }

    @Transactional
    public void updateTrainer(String username,
                              @Nullable TrainingType specialization
    ) {
        Optional<Trainer> optTrainer = Optional.ofNullable(findByUsername(username)
                .orElseThrow(() -> new NoResultException("Trainee not found with username: " + username)));

        Trainer.Builder trainer = optTrainer.get().toBuilder();

        trainerRepository.save(trainer.build());
        log.info("Trainee with username: {} updated succesfully!", username);
    }

//    @Transactional
//    public void changeActiveStatus(String username, String password, boolean isActive) {
//        Optional<Trainer> optTrainer = findByUsername(username);
//        if (optTrainer.isEmpty()) {
//            log.error("User with username: {} is not a trainer", username);
//            throw new NoResultException("Trainer not found: " + username);
//        } else if (!authenticate(username, password)) {
//            log.warn("Authentication failed for {}", username);
//            throw new SecurityException("Authentication failed");
//        }
//        userService.setActiveStatus(username, isActive);
//    }

    @Transactional
    public List<Trainer> getUnassignedTrainersForTrainee(String username) {
        List<Trainer> trainers = trainerRepository.findTrainersNotAssignedToTrainee(username);
        return trainers;
    }

}
