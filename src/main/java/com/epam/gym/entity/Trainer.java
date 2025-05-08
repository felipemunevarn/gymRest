package com.epam.gym.entity;

import jakarta.persistence.*;

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

    public Long getId() { return id; }
    public TrainingType getTrainingType() { return trainingType; }
    public User getUser() { return user; }

    protected Trainer() {}

    private Trainer(Builder builder) {
        this.id = builder.id;
        this.trainingType = builder.trainingType;
        this.user = builder.user;
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

        public Builder() {}

        public Builder(Trainer trainee) {
            this.id = trainee.id;
            this.trainingType = trainee.trainingType;
            this.user = trainee.user;
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

        public Trainer build() {
            return new Trainer(this);
        }
    }
}


