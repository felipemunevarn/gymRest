package com.epam.gym.workload.messaging;

import com.epam.gym.workload.dto.WorkloadRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import com.epam.gym.workload.service.TrainerWorkloadService;

@Component
public class TrainerWorkloadMessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(TrainerWorkloadMessageConsumer.class);

    @Autowired
    private TrainerWorkloadService trainerWorkloadService;

    @JmsListener(destination = "${app.queue.trainer-workload}")
    public void handleTrainerWorkloadUpdate(TrainerWorkloadEvent event) {
        String transactionId = event.getMessageId();

        try {
            log.info("TRANSACTION_START - Message Consumer: TrainerWorkloadUpdate, " +
                            "MessageId: {}, TrainerUsername: {}, ActionType: {}",
                    transactionId,
                    event.getPayload().getTrainerUsername(),
                    event.getPayload().getActionType());

            // Validate message
            if (!isValidWorkloadEvent(event)) {
                log.error("Invalid workload event received: {}", transactionId);
                throw new IllegalArgumentException("Required fields missing in workload event");
            }

            // Convert message to your existing request format
            WorkloadRequest request = convertEventToRequest(event.getPayload());

            // Process using your existing service
            trainerWorkloadService.processWorkload(request, transactionId);

            log.info("TRANSACTION_END - Message Consumer: TrainerWorkloadUpdate, " +
                            "MessageId: {}, Status: SUCCESS, Message: Workload updated successfully via messaging",
                    transactionId);

        } catch (Exception e) {
            log.error("TRANSACTION_END - Message Consumer: TrainerWorkloadUpdate, " +
                            "MessageId: {}, Status: ERROR, Message: {}",
                    transactionId, e.getMessage(), e);
            // This will trigger retry mechanism and eventually send to DLQ
            throw e;
        }
    }

    private boolean isValidWorkloadEvent(TrainerWorkloadEvent event) {
        return event != null &&
                event.getPayload() != null &&
                event.getPayload().getTrainerUsername() != null &&
                event.getPayload().getTrainingDate() != null &&
                event.getPayload().getTrainingDuration() != null &&
                event.getPayload().getActionType() != null;
    }

    private WorkloadRequest convertEventToRequest(TrainerWorkloadEvent.TrainerWorkloadPayload payload) {
        return WorkloadRequest.builder()
                .trainerUsername(payload.getTrainerUsername())
                .trainerFirstName(payload.getTrainerFirstName())
                .trainerLastName(payload.getTrainerLastName())
                .isActive(payload.isActive())
                .trainingDate(payload.getTrainingDate())
                .trainingDuration(payload.getTrainingDuration())
                .actionType(WorkloadRequest.ActionType.valueOf(payload.getActionType().name()))
                .build();
    }
}