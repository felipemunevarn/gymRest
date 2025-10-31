package com.epam.gym.support;

import io.cucumber.spring.ScenarioScope;
import org.springframework.stereotype.Component;
import com.epam.gym.messaging.TrainerWorkloadEvent;

@Component
@ScenarioScope
public class TestMessagingState {

    private TrainerWorkloadEvent.TrainerWorkloadPayload lastPayload;
    private int sendCount;

    public TrainerWorkloadEvent.TrainerWorkloadPayload getLastPayload() {
        return lastPayload;
    }

    public void setLastPayload(TrainerWorkloadEvent.TrainerWorkloadPayload lastPayload) {
        this.lastPayload = lastPayload;
    }

    public int getSendCount() {
        return sendCount;
    }

    public void incrementSendCount() {
        this.sendCount++;
    }
}
