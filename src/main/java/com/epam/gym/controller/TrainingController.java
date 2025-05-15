package com.epam.gym.controller;

import com.epam.gym.dto.TraineeRegistrationRequest;
import com.epam.gym.dto.TraineeRegistrationResponse;
import com.epam.gym.dto.TrainingRegistrationRequest;
import com.epam.gym.service.FacadeService;
import com.epam.gym.service.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/trainings")
public class TrainingController {

    private final FacadeService facadeService;
    private final TokenService tokenService;

    @Autowired
    public TrainingController(FacadeService facadeService,
            TokenService tokenService
    ) {
        this.facadeService = facadeService;
        this.tokenService = tokenService;
    }

    @PostMapping("/")
    public ResponseEntity<Void> registerTrainee(
            @Valid @RequestBody TrainingRegistrationRequest request
    ) {
        facadeService.registerTraining(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
