package com.epam.gym.service;

import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.TrainingTypeEnum;
import com.epam.gym.repository.TrainingTypeRepository;
import jakarta.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainingTypeService {

    private static final Logger log = LoggerFactory.getLogger(TrainingTypeService.class);
    private final TrainingTypeRepository trainingTypeRepository;

    @Autowired
    public TrainingTypeService(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Transactional
    public List<TrainingType> findAllTrainingTypes(){
        return trainingTypeRepository.findAll();
    }

    @Transactional
    public TrainingType findByType(String specialization) {
        log.debug("Finding trainingType by specialization: {}", specialization);
        return trainingTypeRepository.findByType(TrainingTypeEnum.valueOf(specialization))
                .orElseThrow(() -> {
                    log.error("TrainingType not found with specialization: {}", specialization);
                    return new NoResultException("TrainingType not found");
                });
    }
}
