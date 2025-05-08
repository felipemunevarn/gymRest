package com.epam.gym.repository;

import com.epam.gym.entity.Trainee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TraineeRepository extends JpaRepository<Trainee, Long> {
    Optional<Trainee> findByUserUsername(String username);
    void deleteByUserUsername(String username);
}
