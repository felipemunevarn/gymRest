package com.epam.gym.workload.messaging;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkloadEvent {

    @NotNull
    private String messageId;

    @NotNull
    private String messageType; // "TRAINER_WORKLOAD_UPDATE"

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @NotNull
    private String source; // "main-service"

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
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate trainingDate;

        @NotNull
        private Integer trainingDuration;

        @NotNull
        private ActionType actionType;

        public enum ActionType {
            ADD, DELETE
        }
    }
}