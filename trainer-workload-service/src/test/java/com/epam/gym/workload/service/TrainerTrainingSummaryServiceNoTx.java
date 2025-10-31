package com.epam.gym.workload.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class TrainerTrainingSummaryServiceNoTx extends TrainerTrainingSummaryService {
    // inherits all logic, but no @Transactional
}