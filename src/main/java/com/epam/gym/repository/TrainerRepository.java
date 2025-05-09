package com.epam.gym.repository;

import com.epam.gym.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    Optional<Trainer> findByUserUsername(String username);
    @Query("""
            SELECT t FROM Trainer t 
            WHERE t NOT IN (
            SELECT tr.trainer FROM Training tr
            WHERE tr.trainee.user.username = :username)
            """)
    List<Trainer> findTrainersNotAssignedToTrainee(@Param("username") String username);
}
