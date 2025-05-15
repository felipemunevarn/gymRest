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
        List<TrainerDto> trainersDto = trainee.getTrainers().stream().
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

    @Transactional
    public TraineeTrainerResponse updateTraineeTrainers(String username,
                                                        UpdateTraineeTrainersRequest request){
//        List<Trainer> trainers = request.trainerUsernames().stream().
//                map(trainerService::findTrainerByUsername).toList();
        List<Trainer> trainers = trainerService.getTrainersByUsernames(request.trainerUsernames());

        traineeService.updateTraineeTrainers(username, trainers);

        List<TrainerDto> trainersDto = trainers.stream().
                map(trainer -> new TrainerDto(trainer.getUser().getUsername(),
                        trainer.getUser().getFirstName(),
                        trainer.getUser().getLastName(),
                        trainer.getTrainingType().getType().toString()
                        )).toList();
        return new TraineeTrainerResponse(trainersDto);
    }

    ////////////////////////////////////////////////
    //////////// TRAINER ///////////////////////////
    ////////////////////////////////////////////////

    @Transactional
    public TrainerRegistrationResponse registerTrainer(TrainerRegistrationRequest request) {
        TrainingType trainingType = trainingTypeService.findByType(request.specialization());
        Trainer trainer = trainerService.createTrainer(request.firstName(),
                request.lastName(),
                trainingType);
        return new TrainerRegistrationResponse(trainer.getUser().getUsername(),
                trainer.getUser().getPassword());
    }

    @Transactional
    public TrainerProfileResponse getTrainerByUsername(String username) {
        Trainer trainer = trainerService.findTrainerByUsername(username);
        List<TraineeDto> traineesDto = trainer.getTrainees().stream().
                map(trainee -> new TraineeDto(trainee.getUser().getUsername(),
                        trainee.getUser().getFirstName(),
                        trainee.getUser().getLastName()
                )).toList();
        return new TrainerProfileResponse(trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getTrainingType(),
                trainer.getUser().isActive(),
                traineesDto);
    }

    @Transactional
    public TrainerProfileResponse updateTrainer(TrainerUpdateRequest request) {
        TrainingType trainingType = trainingTypeService.findByType(request.specialization());
        trainerService.updateTrainer(request.username(),
                request.firstName(),
                request.lastName(),
                trainingType,
                request.isActive()
        );
        return getTrainerByUsername(request.username());
    }

    @Transactional
    public List<TrainerDto> getAvailableTrainersForTrainee(String traineeUsername) {
        List<Trainer> availableTrainers = trainerService.getAvailableTrainersForTrainee(traineeUsername);
        return availableTrainers
                .stream()
                .map(trainer -> new TrainerDto(trainer.getUser().getUsername(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getTrainingType().getType().toString()))
                .toList();
    }

    ////////////////////////////////////////////////
    //////////// TRAINING //////////////////////////
    ////////////////////////////////////////////////

    @Transactional
    public void registerTraining(TrainingRegistrationRequest request) {
        Trainee trainee = traineeService.findTraineeByUsername(request.traineeUsername());
        Trainer trainer = trainerService.findTrainerByUsername(request.trainerUsername());
        trainingService.createTraining(trainee,
                trainer,
                request.name(),
                trainer.getTrainingType(),
                request.date(),
                request.duration());
    }

    @Transactional
    public List<TraineeTrainingResponse> findTraineeTrainings(String username,
                                                              TraineeTrainingRequest request) {
        TrainingType trainingType = null;
        if (request.specialization() != null) {
            trainingType = trainingTypeService.findByType(request.specialization());
        }
        List<Training> trainings = trainingService.getTraineeTrainings(username,
                request.from(),
                request.to(),
                request.trainerName(),
                trainingType);
        return trainings.stream().
                map(training -> new TraineeTrainingResponse(training.getName(),
                        training.getDate().toString(),
                        training.getTrainingType().getType().toString(),
                        training.getDuration(),
                        training.getTrainer().getUser().getUsername()))
                .toList();
    }

    @Transactional
    public List<TrainerTrainingResponse> findTrainerTrainings(String username,
                                                              TrainerTrainingRequest request) {
        List<Training> trainings = trainingService.getTrainerTrainings(username,
                request.from(),
                request.to(),
                request.traineeName());
        return trainings.stream().
                map(training -> new TrainerTrainingResponse(training.getName(),
                        training.getDate().toString(),
                        training.getTrainingType().getType().toString(),
                        training.getDuration(),
                        training.getTrainee().getUser().getUsername()))
                .toList();
    }

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
