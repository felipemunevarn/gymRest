package com.epam.gym.controller;

import com.epam.gym.dto.*;
import com.epam.gym.service.FacadeService;
import com.epam.gym.service.TokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/")
    public ResponseEntity<TrainerRegistrationResponse> registerTrainer(
            @Valid @RequestBody TrainerRegistrationRequest request
            ) {
        TrainerRegistrationResponse response = facadeService.registerTrainer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerProfileResponse> getTrainer(
            @PathVariable @NotBlank String username,
            @RequestHeader("X-Auth-Token") String token
    ) {
        if (!tokenService.isValidToken(token)){
            throw new RuntimeException("Token not authenticated");
        }
        TrainerProfileResponse response = facadeService.getTrainerByUsername(username);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/")
    public ResponseEntity<TrainerProfileResponse> updateTrainer(
            @Valid @RequestBody TrainerUpdateRequest request
    ) {
        TrainerProfileResponse response = facadeService.updateTrainer(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/available")
    public ResponseEntity<List<TrainerDto>> getAvailableTrainers(
            @RequestParam String traineeUsername) {
        List<TrainerDto> trainers = facadeService.getAvailableTrainersForTrainee(traineeUsername);
        return ResponseEntity.ok(trainers);
    }

    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainerTrainingResponse>> getTrainerTrainings(
            @PathVariable String username,
            @Valid @RequestBody TrainerTrainingRequest request
    ) {
        List<TrainerTrainingResponse> response = facadeService.findTrainerTrainings(username,
                request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/activation")
    public ResponseEntity<Void> updateTrainerActivation(
            @Valid @RequestBody ActivateUserRequest request) {
        facadeService.changeTrainerActiveStatus(request.username(), request.isActive());
        return ResponseEntity.ok().build();
    }

}
