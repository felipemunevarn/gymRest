package com.epam.gym.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class TrainerWorkloadRequest {

    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private Boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate trainingDate;

    private Integer trainingDuration;
    private ActionType actionType;

    public enum ActionType {
        ADD, DELETE
    }

    public TrainerWorkloadRequest() {}

    public TrainerWorkloadRequest(String trainerUsername, String trainerFirstName, String trainerLastName,
                                  Boolean isActive, LocalDate trainingDate, Integer trainingDuration,
                                  ActionType actionType) {
        this.trainerUsername = trainerUsername;
        this.trainerFirstName = trainerFirstName;
        this.trainerLastName = trainerLastName;
        this.isActive = isActive;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
        this.actionType = actionType;
    }

    public String getTrainerUsername() {
        return trainerUsername;
    }

    public void setTrainerUsername(String trainerUsername) {
        this.trainerUsername = trainerUsername;
    }

    public String getTrainerFirstName() {
        return trainerFirstName;
    }

    public void setTrainerFirstName(String trainerFirstName) {
        this.trainerFirstName = trainerFirstName;
    }

    public String getTrainerLastName() {
        return trainerLastName;
    }

    public void setTrainerLastName(String trainerLastName) {
        this.trainerLastName = trainerLastName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(LocalDate trainingDate) {
        this.trainingDate = trainingDate;
    }

    public Integer getTrainingDuration() {
        return trainingDuration;
    }

    public void setTrainingDuration(Integer trainingDuration) {
        this.trainingDuration = trainingDuration;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String trainerUsername;
        private String trainerFirstName;
        private String trainerLastName;
        private Boolean isActive;
        private LocalDate trainingDate;
        private Integer trainingDuration;
        private ActionType actionType;

        public Builder trainerUsername(String trainerUsername) {
            this.trainerUsername = trainerUsername;
            return this;
        }

        public Builder trainerFirstName(String trainerFirstName) {
            this.trainerFirstName = trainerFirstName;
            return this;
        }

        public Builder trainerLastName(String trainerLastName) {
            this.trainerLastName = trainerLastName;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder trainingDate(LocalDate trainingDate) {
            this.trainingDate = trainingDate;
            return this;
        }

        public Builder trainingDuration(Integer trainingDuration) {
            this.trainingDuration = trainingDuration;
            return this;
        }

        public Builder actionType(ActionType actionType) {
            this.actionType = actionType;
            return this;
        }

        public TrainerWorkloadRequest build() {
            return new TrainerWorkloadRequest(trainerUsername, trainerFirstName, trainerLastName,
                    isActive, trainingDate, trainingDuration, actionType);
        }
    }
}