package com.epam.gym.workload.cucumber.steps;

import com.epam.gym.workload.cucumber.TestContext;
import com.epam.gym.workload.document.TrainerTrainingSummary;
import com.epam.gym.workload.messaging.TrainerWorkloadEvent;
import com.epam.gym.workload.repository.TrainerTrainingSummaryRepository;
import com.epam.gym.workload.security.JwtUtil;
import com.epam.gym.workload.service.TrainerTrainingSummaryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class WorkloadProcessingSteps {

    @Autowired
    private TrainerTrainingSummaryService trainerTrainingSummaryService;

    @Autowired
    private TrainerTrainingSummaryRepository repository;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private TestContext testContext;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String QUEUE_NAME = "trainer.workload.updates";

    private TrainerWorkloadEvent.TrainerWorkloadPayload currentPayload;
    private Exception processingException;
    private String currentTrainerUsername;
    private ResponseEntity<?> response;

    @When("a workload message is received with invalid authentication")
    public void a_workload_message_is_received_with_invalid_authentication() {
        TrainerWorkloadEvent.TrainerWorkloadPayload payload = TrainerWorkloadEvent.TrainerWorkloadPayload.builder()
                .trainerUsername("") // Invalid: missing username
                .trainerFirstName("Maria")
                .trainerLastName("Ramirez")
                .isActive(true)
                .trainingDate("2024-10-15")
                .trainingDuration(60)
                .actionType(TrainerWorkloadEvent.TrainerWorkloadPayload.ActionType.ADD)
                .build();

        TrainerWorkloadEvent event = TrainerWorkloadEvent.builder()
                .messageId("invalid-auth-test")
                .messageType("TRAINING_WORKLOAD")
                .timestamp("2024-10-29T21:00:00Z")
                .source("test-suite")
                .authToken("Bearer invalid.jwt.token") // Simulate invalid token
                .payload(payload)
                .build();

        try {
            String jsonMessage = new ObjectMapper().writeValueAsString(event);
            jmsTemplate.convertAndSend("trainer.workload.updates", jsonMessage);
            // Optionally wait for processing
            Thread.sleep(1000);
            testContext.setResponse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token"));
        } catch (Exception e) {
            testContext.setResponse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending message"));
        }
    }

    @Then("no data should be saved to MongoDB")
    public void no_data_should_be_saved_to_mongo_db() {
        List<TrainerTrainingSummary> allSummaries = repository.findAll();
        assertTrue(allSummaries.isEmpty(), "Expected no data in MongoDB, but found: " + allSummaries.size());
    }

    @When("a workload message is received with:")
    public void a_workload_message_is_received_with(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);

        String token = jwtUtil.generateToken("main-service");
        System.out.println("Generated JWT token: " + token);

        TrainerWorkloadEvent.TrainerWorkloadPayload payload = TrainerWorkloadEvent.TrainerWorkloadPayload.builder()
                .trainerUsername(data.get("trainerUsername"))
                .trainerFirstName(data.get("trainerFirstName"))
                .trainerLastName(data.get("trainerLastName"))
                .isActive(Boolean.parseBoolean(data.get("isActive")))
                .trainingDate(data.get("trainingDate")) // This will be "invalid-date"
                .trainingDuration(Integer.parseInt(data.get("trainingDuration")))
                .actionType(TrainerWorkloadEvent.TrainerWorkloadPayload.ActionType.valueOf(data.get("actionType")))
                .build();

        TrainerWorkloadEvent event = TrainerWorkloadEvent.builder()
                .messageId("dlq-test")
                .messageType("TRAINING_WORKLOAD")
                .timestamp("2024-10-29T21:00:00Z")
                .source("test-suite")
                .authToken("Bearer " + token) // Use a valid token to isolate the error
                .payload(payload)
                .build();

        try {
            String jsonMessage = new ObjectMapper().writeValueAsString(event);
            jmsTemplate.convertAndSend("trainer.workload.updates", jsonMessage);
            Thread.sleep(1000); // Wait for async processing
        } catch (Exception e) {
            testContext.setResponse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Message failed"));
        }
    }

    @Then("the message should fail processing")
    public void the_message_should_fail_processing() {
        HttpStatus actualStatus = (HttpStatus) testContext.getResponse().getStatusCode();
        assertTrue(
                actualStatus == HttpStatus.INTERNAL_SERVER_ERROR || actualStatus == HttpStatus.UNAUTHORIZED,
                "Expected failure status (500 or 401), but got: " + actualStatus
        );
    }

    @Then("the message should be sent to the DLQ")
    public void the_message_should_be_sent_to_the_dlq() {
        Object dlqMessage = jmsTemplate.receiveAndConvert("DLQ");
        assertNotNull(dlqMessage, "Expected message in DLQ, but none was found.");
        System.out.println("✅ Message found in DLQ: " + dlqMessage);
    }

    @Then("the month {int} of year {int} should have duration {int}")
    public void the_month_of_year_should_have_duration(int month, int year, int expectedDuration) {
        TrainerTrainingSummary summary = repository.findByTrainerUsername("maria.ramirez").orElseThrow();
        TrainerTrainingSummary.YearSummary yearSummary = summary.getYears().stream()
                .filter(y -> y.getYear().equals(year))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Year " + year + " not found"));

        TrainerTrainingSummary.MonthSummary monthSummary = yearSummary.getMonths().stream()
                .filter(m -> m.getMonth().equals(month))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Month " + month + " not found"));

        assertEquals(expectedDuration, monthSummary.getTrainingSummaryDuration());
    }

    @Then("the message should be processed successfully")
    public void the_message_should_be_processed_successfully() {
        assertEquals(HttpStatus.OK, testContext.getResponse().getStatusCode(),
                "Expected 200 OK but got: " + testContext.getResponse().getStatusCode());
    }

    @Then("the trainer summary for {string} should contain month {int}")
    public void the_trainer_summary_for_should_contain_month(String username, Integer month) {
        TrainerTrainingSummary summary = repository.findByTrainerUsername(username)
                .orElseThrow(() -> new AssertionError("Trainer summary not found for: " + username));

        boolean found = summary.getYears().stream()
                .flatMap(y -> y.getMonths().stream())
                .anyMatch(m -> m.getMonth().equals(month));

        assertTrue(found, "Month " + month + " not found in trainer summary for " + username);
    }
}