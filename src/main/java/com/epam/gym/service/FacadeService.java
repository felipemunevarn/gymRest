package com.epam.gym.service;

import com.epam.gym.dto.*;
import com.epam.gym.entity.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FacadeService {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;

    @Autowired
    public FacadeService(TraineeService traineeService,
                         TrainerService trainerService,
                         TrainingService trainingService,
                         TrainingTypeService trainingTypeService
    ){
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
    }

    ////////////////////////////////////////////////
    //////////// TRAINEE ///////////////////////////
    ////////////////////////////////////////////////

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

    @Transactional
    public void deleteTrainee(String username) {
        traineeService.deleteTrainee(username);
    }

    ////////////////////////////////////////////////
    //////////// TRAINER ///////////////////////////
    ////////////////////////////////////////////////

    @Transactional
    public TrainerRegistrationResponse registerTrainer(TrainerRegistrationRequest request) {
//        Optional<TrainingType> optTrainingType = trainingTypeRepository.findByType(TrainingTypeEnum.valueOf(request.specialization()));
//        Trainer trainer = trainerService.createTrainer(request.firstName(),
//                request.lastName(),
//                request.address());
//        return new TrainerRegistrationResponse(trainer.getUser().getUsername(),
//                trainer.getUser().getPassword());
        return null;
    }

    ////////////////////////////////////////////////
    //////////// TRAINING //////////////////////////
    ////////////////////////////////////////////////

    ////////////////////////////////////////////////
    //////////// TRAINING TYPE /////////////////////
    ////////////////////////////////////////////////

    @Transactional
    public List<TrainingTypeResponse> findAllTrainingTypes() {
        List<TrainingType> trainingTypes = trainingTypeService.findAllTrainingTypes();
        return trainingTypes.stream().
                map(trainingType -> new TrainingTypeResponse(trainingType.getId(),
                        trainingType.getType().toString())).
                toList();
    }

}
