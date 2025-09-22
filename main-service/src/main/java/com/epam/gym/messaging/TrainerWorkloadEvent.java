package com.epam.gym.messaging;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TrainerWorkloadEvent {

    @NotNull
    private String messageId;

    @NotNull
    private String messageType;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @NotNull
    private String source;

    @NotNull
    private TrainerWorkloadPayload payload;

    public TrainerWorkloadEvent() {}

    public TrainerWorkloadEvent(String messageId, String messageType, LocalDateTime timestamp, String source, TrainerWorkloadPayload payload) {
        this.messageId = messageId;
        this.messageType = messageType;
        this.timestamp = timestamp;
        this.source = source;
        this.payload = payload;
    }

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public TrainerWorkloadPayload getPayload() { return payload; }
    public void setPayload(TrainerWorkloadPayload payload) { this.payload = payload; }

    // Inner class
    public static class TrainerWorkloadPayload {

        @NotNull
        private String trainerUsername;

        private String trainerFirstName;

        private String trainerLastName;

        private boolean isActive;

        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate trainingDate;

        @NotNull
        private Integer trainingDuration;

        @NotNull
        private ActionType actionType;

        public TrainerWorkloadPayload() {}

        public TrainerWorkloadPayload(String trainerUsername, String trainerFirstName, String trainerLastName,
                                      boolean isActive, LocalDate trainingDate, Integer trainingDuration, ActionType actionType) {
            this.trainerUsername = trainerUsername;
            this.trainerFirstName = trainerFirstName;
            this.trainerLastName = trainerLastName;
            this.isActive = isActive;
            this.trainingDate = trainingDate;
            this.trainingDuration = trainingDuration;
            this.actionType = actionType;
        }

        // Getters and Setters
        public String getTrainerUsername() { return trainerUsername; }
        public void setTrainerUsername(String trainerUsername) { this.trainerUsername = trainerUsername; }

        public String getTrainerFirstName() { return trainerFirstName; }
        public void setTrainerFirstName(String trainerFirstName) { this.trainerFirstName = trainerFirstName; }

        public String getTrainerLastName() { return trainerLastName; }
        public void setTrainerLastName(String trainerLastName) { this.trainerLastName = trainerLastName; }

        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }

        public LocalDate getTrainingDate() { return trainingDate; }
        public void setTrainingDate(LocalDate trainingDate) { this.trainingDate = trainingDate; }

        public Integer getTrainingDuration() { return trainingDuration; }
        public void setTrainingDuration(Integer trainingDuration) { this.trainingDuration = trainingDuration; }

        public ActionType getActionType() { return actionType; }
        public void setActionType(ActionType actionType) { this.actionType = actionType; }

        public enum ActionType {
            ADD, DELETE
        }
    }
}
