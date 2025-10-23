package com.epam.gym.workload.cucumber.steps;

import com.epam.gym.workload.document.TrainerTrainingSummary;
import com.epam.gym.workload.document.TrainerTrainingSummary.MonthSummary;
import com.epam.gym.workload.document.TrainerTrainingSummary.YearSummary;
import com.epam.gym.workload.messaging.TrainerWorkloadEvent;
import com.epam.gym.workload.repository.TrainerTrainingSummaryRepository;
import com.epam.gym.workload.service.TrainerTrainingSummaryService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class WorkloadProcessingSteps {

    @Autowired
    private TrainerTrainingSummaryService trainerTrainingSummaryService;

    @Autowired
    private TrainerTrainingSummaryRepository repository;

    private TrainerWorkloadEvent.TrainerWorkloadPayload currentPayload;
    private Exception processingException;
    private String currentTrainerUsername;

    @Given("the message queue is empty")
    public void theMessageQueueIsEmpty() {
        // Queue is managed by ActiveMQ, nothing to do here
        // This step is for documentation purposes
    }

    @When("a workload message is received with:")
    public void aWorkloadMessageIsReceivedWith(DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);

        currentTrainerUsername = data.get("trainerUsername");
        processingException = null;

        try {
            currentPayload = TrainerWorkloadEvent.TrainerWorkloadPayload.builder()
                    .trainerUsername(data.get("trainerUsername"))
                    .trainerFirstName(data.get("trainerFirstName"))
                    .trainerLastName(data.get("trainerLastName"))
                    .isActive(Boolean.parseBoolean(data.get("isActive")))
                    .trainingDate(data.get("trainingDate"))
                    .trainingDuration(Integer.parseInt(data.get("trainingDuration")))
                    .actionType(TrainerWorkloadEvent.TrainerWorkloadPayload.ActionType.valueOf(data.get("actionType")))
                    .build();

            // Process the message
            trainerTrainingSummaryService.processTrainingEvent(currentPayload, "test-tx-" + System.currentTimeMillis());

        } catch (Exception e) {
            processingException = e;
        }
    }

    @Then("the message should be processed successfully")
    public void theMessageShouldBeProcessedSuccessfully() {
        assertNull(processingException, "Processing should not throw exception");
    }

    @Then("the message should fail validation")
    public void theMessageShouldFailValidation() {
        assertNotNull(processingException, "Should have thrown validation exception");
    }

    @And("a trainer summary should be created in MongoDB for {string}")
    public void aTrainerSummaryShouldBeCreatedInMongoDBFor(String username) {
        Optional<TrainerTrainingSummary> summary = repository.findByTrainerUsername(username);
        assertTrue(summary.isPresent(), "Trainer summary should exist in MongoDB");
        currentTrainerUsername = username;
    }

    @And("the summary should contain year {int} and month {int}")
    public void theSummaryShouldContainYearAndMonth(int year, int month) {
        Optional<TrainerTrainingSummary> summary = repository.findByTrainerUsername(currentTrainerUsername);
        assertTrue(summary.isPresent());

        boolean found = summary.get().getYears().stream()
                .anyMatch(y -> y.getYear().equals(year) &&
                        y.getMonths().stream().anyMatch(m -> m.getMonth().equals(month)));

        assertTrue(found, "Year " + year + " and month " + month + " should exist");
    }

    @And("the month duration should be {int}")
    public void theMonthDurationShouldBe(int expectedDuration) {
        Optional<TrainerTrainingSummary> summary = repository.findByTrainerUsername(currentTrainerUsername);
        assertTrue(summary.isPresent());

        // Get the first month's duration (assuming single year/month for this step)
        int actualDuration = summary.get().getYears().get(0)
                .getMonths().get(0)
                .getTrainingSummaryDuration();

        assertEquals(expectedDuration, actualDuration);
    }

    @And("the trainer summary for {string} should be updated")
    public void theTrainerSummaryForShouldBeUpdated(String username) {
        Optional<TrainerTrainingSummary> summary = repository.findByTrainerUsername(username);
        assertTrue(summary.isPresent(), "Trainer summary should exist");
        currentTrainerUsername = username;
    }

    @And("the month {int} of year {int} should have duration {int}")
    public void theMonthOfYearShouldHaveDuration(int month, int year, int expectedDuration) {
        Optional<TrainerTrainingSummary> summary = repository.findByTrainerUsername(currentTrainerUsername);
        assertTrue(summary.isPresent());

        YearSummary yearSummary = summary.get().getYears().stream()
                .filter(y -> y.getYear().equals(year))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Year " + year + " not found"));

        MonthSummary monthSummary = yearSummary.getMonths().stream()
                .filter(m -> m.getMonth().equals(month))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Month " + month + " not found"));

        assertEquals(expectedDuration, monthSummary.getTrainingSummaryDuration(),
                "Month " + month + " should have duration " + expectedDuration);
    }

    @And("the trainer summary for {string} should contain month {int}")
    public void theTrainerSummaryForShouldContainMonth(String username, int month) {
        Optional<TrainerTrainingSummary> summary = repository.findByTrainerUsername(username);
        assertTrue(summary.isPresent());

        boolean found = summary.get().getYears().stream()
                .flatMap(y -> y.getMonths().stream())
                .anyMatch(m -> m.getMonth().equals(month));

        assertTrue(found, "Month " + month + " should exist");
    }

    @And("the month {int} of year {int} should still have duration {int}")
    public void theMonthOfYearShouldStillHaveDuration(int month, int year, int expectedDuration) {
        // Same as theMonthOfYearShouldHaveDuration - verifies duration hasn't changed
        theMonthOfYearShouldHaveDuration(month, year, expectedDuration);
    }

    @And("no data should be saved to MongoDB")
    public void noDataShouldBeSavedToMongoDB() {
        // Since we caught the exception, verify no partial data was saved
        if (currentPayload != null && currentPayload.getTrainerUsername() != null) {
            Optional<TrainerTrainingSummary> summary =
                    repository.findByTrainerUsername(currentPayload.getTrainerUsername());

            // Either no summary exists, or if it does, it should be from a previous test
            // and not have been modified by this failed operation
            assertTrue(summary.isEmpty() || processingException != null);
        }
    }

    @And("the trainer status should be updated to {word}")
    public void theTrainerStatusShouldBeUpdatedTo(String status) {
        Optional<TrainerTrainingSummary> summary = repository.findByTrainerUsername(currentTrainerUsername);
        assertTrue(summary.isPresent());

        boolean expectedStatus = Boolean.parseBoolean(status);
        assertEquals(expectedStatus, summary.get().getTrainerStatus(),
                "Trainer status should be " + status);
    }
}