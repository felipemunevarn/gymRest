package com.epam.gym.controller;

import com.epam.gym.dto.TrainingRegistrationRequest;
import com.epam.gym.exception.InvalidTokenException;
import com.epam.gym.service.TrainingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/trainings")
public class TrainingController {

    private final TrainingService trainingService;

    @Autowired
    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @PostMapping("/")
    public ResponseEntity<Void> registerTrainee(
            @Valid @RequestBody TrainingRegistrationRequest request
    ) {
        trainingService.createTraining(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{trainingId}")
    public ResponseEntity<Void> deleteTrainee(
            @PathVariable Long trainingId
    ) {
        trainingService.deleteTraining(trainingId);
        return ResponseEntity.noContent().build();
    }

}
