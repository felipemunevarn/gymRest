package com.epam.gym.service;

import com.epam.gym.dto.*;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.User;
import com.epam.gym.util.UsernamePasswordUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TraineeManagementService {

    private final TraineeService traineeService;
    private final UserService userService;
    private final TrainingService trainingService;
    private final UsernamePasswordUtil usernamePasswordUtil;

    @Autowired
    public TraineeManagementService(TraineeService traineeService,
                                    UserService userService,
                                    TrainingService trainingService,
                                    UsernamePasswordUtil usernamePasswordUtil) {
        this.traineeService = traineeService;
        this.userService = userService;
        this.trainingService = trainingService;
        this.usernamePasswordUtil = usernamePasswordUtil;
    }

    @Transactional
    public TraineeRegistrationResponse registerTrainee(TraineeRegistrationRequest request) {
        User user = userService.createUser(request.firstName(), request.lastName());
//        traineeService.createTrainee(request.dateOfBirth(), request.address(), user);
        return new TraineeRegistrationResponse(user.getUsername(), user.getPassword());
    }

    @Transactional
    public TraineeProfileResponse getTraineeByUsername(String username) {
        Trainee trainee = traineeService.findTraineeByUsername(username);
        List<Trainer> trainers = trainingService.getTrainersByTraineeUsername(username);
        List<TrainerDto> trainersDto = trainers.stream().
                map(trainer -> new TrainerDto(trainer.getUser().getUsername(),
                        trainer.getUser().getFirstName(),
                        trainer.getUser().getLastName(),
                        trainer.getTrainingType().getType().toString())).
                toList();
        return new TraineeProfileResponse(trainee.getUser().getFirstName(),
                trainee.getUser().getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.getUser().isActive(),
                trainersDto);
    }

    @Transactional
    public void deleteTraineeCompletely(String username) {
        List<Training> trainings = trainingService.getTraineeTrainings(username,
                null,
                null,
                null,
                null);
        trainingService.deleteTrainings(trainings);
        traineeService.deleteTrainee(username);
    }

    @Transactional
    public TraineeProfileResponse updateTrainee(TraineeUpdateRequest request) {
//        traineeService.updateTrainee(request.username(), request.dateOfBirth(), request.address());
        userService.updateUser(request.username(),
                request.firstName(),
                request.lastName(),
                request.isActive());
        return getTraineeByUsername(request.username());
    }
}
