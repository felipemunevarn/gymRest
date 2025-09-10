package com.epam.gym.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "trainers")
public class Trainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "training_type_id")
    private TrainingType trainingType;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    @ManyToMany(mappedBy = "trainers")
    private Set<Trainee> trainees = new HashSet<>();

    public Long getId() { return id; }
    public TrainingType getTrainingType() { return trainingType; }
    public User getUser() { return user; }
    public Set<Trainee> getTrainees() { return trainees; }

    protected Trainer() {}

    private Trainer(Builder builder) {
        this.id = builder.id;
        this.trainingType = builder.trainingType;
        this.user = builder.user;
        this.trainees = builder.trainees;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        private Long id;
        private TrainingType trainingType;
        private User user;
        private Set<Trainee> trainees;

        public Builder() {}

        public Builder(Trainer trainer) {
            this.id = trainer.id;
            this.trainingType = trainer.trainingType;
            this.user = trainer.user;
            this.trainees = trainer.trainees;
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder trainingType(TrainingType trainingType) {
            this.trainingType = trainingType;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder trainees(Set<Trainee> trainees) {
            this.trainees = trainees;
            return this;
        }

        public Trainer build() {
            return new Trainer(this);
        }
    }
}


