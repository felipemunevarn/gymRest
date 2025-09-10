package com.epam.gym.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Entity
@Table(name = "trainings")
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne //(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "trainee_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Trainee trainee;

    @ManyToOne
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "training_type_id")
    private TrainingType trainingType;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "duration", nullable = false)
    private int duration;

    public Long getId() { return id; }
    public Trainee getTrainee() { return trainee; }
    public Trainer getTrainer() { return trainer; }
    public String getName() { return name; }
    public TrainingType getTrainingType() { return trainingType; }
    public LocalDate getDate() { return date; }
    public int getDuration() { return duration; }

    protected Training() {}

    private Training(Builder builder) {
        this.id = builder.id;
        this.trainee = builder.trainee;
        this.trainer = builder.trainer;
        this.name = builder.name;
        this.trainingType = builder.trainingType;
        this.date = builder.date;
        this.duration = builder.duration;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        private Long id;
        private Trainee trainee;
        private Trainer trainer;
        private String name;
        private TrainingType trainingType;
        private LocalDate date;
        private int duration;

        public Builder() {}

        public Builder(Training training) {
            this.id = training.id;
            this.trainee = training.trainee;
            this.trainer = training.trainer;
            this.name = training.name;
            this.trainingType = training.trainingType;
            this.date = training.date;
            this.duration = training.duration;
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder trainee(Trainee trainee) {
            this.trainee = trainee;
            return this;
        }

        public Builder trainer(Trainer trainer) {
            this.trainer = trainer;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder trainingType(TrainingType trainingType) {
            this.trainingType = trainingType;
            return this;
        }

        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder duration(int duration) {
            this.duration = duration;
            return this;
        }

        public Training build() {
            return new Training(this);
        }
    }
}


