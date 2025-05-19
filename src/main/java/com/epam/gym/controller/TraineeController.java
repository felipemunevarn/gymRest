package com.epam.gym.controller;

import com.epam.gym.dto.*;
import com.epam.gym.exception.InvalidTokenException;
import com.epam.gym.service.FacadeService;
import com.epam.gym.service.TokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/trainees")
public class TraineeController {

    private final FacadeService facadeService;
    private final TokenService tokenService;

    @Autowired
    public TraineeController(FacadeService facadeService,
                             TokenService tokenService
    ) {
        this.facadeService = facadeService;
        this.tokenService = tokenService;
    }

    /**
     * Registers a new trainee.
     *
     * @param request The trainee registration request body.
     * @return ResponseEntity with TraineeRegistrationResponse and HTTP status CREATED.
     */
    @PostMapping("/")
    public ResponseEntity<TraineeRegistrationResponse> registerTrainee(
            @Valid @RequestBody TraineeRegistrationRequest request
    ) {
        TraineeRegistrationResponse response = facadeService.registerTrainee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets trainee profile by username. Requires authentication token.
     *
     * @param username The username of the trainee.
     * @param token The authentication token.
     * @return ResponseEntity with TraineeProfileResponse and HTTP status OK.
     * @throws InvalidTokenException if the token is invalid.
     */
    @GetMapping("/{username}")
    public ResponseEntity<TraineeProfileResponse> getTrainee(
            @PathVariable @NotBlank String username,
            @RequestHeader("X-Auth-Token") String token
    ) {
        if (!tokenService.isValidToken(token)){
            throw new InvalidTokenException("Token not authenticated");
        }
        TraineeProfileResponse response = facadeService.getTraineeByUsername(username);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates trainee profile. Requires authentication token.
     *
     * @param request The trainee update request body.
     * @param token The authentication token.
     * @return ResponseEntity with updated TraineeProfileResponse and HTTP status OK.
     * @throws InvalidTokenException if the token is invalid.
     */
    @PutMapping("/")
    public ResponseEntity<TraineeProfileResponse> updateTrainee(
            @Valid @RequestBody TraineeUpdateRequest request,
            @RequestHeader("X-Auth-Token") String token
    ) {
        if (!tokenService.isValidToken(token)){
            throw new InvalidTokenException("Token not authenticated");
        }
        TraineeProfileResponse response = facadeService.updateTrainee(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Deletes a trainee by username. Requires authentication token.
     *
     * @param username The username of the trainee to delete.
     * @param token The authentication token.
     * @return ResponseEntity with HTTP status NO_CONTENT.
     * @throws InvalidTokenException if the token is invalid.
     */
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteTrainee(@PathVariable String username,
                                              @RequestHeader("X-Auth-Token") String token
    ) {
        if (!tokenService.isValidToken(token)){
            throw new InvalidTokenException("Token not authenticated");
        }
        facadeService.deleteTrainee(username);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets trainings for a trainee with filtering options. Requires authentication token.
     * Uses RequestParam for filtering criteria.
     *
     * @param username The username of the trainee.
     * @param from Start date for filtering trainings.
     * @param to End date for filtering trainings.
     * @param trainerName Trainer name for filtering trainings.
     * @param specialization Training type specialization for filtering trainings.
     * @param token The authentication token.
     * @return ResponseEntity with a list of TraineeTrainingResponse and HTTP status OK.
     * @throws InvalidTokenException if the token is invalid.
     */
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TraineeTrainingResponse>> getTraineeTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String specialization,
            @RequestHeader("X-Auth-Token") String token
    ) {
        if (!tokenService.isValidToken(token)){
            throw new InvalidTokenException("Token not authenticated");
        }

        TraineeTrainingRequest request = new TraineeTrainingRequest(from,
                to,
                trainerName,
                specialization);

        List<TraineeTrainingResponse> response = facadeService.findTraineeTrainings(username, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates the list of trainers for a trainee. Requires authentication token.
     *
     * @param username The username of the trainee.
     * @param request The update trainee trainers request body.
     * @param token The authentication token.
     * @return ResponseEntity with TraineeTrainerResponse and HTTP status OK.
     * @throws InvalidTokenException if the token is invalid.
     */
    @PutMapping("/{username}/trainers")
    public ResponseEntity<TraineeTrainerResponse> updateTraineeTrainers(
            @PathVariable String username,
            @Valid @RequestBody UpdateTraineeTrainersRequest request,
            @RequestHeader("X-Auth-Token") String token
    ) {
        if (!tokenService.isValidToken(token)){
            throw new InvalidTokenException("Token not authenticated");
        }
        TraineeTrainerResponse response = facadeService.updateTraineeTrainers(username, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates the activation status of a trainee. Requires authentication token.
     * Uses PATCH method for partial update.
     *
     * @param request The activate user request body.
     * @param token The authentication token.
     * @return ResponseEntity with HTTP status NO_CONTENT.
     * @throws InvalidTokenException if the token is invalid.
     */
    @PatchMapping("/activation")
    public ResponseEntity<Void> updateTraineeActivation(
            @Valid @RequestBody ActivateUserRequest request,
            @RequestHeader("X-Auth-Token") String token
    ) {
        if (!tokenService.isValidToken(token)){
            throw new InvalidTokenException("Token not authenticated");
        }
        facadeService.changeTraineeActiveStatus(request.username(), request.isActive());
        return ResponseEntity.noContent().build();
    }

}