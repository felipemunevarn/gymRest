package com.epam.gym.controller;

import com.epam.gym.dto.*;
import com.epam.gym.service.FacadeService;
import com.epam.gym.service.TrainerManagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/trainers")
public class TrainerController {

//    private TrainerManagementService trainerManagementService;
    private final FacadeService facadeService;

    @Autowired
    public TrainerController(/*TrainerManagementService trainerManagementService,*/
                             FacadeService facadeService) {
//        this.trainerManagementService = trainerManagementService;
        this.facadeService = facadeService;
    }

    @PostMapping("/")
    public ResponseEntity<TrainerRegistrationResponse> registerTrainer(
            @Valid @RequestBody TrainerRegistrationRequest request
            ) {
//        TrainerRegistrationResponse response = trainerManagementService.registerTrainer(request);
        TrainerRegistrationResponse response = facadeService.registerTrainer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

//    @GetMapping("/{username}")
//    public ResponseEntity<TrainerProfileResponse> getProfile(
//            @PathVariable @NotBlank String username
//    ) {
//        TrainerProfileResponse response = trainerManagementService.getTrainerByUsername(username);
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }
//
//    @PutMapping
//    public ResponseEntity<TrainerProfileResponse> updateProfile (
//            @RequestBody TrainerUpdateRequest request
//    ) {
//        TrainerProfileResponse response = trainerManagementService.updateTrainer(request);
//        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
//    }

}
