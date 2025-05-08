package com.epam.gym.service;

import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.repository.TrainerRepository;
import jakarta.annotation.Nullable;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TrainerService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final TrainerRepository trainerRepository;
//    private final TrainingTypeRepository trainingTypeRepository;
//    private final UserService userService;
//    private final UsernamePasswordUtil usernamePasswordUtil;

    @Autowired
    public TrainerService(TrainerRepository trainerRepository /*,
                          TrainingTypeRepository trainingTypeRepository,
                          UserService userService,
                          UsernamePasswordUtil usernamePasswordUtil */) {
        this.trainerRepository = trainerRepository;
//        this.trainingTypeRepository = trainingTypeRepository;
//        this.userService = userService;
//        this.usernamePasswordUtil = usernamePasswordUtil;
    }

    @Transactional
    public void createTrainer(/* String firstName, String lastName, */ TrainingType trainingType,
                                                                           User user) {

//        String newUsername = usernamePasswordUtil.generateUsername(firstName, lastName);
//        String newPassword = usernamePasswordUtil.generatePassword();
//        User user = new User.Builder()
//                .firstName(firstName)
//                .lastName(lastName)
//                .username(newUsername)
//                .password(newPassword)
//                .isActive(true)
//                .build();

        Trainer trainer = new Trainer.Builder()
                .trainingType(trainingType)
                .user(user)
                .build();

        log.info("Creating trainer: {}", user.getUsername());
        try {
            trainerRepository.save(trainer);
            log.debug("Trainer saved with ID: {}", trainer.getId());
        } catch (Exception e) {
            log.error("Failed to save trainer: {}", e.getMessage(), e);
        }
    }

//    @Transactional
//    public boolean authenticate(String username, String password) {
//        return userService.authenticate(username, password);
//    }

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
