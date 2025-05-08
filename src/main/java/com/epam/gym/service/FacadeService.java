package com.epam.gym.service;

import com.epam.gym.dto.*;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.epam.gym.util.UsernamePasswordUtil;

import java.util.List;
import java.util.Optional;

@Service
public class FacadeService {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
//    private final UserService userService;

    @Autowired
    public FacadeService(TraineeService traineeService,
                         TrainerService trainerService,
                         TrainingService trainingService
//                         , UserService userService
    ){
        this.traineeService = traineeService;
        this.trainerService = trainerService;
//        this.userService = userService;
        this.trainingService = trainingService;
    }

    @Transactional
    public TraineeRegistrationResponse registerTrainee(TraineeRegistrationRequest request) {
        Trainee trainee = traineeService.createTrainee(request.firstName(),
                request.lastName(),
                request.dateOfBirth(),
                request.address());
        return new TraineeRegistrationResponse(trainee.getUser().getUsername(),
                trainee.getUser().getPassword());
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
    public TraineeProfileResponse updateTrainee(TraineeUpdateRequest request) {
        traineeService.updateTrainee(request.username(),
                request.firstName(),
                request.lastName(),
                request.dateOfBirth(),
                request.address(),
                request.isActive()
                );
        return getTraineeByUsername(request.username());
    }

//    @Transactional
//    public void deleteTraineeCompletely(String username) {
//        List<Training> trainings = trainingService.getTraineeTrainings(username,
//                null,
//                null,
//                null,
//                null);
//        trainingService.deleteTrainings(trainings);
//        traineeService.deleteTrainee(username);
//    }

    @Transactional
    public void deleteTrainee(String username) {
//        List<Training> trainings = trainingService.getTraineeTrainings(username,
//                null,
//                null,
//                null,
//                null);
//        trainingService.deleteTrainings(trainings);
        traineeService.deleteTrainee(username);
    }
}
