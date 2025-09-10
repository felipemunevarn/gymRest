package com.epam.gym.controller;

import com.epam.gym.dto.TrainingTypeResponse;
import com.epam.gym.service.TrainingTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/training-types")
public class TrainingTypeController {

    private final TrainingTypeService trainingTypeService;

    @Autowired
    public TrainingTypeController(
            TrainingTypeService trainingTypeService
    ) {
        this.trainingTypeService = trainingTypeService;
    }

    @GetMapping("/")
    public ResponseEntity<List<TrainingTypeResponse>> getAllTrainingTypes(){
        List<TrainingTypeResponse> response = trainingTypeService.findAllTrainingTypes();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
