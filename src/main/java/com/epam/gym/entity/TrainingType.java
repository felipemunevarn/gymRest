package com.epam.gym.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "training_types")
public class TrainingType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private TrainingTypeEnum type;

    public TrainingType() {}

    public TrainingType(TrainingTypeEnum type) {
        this.type = type;
    }

    public Long getId() { return id; }
    public TrainingTypeEnum getType() { return type; }
}
