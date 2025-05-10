package com.epam.gym.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "trainees")
public class Trainee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "address")
    private String address;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    @ManyToMany
    @JoinTable(
            name = "trainee_trainer",
            joinColumns = @JoinColumn(name = "trainee_id"),
            inverseJoinColumns = @JoinColumn(name = "trainer_id")
    )
    private Set<Trainer> trainers = new HashSet<>();

    public Long getId() { return id; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getAddress() { return address; }
    public User getUser() { return user; }
    public Set<Trainer> getTrainers() { return trainers; }

    protected Trainee() {}

    private Trainee(Builder builder) {
        this.id = builder.id;
        this.dateOfBirth = builder.dateOfBirth;
        this.address = builder.address;
        this.user = builder.user;
        this.trainers = builder.trainers;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        private Long id;
        private LocalDate dateOfBirth;
        private String address;
        private User user;
        private Set<Trainer> trainers;

        public Builder() {}

        public Builder(Trainee trainee) {
            this.id = trainee.id;
            this.dateOfBirth = trainee.dateOfBirth;
            this.address = trainee.address;
            this.user = trainee.user;
            this.trainers = trainee.trainers;
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder dateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder trainers(Set<Trainer> trainers) {
            this.trainers = trainers;
            return this;
        }

        public Trainee build() {
            return new Trainee(this);
        }
    }
}


