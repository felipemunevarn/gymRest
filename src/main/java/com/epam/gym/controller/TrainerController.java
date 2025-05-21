package com.epam.gym.controller;

import com.epam.gym.dto.*;
import com.epam.gym.exception.InvalidTokenException;
import com.epam.gym.service.FacadeService;
import com.epam.gym.service.TokenService;
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

    private final FacadeService facadeService;
    private final TokenService tokenService;

    @Autowired
    public TrainerController(FacadeService facadeService,
                             TokenService tokenService) {
        this.facadeService = facadeService;
        this.tokenService = tokenService;
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
        TrainerRegistrationResponse response = facadeService.registerTrainer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets trainer profile by username. Requires authentication token.
     *
     * @param username The username of the trainer.
     * @param token The authentication token.
     * @return ResponseEntity with TrainerProfileResponse and HTTP status OK.
     * @throws InvalidTokenException if the token is invalid.
     */
    @GetMapping("/{username}")
    public ResponseEntity<TrainerProfileResponse> getTrainer(
            @PathVariable @NotBlank String username,
            @RequestHeader("X-Auth-Token") String token
    ) {
        if (!tokenService.isValidToken(username, token)){
            throw new InvalidTokenException("Token not authenticated");
        }
        TrainerProfileResponse response = facadeService.getTrainerByUsername(username);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates trainer profile. Requires authentication token.
     *
     * @param request The trainer update request body.
     * @param token The authentication token.
     * @return ResponseEntity with updated TrainerProfileResponse and HTTP status OK.
     * @throws InvalidTokenException if the token is invalid.
     */
    @PutMapping("/")
    public ResponseEntity<TrainerProfileResponse> updateTrainer(
            @Valid @RequestBody TrainerUpdateRequest request,
            @RequestHeader("X-Auth-Token") String token
    ) {
        if (!tokenService.isValidToken(request.username(), token)){
            throw new InvalidTokenException("Token not authenticated");
        }
        TrainerProfileResponse response = facadeService.updateTrainer(request);
        return ResponseEntity.status(HttpStatus.OK).body(response); // Changed status to OK
    }

    /**
     * Gets a list of available trainers for a specific trainee. Requires authentication token.
     *
     * @param traineeUsername The username of the trainee.
     * @param token The authentication token.
     * @return ResponseEntity with a list of TrainerDto and HTTP status OK.
     * @throws InvalidTokenException if the token is invalid.
     */
    @GetMapping("/available")
    public ResponseEntity<List<TrainerDto>> getAvailableTrainers(
            @RequestParam String traineeUsername,
            @RequestHeader("X-Auth-Token") String token
    ) {
        if (!tokenService.isValidToken(traineeUsername, token)){
            throw new InvalidTokenException("Token not authenticated");
        }
        List<TrainerDto> trainers = facadeService.getAvailableTrainersForTrainee(traineeUsername);
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
     * @param token The authentication token.
     * @return ResponseEntity with a list of TrainerTrainingResponse and HTTP status OK.
     * @throws InvalidTokenException if the token is invalid.
     */
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainerTrainingResponse>> getTrainerTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from, // Changed to RequestParam
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to, // Changed to RequestParam
            @RequestParam(required = false) String traineeName,
            @RequestHeader("X-Auth-Token") String token
    ) {
        if (!tokenService.isValidToken(username, token)){
            throw new InvalidTokenException("Token not authenticated");
        }

        TrainerTrainingRequest request = new TrainerTrainingRequest(from, to, traineeName);

        List<TrainerTrainingResponse> response = facadeService.findTrainerTrainings(username,
                request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates the activation status of a trainer. Requires authentication token.
     * Uses PATCH method for partial update.
     *
     * @param request The activate user request body.
     * @param token The authentication token.
     * @return ResponseEntity with HTTP status NO_CONTENT.
     * @throws InvalidTokenException if the token is invalid.
     */
    @PatchMapping("/activation")
    public ResponseEntity<Void> updateTrainerActivation(
            @Valid @RequestBody ActivateUserRequest request,
            @RequestHeader("X-Auth-Token") String token
    ) {
        if (!tokenService.isValidToken(request.username(), token)){
            throw new InvalidTokenException("Token not authenticated");
        }
        facadeService.changeTrainerActiveStatus(request.username(), request.isActive());
        return ResponseEntity.noContent().build();
    }

}
