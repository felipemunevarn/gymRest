package com.epam.gym.workload.repository;

import com.epam.gym.workload.document.TrainerTrainingSummary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerTrainingSummaryRepository extends MongoRepository<TrainerTrainingSummary, String> {

    /**
     * Find trainer summary by username
     */
    Optional<TrainerTrainingSummary> findByTrainerUsername(String trainerUsername);

    /**
     * Check if trainer exists by username
     */
    boolean existsByTrainerUsername(String trainerUsername);

    /**
     * Find trainers by first name and last name (uses the compound index)
     */
    @Query("{ 'trainerFirstName': ?0, 'trainerLastName': ?1 }")
    List<TrainerTrainingSummary> findByTrainerFirstNameAndTrainerLastName(String firstName, String lastName);

    /**
     * Find all active trainers
     */
    List<TrainerTrainingSummary> findByTrainerStatusTrue();

    /**
     * Find trainers by status
     */
    List<TrainerTrainingSummary> findByTrainerStatus(Boolean status);
}