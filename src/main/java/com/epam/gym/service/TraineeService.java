package com.epam.gym.service;

import com.epam.gym.dto.*;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.User;
import com.epam.gym.exception.TraineeCreationException;
import com.epam.gym.mapper.TraineeMapper;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.util.UsernamePasswordUtil;
import jakarta.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UsernamePasswordUtil usernamePasswordUtil;
    private final TraineeMapper traineeMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public TraineeService(
            TraineeRepository traineeRepository,
            TrainerRepository trainerRepository,
            UsernamePasswordUtil usernamePasswordUtil,
            TraineeMapper traineeMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.usernamePasswordUtil = usernamePasswordUtil;
        this.traineeMapper = traineeMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public TraineeRegistrationResponse createTrainee(
            TraineeRegistrationRequest request
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
                .password(passwordEncoder.encode(password))
                .isActive(true)
                .role(User.Role.TRAINEE)
                .build();

        Trainee trainee = new Trainee.Builder()
                .dateOfBirth(request.dateOfBirth())
                .address(request.address())
                .user(user)
                .build();
        log.info("Creating trainee: {}", user.getUsername());
        try {
            traineeRepository.save(trainee);
            log.info("Trainee {} created successfully with ID: {}", user.getUsername(), trainee.getId());
            return  traineeMapper.toTraineeRegistrationResponse(trainee);
        } catch (Exception e) {
            log.error("Failed to save trainee: {}", e.getMessage(), e);
            throw new TraineeCreationException("Failed to create trainee", e);
        }
    }

    @Transactional(readOnly = true)
    public TraineeProfileResponse findTraineeByUsername(String username) {
        log.debug("Finding trainee by username: {}", username);
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> {
                    log.error("Trainee not found with username: {}", username);
                    return new NoResultException("Trainee not found");
                });
        return traineeMapper.toTraineeProfileResponse(trainee);
    }

    @Transactional
    public TraineeProfileResponse updateTrainee(TraineeUpdateRequest request) {
        Trainee trainee = traineeRepository.findByUserUsername(request.username())
                .orElseThrow(() -> {
                    log.error("Trainee not found with username: {}", request.username());
                    return new NoResultException("Trainee not found");
                });

        boolean updated = false;

        Trainee.Builder traineeBuilder = trainee.toBuilder();

        User user = trainee.getUser();

        if (!request.firstName().equals(user.getFirstName()) ||
            !request.lastName().equals(user.getLastName()) ||
            (request.isActive() != user.isActive())
        ) {
            updated = true;
        }

        User userUpdated = user.toBuilder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .isActive(request.isActive())
                .build();

        traineeBuilder.user(userUpdated);

        if (request.dateOfBirth() != null) {
            traineeBuilder.dateOfBirth(request.dateOfBirth());
            updated = true;
        }
        if (request.address() != null) {
            traineeBuilder.address(request.address());
            updated = true;
        }

        if (updated) {
            Trainee updatedTrainee = traineeRepository.save(traineeBuilder.build());
            log.info("Trainee with username '{}' updated successfully!", request.username());
            return traineeMapper.toTraineeProfileResponse(updatedTrainee);
        } else {
            log.info("No updates applied for trainee with username '{}'.", request.username());
            return traineeMapper.toTraineeProfileResponse(trainee);
        }
    }

    @Transactional
    public void deleteTrainee(String username) {
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> {
                    log.error("Trainee not found with username: {}", username);
                    return new NoResultException("Trainee not found");
                });
        traineeRepository.delete(trainee);
        log.info("Deleted trainee {}", username);
    }

    @Transactional
//    public Trainee updateTraineeTrainers(String traineeUsername, List<Trainer> trainers) {
    public TraineeTrainerResponse updateTraineeTrainers(
            String traineeUsername,
            UpdateTraineeTrainersRequest request
    ) {
        Trainee trainee = traineeRepository.findByUserUsername(traineeUsername)
                .orElseThrow(() -> {
                    log.error("Trainee not found with username: {}", traineeUsername);
                    return new NoResultException("Trainee not found");
                });

        Set<Trainer> trainers = request.trainerUsernames().stream()
                .map(trainerUsername -> {
                    return trainerRepository.findByUserUsername(traineeUsername)
                            .orElseThrow(() -> {
                                log.error("Trainee not found with username: {}", trainerUsername);
                                return new NoResultException("Trainee not found");
                            });
                })
                .collect(Collectors.toSet());

        Trainee updatedTrainee = trainee.toBuilder()
                .trainers(trainers).build();

        Trainee savedTrainee = traineeRepository.save(updatedTrainee);

        log.info("Updated trainers for trainee with username '{}'.", traineeUsername);
        return new TraineeTrainerResponse(traineeMapper.mapTrainersToTrainerDtoList(savedTrainee.getTrainers()));
//        return savedTrainee;
    }

    @Transactional
    public void changeActiveStatus(String username, boolean isActive){
        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> {
                    log.error("Trainee not found with username: {}", username);
                    return new NoResultException("Trainee not found");
                });

        if (trainee.getUser().isActive() != isActive) {
            User updatedUser = trainee.getUser().toBuilder().
                    isActive(isActive).
                    build();
            Trainee updatedTrainee = trainee.toBuilder().
                    user(updatedUser).
                    build();
            traineeRepository.save(updatedTrainee);
            log.info("Active status successfully updated for trainee with username '{}'.", username);
        } else {
            log.info("No update performed. Trainee '{}' is already in state isActive={}", username, isActive);
        }
    }
}
