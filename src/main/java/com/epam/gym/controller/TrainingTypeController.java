package com.epam.gym.controller;

import com.epam.gym.dto.TrainingTypeResponse;
import com.epam.gym.service.FacadeService;
import com.epam.gym.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/training-types")
public class TrainingTypeController {

    private final FacadeService facadeService;
    private final TokenService tokenService;

    @Autowired
    public TrainingTypeController(
            FacadeService facadeService,
            TokenService tokenService
    ) {
        this.facadeService = facadeService;
        this.tokenService = tokenService;
    }

    @GetMapping("/")
    public ResponseEntity<List<TrainingTypeResponse>> getAllTrainingTypes(){
        List<TrainingTypeResponse> response = facadeService.findAllTrainingTypes();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
