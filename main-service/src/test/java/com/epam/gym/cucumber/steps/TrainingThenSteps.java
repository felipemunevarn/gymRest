package com.epam.gym.cucumber.steps;

import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;

import com.epam.gym.support.TestApiState;
import com.epam.gym.support.TestMessagingState;

import com.epam.gym.messaging.TrainerWorkloadMessageProducer;
import com.epam.gym.messaging.TrainerWorkloadEvent;

public class TrainingThenSteps {

    @Autowired
    TestApiState api;

    @Autowired
    TestMessagingState msgState;

    @Autowired
    TrainerWorkloadMessageProducer producer; // Mockito mock from @MockBean

    @Then("the training should be created successfully")
    public void the_training_should_be_created_successfully() {
        ResponseEntity<String> resp = api.getLastResponse();
        assertThat(resp).as("No API response captured").isNotNull();

        // Prefer exact 201; if your endpoint returns 200, you can relax to is2xxSuccessful()
        assertThat(resp.getStatusCodeValue())
                .withFailMessage("Expected HTTP 201 Created but got %s", resp.getStatusCodeValue())
                .isEqualTo(201);
    }

    @Then("a workload message should be sent to the message queue")
    public void a_workload_message_should_be_sent_to_the_message_queue() {
        ArgumentCaptor<TrainerWorkloadEvent.TrainerWorkloadPayload> captor =
                ArgumentCaptor.forClass(TrainerWorkloadEvent.TrainerWorkloadPayload.class);

        // verify at least once
        verify(producer, atLeastOnce()).sendWorkloadUpdate(captor.capture());

        // If multiple calls happen, take the last one
        var payload = captor.getValue();
        assertThat(payload).as("Producer was called but payload was null").isNotNull();

        // Keep it for the next assertions
        msgState.setLastPayload(payload);
        msgState.incrementSendCount();
    }

    @Then("the message should contain trainer username {string}")
    public void the_message_should_contain_trainer_username(String expectedUsername) {
        assertThat(msgState.getLastPayload())
                .as("No captured payload. Make sure 'a workload message should be sent...' step ran first.")
                .isNotNull();

        assertThat(msgState.getLastPayload().getTrainerUsername()).isEqualTo(expectedUsername);
    }

    @Then("the message should contain duration {int}")
    public void the_message_should_contain_duration(Integer expectedDuration) {
        assertThat(msgState.getLastPayload())
                .as("No captured payload. Make sure 'a workload message should be sent...' step ran first.")
                .isNotNull();

        assertThat(msgState.getLastPayload().getTrainingDuration()).isEqualTo(expectedDuration);
    }

    @Then("the message should have action type {string}")
    public void the_message_should_have_action_type(String expectedAction) {
        assertThat(msgState.getLastPayload())
                .as("No captured payload. Make sure 'a workload message should be sent...' step ran first.")
                .isNotNull();

        var actual = String.valueOf(msgState.getLastPayload().getActionType());
        assertThat(actual).isEqualTo(expectedAction); // "ADD" or "DELETE"
    }
}
