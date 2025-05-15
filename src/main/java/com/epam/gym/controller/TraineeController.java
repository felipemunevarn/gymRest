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
@RequestMapping(value = "/api/v1/trainees")
public class TraineeController {

    private final FacadeService facadeService;
    private final TokenService tokenService;

    @Autowired
    public TraineeController(/*TraineeManagementService traineeManagementService,*/
                             FacadeService facadeService,
                             TokenService tokenService
    ) {
        this.facadeService = facadeService;
        this.tokenService = tokenService;
    }

    @GetMapping(value = "/greeting")
    public String greeting() {
        return "Hello, World!";
    }

    @PostMapping("/")
    public ResponseEntity<TraineeRegistrationResponse> registerTrainee(
            @Valid @RequestBody TraineeRegistrationRequest request
    ) {
        TraineeRegistrationResponse response = facadeService.registerTrainee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{username}")
    public ResponseEntity<TraineeProfileResponse> getTrainee(
            @PathVariable @NotBlank String username,
            @RequestHeader("X-Auth-Token") String token
    ) {
        if (!tokenService.isValidToken(token)){
            throw new RuntimeException("Token not authenticated");
        }
        TraineeProfileResponse response = facadeService.getTraineeByUsername(username);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/")
    public ResponseEntity<TraineeProfileResponse> updateTrainee(
            @RequestBody TraineeUpdateRequest request
            ) {
        TraineeProfileResponse response = facadeService.updateTrainee(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteTrainee(@PathVariable String username) {
        facadeService.deleteTrainee(username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TraineeTrainingResponse>> getTraineeTrainings(
            @PathVariable String username,
            @Valid TraineeTrainingRequest request
    ) {
        List<TraineeTrainingResponse> response = facadeService.findTraineeTrainings(username, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{username}/trainers")
    public ResponseEntity<TraineeTrainerResponse> updateTraineeTrainers(
            @PathVariable String username,
            @RequestBody UpdateTraineeTrainersRequest request
    ){
        TraineeTrainerResponse response = facadeService.updateTraineeTrainers(username, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/activation")
    public ResponseEntity<Void> updateTraineeActivation(
            @RequestBody @Valid ActivateUserRequest request) {
        facadeService.changeTraineeActiveStatus(request.username(), request.isActive());
        return ResponseEntity.ok().build();
    }

}
