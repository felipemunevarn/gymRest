package com.epam.gym.service;

import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.User;
import com.epam.gym.exception.TraineeCreationException;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.util.UsernamePasswordUtil;
import jakarta.annotation.Nullable;
import jakarta.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class TraineeService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final TraineeRepository traineeRepository;
    private final UsernamePasswordUtil usernamePasswordUtil;

    @Autowired
    public TraineeService(TraineeRepository traineeRepository,
                          UsernamePasswordUtil usernamePasswordUtil) {
        this.traineeRepository = traineeRepository;
        this.usernamePasswordUtil = usernamePasswordUtil;
    }

    @Transactional
    public Trainee createTrainee(String firstName,
                                 String lastName,
                                 LocalDate dateOfBirth,
                                 String address) {

        String username = usernamePasswordUtil.generateUsername(firstName, lastName);
        String password = usernamePasswordUtil.generatePassword();

        User user = new User.Builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .password(password)
                .isActive(true)
                .build();

        Trainee trainee = new Trainee.Builder()
                .dateOfBirth(dateOfBirth)
                .address(address)
                .user(user)
                .build();
        log.info("Creating trainee: {}", user.getUsername());
        try {
            traineeRepository.save(trainee);
            log.info("Trainee {} created successfully with ID: {}", user.getUsername(), trainee.getId());
            return trainee;
        } catch (Exception e) {
            log.error("Failed to save trainee: {}", e.getMessage(), e);
            throw new TraineeCreationException("Failed to create trainee", e);
        }
    }

    @Transactional(readOnly = true)
    public Trainee findTraineeByUsername(String username) {
        log.debug("Finding trainee by username: {}", username);
        return traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> {
                    log.error("Trainee not found with username: {}", username);
                    return new NoResultException("Trainee not found");
                });
    }

    @Transactional
    public void updateTrainee(String username,
                              String firstName,
                              String lastName,
                              @Nullable LocalDate dateOfBirth,
                              @Nullable String address,
                              boolean isActive
    ) {
        Trainee trainee = findTraineeByUsername(username);

        boolean updated = false;

        Trainee.Builder traineeBuilder = trainee.toBuilder();

        User user = trainee.getUser();

        if (!firstName.equals(user.getFirstName()) ||
            !lastName.equals(user.getLastName()) ||
            (isActive != user.isActive())
        ) {
            updated = true;
        }

        User userUpdated = user.toBuilder()
                .firstName(firstName)
                .lastName(lastName)
                .isActive(isActive)
                .build();

        traineeBuilder.user(userUpdated);

        if (dateOfBirth != null) {
            traineeBuilder.dateOfBirth(dateOfBirth);
            updated = true;
        }
        if (address != null) {
            traineeBuilder.address(address);
            updated = true;
        }

        if (updated) {
            traineeRepository.save(traineeBuilder.build());
            log.info("Trainee with username '{}' updated successfully!", username);
        } else {
            log.info("No updates applied for trainee with username '{}'.", username);
        }
    }

    @Transactional
    public void deleteTrainee(String username) {
        Trainee trainee = findTraineeByUsername(username);
        traineeRepository.delete(trainee);
        log.info("Deleted trainee {}", username);
    }
}
