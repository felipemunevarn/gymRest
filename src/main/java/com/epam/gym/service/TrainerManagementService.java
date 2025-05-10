package com.epam.gym.service;

import com.epam.gym.dto.*;
import com.epam.gym.entity.*;
import com.epam.gym.repository.TrainingTypeRepository;
import com.epam.gym.util.UsernamePasswordUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainerManagementService {

    private final TrainerService trainerService;
    private final UserService userService;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TrainingService trainingService;
    private final UsernamePasswordUtil usernamePasswordUtil;

    @Autowired
    public TrainerManagementService(TrainerService trainerService,
                                    UserService userService,
                                    TrainingTypeRepository trainingTypeRepository,
                                    TrainingService trainingService,
                                    UsernamePasswordUtil usernamePasswordUtil) {
        this.trainerService = trainerService;
        this.userService = userService;
        this.trainingTypeRepository = trainingTypeRepository;
        this.trainingService = trainingService;
        this.usernamePasswordUtil = usernamePasswordUtil;
    }

    @Transactional
    public TrainerRegistrationResponse registerTrainer(TrainerRegistrationRequest request) {
        User user = userService.createUser(request.firstName(), request.lastName());
        Optional<TrainingType> optTrainingType = trainingTypeRepository.findByType(TrainingTypeEnum.valueOf(request.specialization()));
//        TrainingType tt = new TrainingType(TrainingTypeEnum.valueOf(request.specialization()));
//        trainerService.createTrainer(optTrainingType.get(), user);
        return new TrainerRegistrationResponse(user.getUsername(), user.getPassword());
    }

    @Transactional
    public TrainerProfileResponse getTrainerByUsername(String username) {
        Optional<Trainer> trainer = trainerService.findByUsername(username);
        List<Trainee> trainees = trainingService.getTraineesByTrainerUsername(username);
        List<TraineeDto> traineesDto = trainees.stream().
                map(trainee -> new TraineeDto(trainee.getUser().getUsername(),
                        trainee.getUser().getFirstName(),
                        trainee.getUser().getLastName())).
                toList();
        return new TrainerProfileResponse(trainer.get().getUser().getFirstName(),
                trainer.get().getUser().getLastName(),
                trainer.get().getTrainingType(),
                trainer.get().getUser().isActive(),
                traineesDto);
    }

    @Transactional
    public TrainerProfileResponse updateTrainer(TrainerUpdateRequest request) {
        Optional<TrainingType> optTrainingType = trainingTypeRepository.findByType(TrainingTypeEnum.valueOf(request.specialization()));
        trainerService.updateTrainer(request.username(), optTrainingType.get());
        userService.updateUser(request.username(),
                request.firstName(),
                request.lastName(),
                request.isActive());
        return getTrainerByUsername(request.username());
    }
}
