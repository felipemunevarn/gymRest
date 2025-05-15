package com.epam.gym.repository;

import com.epam.gym.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {

    @Query("""
    SELECT t FROM Training t
    WHERE t.trainee.user.username = :username
    AND (:fromDate IS NULL OR t.date >= :fromDate)
    AND (:toDate IS NULL OR t.date <= :toDate)
    AND (:trainerName IS NULL OR CONCAT(t.trainer.user.firstName, ' ', t.trainer.user.lastName) LIKE %:trainerName%)
    AND (:trainingType IS NULL OR t.trainingType = :trainingType)
""")

    List<Training> findTraineeTrainingsByCriteria(
            @Param("username") String username,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("trainerName") String trainerName,
            @Param("trainingType") TrainingType trainingType
    );

    @Query("""
    SELECT t FROM Training t
    WHERE t.trainer.user.username = :username
    AND (:fromDate IS NULL OR t.date >= :fromDate)
    AND (:toDate IS NULL OR t.date <= :toDate)
    AND (:traineeName IS NULL OR CONCAT(t.trainee.user.firstName, ' ', t.trainee.user.lastName) LIKE %:traineeName%)
""")
    List<Training> findTrainerTrainingsByCriteria(
            @Param("username") String username,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("traineeName") String traineeName
    );

    @Query("""
            SELECT DISTINCT t.trainer FROM Training t
            JOIN FETCH t.trainer.user
            JOIN FETCH t.trainer.trainingType
            WHERE t.trainee.user.username = :traineeUsername
            """)
    List<Trainer> findTrainersByTraineeUsername(@Param("traineeUsername") String traineeUsername);

    @Query("""
            SELECT DISTINCT t.trainee FROM Training t
            JOIN FETCH t.trainee.user
            WHERE t.trainer.user.username = :trainerUsername
            """)
    List<Trainee> findTraineesByTrainerUsername(@Param("trainerUsername") String trainerUsername);

}
