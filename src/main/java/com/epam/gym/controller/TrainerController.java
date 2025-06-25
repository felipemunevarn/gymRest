package com.epam.gym.controller;

import com.epam.gym.dto.*;
import com.epam.gym.exception.InvalidTokenException;
import com.epam.gym.service.TokenService;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.TrainingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat; // Import for date formatting
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate; // Import LocalDate
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/trainers")
public class TrainerController {

    private final TrainerService trainerService;
    private final TrainingService trainingService;
//    private final TokenService tokenService;

    @Autowired
    public TrainerController(
//            TokenService tokenService,
            TrainerService trainerService,
            TrainingService trainingService
    ) {
//        this.tokenService = tokenService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    /**
     * Registers a new trainer.
     *
     * @param request The trainer registration request body.
     * @return ResponseEntity with TrainerRegistrationResponse and HTTP status CREATED.
     */
    @PostMapping("/")
    public ResponseEntity<TrainerRegistrationResponse> registerTrainer(
            @Valid @RequestBody TrainerRegistrationRequest request
    ) {
        TrainerRegistrationResponse response = trainerService.createTrainer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets trainer profile by username. Requires authentication token.
     *
     * @param username The username of the trainer.
     * @return ResponseEntity with TrainerProfileResponse and HTTP status OK.
     * @throws InvalidTokenException if the token is invalid.
     */
    @GetMapping("/{username}")
    public ResponseEntity<TrainerProfileResponse> getTrainer(
            @PathVariable @NotBlank String username
    ) {
        TrainerProfileResponse response = trainerService.findTrainerByUsername(username);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates trainer profile. Requires authentication token.
     *
     * @param request The trainer update request body.
     * @return ResponseEntity with updated TrainerProfileResponse and HTTP status OK.
     * @throws InvalidTokenException if the token is invalid.
     */
    @PutMapping("/")
    public ResponseEntity<TrainerProfileResponse> updateTrainer(
            @Valid @RequestBody TrainerUpdateRequest request
    ) {
        TrainerProfileResponse response = trainerService.updateTrainer(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Gets a list of available trainers for a specific trainee. Requires authentication token.
     *
     * @param traineeUsername The username of the trainee.
     * @return ResponseEntity with a list of TrainerDto and HTTP status OK.
     * @throws InvalidTokenException if the token is invalid.
     */
    @GetMapping("/available")
    public ResponseEntity<List<TrainerDto>> getAvailableTrainers(
            @RequestParam String traineeUsername
    ) {
        List<TrainerDto> trainers = trainerService.getAvailableTrainersForTrainee(traineeUsername);
        return ResponseEntity.ok(trainers);
    }

    /**
     * Gets trainings for a trainer with filtering options. Requires authentication token.
     * Uses RequestParam for filtering criteria.
     *
     * @param username The username of the trainer.
     * @param from Start date for filtering trainings.
     * @param to End date for filtering trainings.
     * @param traineeName Trainee name for filtering trainings.
     * @return ResponseEntity with a list of TrainerTrainingResponse and HTTP status OK.
     * @throws InvalidTokenException if the token is invalid.
     */
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainerTrainingResponse>> getTrainerTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from, // Changed to RequestParam
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to, // Changed to RequestParam
            @RequestParam(required = false) String traineeName
    ) {
        TrainerTrainingRequest request = new TrainerTrainingRequest(from, to, traineeName);

        List<TrainerTrainingResponse> response = trainingService.getTrainerTrainings(
                username,
                request
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates the activation status of a trainer. Requires authentication token.
     * Uses PATCH method for partial update.
     *
     * @param request The activate user request body.
     * @return ResponseEntity with HTTP status NO_CONTENT.
     * @throws InvalidTokenException if the token is invalid.
     */
    @PatchMapping("/activation")
    public ResponseEntity<Void> updateTrainerActivation(
            @Valid @RequestBody ActivateUserRequest request
    ) {
        trainerService.changeActiveStatus(request.username(), request.isActive());
        return ResponseEntity.noContent().build();
    }

}
