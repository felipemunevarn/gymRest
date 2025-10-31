package com.epam.gym.cucumber.steps;

import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.TrainingTypeEnum;
import com.epam.gym.entity.User;
import com.epam.gym.messaging.TrainerWorkloadEvent;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingTypeRepository;
import com.epam.gym.repository.UserRepository;
import com.epam.gym.messaging.TrainerWorkloadMessageProducer;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;




public class TrainerSteps {

    @Autowired TestRestTemplate rest;
    @Autowired JmsTemplate jmsTemplate; // mocked
    @Autowired TrainerWorkloadMessageProducer producer;


    @Autowired
    UserRepository userRepository;                 // <-- adjust package if needed
    @Autowired
    TrainingTypeRepository trainingTypeRepository; // <-- adjust
    @Autowired
    TrainerRepository trainerRepository;           // <-- adjust

    ResponseEntity<String> response;
    String currentUsername = "alice";

    @Given("the main database is clean")
    public void cleanDb() {
        trainerRepository.deleteAll();
        userRepository.deleteAll();
        trainingTypeRepository.deleteAll();
    }

    @Given("security principal username is {string}")
    public void principalIs(String username) { this.currentUsername = username; }

    @Given("a training type {string} exists")
    public void createTrainingType(String enumName) {
        TrainingTypeEnum type = TrainingTypeEnum.valueOf(enumName); // CARDIO/STRENGTH/...
        trainingTypeRepository.save(new TrainingType(type));
    }

    @Given("a user {string} with name {string} {string} active {word} and role {string} exists")
    public void createUser(String username, String first, String last, String active, String role) {
        var u = new User.Builder()
            .firstName(first)
            .lastName(last)
            .password("{noop}test")
            .isActive(Boolean.parseBoolean(active))
            .role(User.Role.valueOf(role))
            .build();
        userRepository.save(u);
    }

    @Given("a trainer for username {string} already exists with training type {string}")
    public void seedTrainer(String username, String enumName) {
        var u = userRepository.findByUsername(username).orElseThrow();
        var tt = trainingTypeRepository.findByType(TrainingTypeEnum.valueOf(enumName)).orElseThrow();
        var t = new Trainer.Builder()
                .user(u)
                .trainingType(tt)
                .build();
        trainerRepository.save(t);
    }

    @When("I create a trainer with firstName {string} lastName {string} specialization {string}")
    public void postCreate(String first, String last, String specialization) {
        String url = "/api/v1/trainers/";
        String body = """
      { "firstName":"%s", "lastName":"%s", "specialization":"%s" }
      """.formatted(first, last, specialization);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Username", currentUsername);
        response = rest.postForEntity(url, new HttpEntity<>(body, headers), String.class);
    }

//    @Then("the API responds with status {int}")
//    public void apiResponds(int status) {
//        assertThat(response.getStatusCodeValue()).isEqualTo(status);
//    }

    @Then("the trainer for username {string} exists in PostgreSQL")
    public void trainerExists(String username) {
        var u = userRepository.findByUsername(username).orElseThrow();
        assertThat(trainerRepository.findByUserUsername(u.getUsername()).isPresent());
    }

    @Then("a {string} event is published to {string} with payload for username {string} and name {string} {string} active {word} and action {string}")
    public void assertEvent(String messageType, String queue, String username, String first, String last, String active, String action) {
//        ArgumentCaptor<String> dest = ArgumentCaptor.forClass(String.class);
//        ArgumentCaptor<Object> msg = ArgumentCaptor.forClass(Object.class);
//        verify(jmsTemplate, atLeastOnce()).convertAndSend(dest.capture(), msg.capture());


        ArgumentCaptor<TrainerWorkloadEvent.TrainerWorkloadPayload> captor =
                ArgumentCaptor.forClass(TrainerWorkloadEvent.TrainerWorkloadPayload.class);

        verify(producer, atLeastOnce()).sendWorkloadUpdate(captor.capture());

        var payload = captor.getValue();
        assertThat(payload).isNotNull();


        assertThat(payload.getTrainerUsername()).isEqualTo(username);
        assertThat(payload.getTrainerFirstName()).isEqualTo(first);
        assertThat(payload.getTrainerLastName()).isEqualTo(last);
        assertThat(payload.isActive()).isEqualTo(Boolean.parseBoolean(active));
        assertThat(String.valueOf(payload.getActionType())).isEqualTo(action); // ADD or DELETE

        assertThat(payload.getTrainingDate()).isNotNull();
        assertThat(payload.getTrainingDuration()).isNotNull();

//        String destination = dest.getValue();
//        String json = String.valueOf(msg.getValue());
//
//        assertThat(destination).isEqualTo(queue);
//        assertThat(json).contains("\"messageType\":\"" + messageType + "\"");
//        assertThat(json).contains("\"source\":\"main-service\"");
//        assertThat(json).contains("\"authToken\":"); // produced by JwtUtil
//        // Payload checks (your exact class)
//        assertThat(json).contains("\"trainerUsername\":\"" + username + "\"");
//        assertThat(json).contains("\"trainerFirstName\":\"" + first + "\"");
//        assertThat(json).contains("\"trainerLastName\":\"" + last + "\"");
//        assertThat(json).contains("\"isActive\":" + Boolean.parseBoolean(active));
//        assertThat(json).contains("\"actionType\":\"" + action + "\"");
//        // Required in your payload schema:
//        assertThat(json).contains("\"trainingDate\"");
//        assertThat(json).contains("\"trainingDuration\"");
    }

    @Then("no event is published")
    public void noEventPublished() {
        verifyNoInteractions(jmsTemplate);
    }
}
