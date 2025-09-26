package com.epam.gym.messaging;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TrainerWorkloadEvent {

    @NotNull
    private String messageId;

    @NotNull
    private String messageType;

    @NotNull
    private String timestamp;

    @NotNull
    private String source;

    private String authToken;

    @NotNull
    private TrainerWorkloadPayload payload;

    public TrainerWorkloadEvent() {}

    public TrainerWorkloadEvent(String messageId,
                                String messageType,
                                String timestamp,
                                String source,
                                String authToken,
                                TrainerWorkloadPayload payload) {
        this.messageId = messageId;
        this.messageType = messageType;
        this.timestamp = timestamp;
        this.source = source;
        this.authToken = authToken;
        this.payload = payload;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getAuthToken() { return authToken; }
    public void setAuthToken(String authToken) { this.authToken = authToken; }

    public TrainerWorkloadPayload getPayload() { return payload; }
    public void setPayload(TrainerWorkloadPayload payload) { this.payload = payload; }

    // Builder
    public static class Builder {
        private String messageId;
        private String messageType;
        private String timestamp;
        private String source;
        private String authToken;
        private TrainerWorkloadPayload payload;

        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder messageType(String messageType) {
            this.messageType = messageType;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder payload(TrainerWorkloadPayload payload) {
            this.payload = payload;
            return this;
        }

        public Builder authToken(String authToken) {
            this.authToken = authToken;
            return this;
        }

        public TrainerWorkloadEvent build() {
            return new TrainerWorkloadEvent(messageId, messageType, timestamp, source, authToken, payload);
        }
    }

    // Inner class
    public static class TrainerWorkloadPayload {

        @NotNull
        private String trainerUsername;

        private String trainerFirstName;

        private String trainerLastName;

        private boolean isActive;

        @NotNull
        private String trainingDate;

        @NotNull
        private Integer trainingDuration;

        @NotNull
        private ActionType actionType;

        public TrainerWorkloadPayload() {
        }

        public TrainerWorkloadPayload(String trainerUsername,
                                      String trainerFirstName,
                                      String trainerLastName,
                                      boolean isActive,
                                      String trainingDate,
                                      Integer trainingDuration,
                                      ActionType actionType) {
            this.trainerUsername = trainerUsername;
            this.trainerFirstName = trainerFirstName;
            this.trainerLastName = trainerLastName;
            this.isActive = isActive;
            this.trainingDate = trainingDate;
            this.trainingDuration = trainingDuration;
            this.actionType = actionType;
        }

        public static Builder builder() {
            return new TrainerWorkloadPayload.Builder();
        }

        // Getters and Setters
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

        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean active) {
            isActive = active;
        }

        public String getTrainingDate() {
            return trainingDate;
        }

        public void setTrainingDate(String trainingDate) {
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

        public enum ActionType {
            ADD, DELETE
        }

        // Builder
        public static class Builder {
            private String trainerUsername;
            private String trainerFirstName;
            private String trainerLastName;
            private boolean isActive;
            private String trainingDate;
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

            public Builder isActive(boolean isActive) {
                this.isActive = isActive;
                return this;
            }

            public Builder trainingDate(LocalDate trainingDate) {
                this.trainingDate = trainingDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
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

            public TrainerWorkloadPayload build() {
                return new TrainerWorkloadPayload(trainerUsername, trainerFirstName, trainerLastName,
                        isActive, trainingDate, trainingDuration, actionType);
            }
        }
    }
}
