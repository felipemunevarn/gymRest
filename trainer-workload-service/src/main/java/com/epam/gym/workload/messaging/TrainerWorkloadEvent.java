package com.epam.gym.workload.messaging;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkloadEvent {

    @NotNull
    private String messageId;

    @NotNull
    private String messageType;

    @NotNull
    private String timestamp;

    @NotNull
    private String source;

    @NotNull
    private String authToken;

    @NotNull
    private TrainerWorkloadPayload payload;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
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

        public enum ActionType {
            ADD, DELETE
        }
    }
}