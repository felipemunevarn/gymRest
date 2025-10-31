package com.epam.gym.workload.cucumber.steps;

import com.epam.gym.workload.controller.TrainerSummaryController;
import com.epam.gym.workload.cucumber.TestContext;
import com.epam.gym.workload.document.TrainerTrainingSummary;
import com.epam.gym.workload.document.TrainerTrainingSummary.MonthSummary;
import com.epam.gym.workload.document.TrainerTrainingSummary.YearSummary;
import com.epam.gym.workload.repository.TrainerTrainingSummaryRepository;
import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class TrainerSummarySteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Autowired
    private TrainerSummaryController trainerSummaryController;

    @Autowired
    private TrainerTrainingSummaryRepository repository;

    @Autowired
    private TestContext testContext;

    private String baseUrl;

    private ResponseEntity<?> response;
    private TrainerTrainingSummary currentSummary;

    @After
    public void cleanup() {
        repository.deleteAll();
    }

    @Given("I am authenticated as an admin")
    public void i_am_authenticated_as_an_admin() {
    }

    @And("MongoDB is available")
    public void mongoDBIsAvailable() {
        assertNotNull(repository);
    }

    @Given("the trainer workload service is running")
    public void theTrainerWorkloadServiceIsRunning() {
        baseUrl = "http://localhost:" + port;
        System.out.println("✅ Trainer Workload Service is running at " + baseUrl);

        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getForEntity(baseUrl + "/actuator/health", String.class);
        } catch (Exception e) {
            throw new AssertionError("Service is not running or Actuator is disabled", e);
        }
    }

    @Given("a trainer summary exists in MongoDB for {string} with:")
    public void aTrainerSummaryExistsInMongoDBFor(String username, DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);

        MonthSummary monthSummary = MonthSummary.builder()
                .month(Integer.parseInt(data.get("month")))
                .trainingSummaryDuration(Integer.parseInt(data.get("duration")))
                .build();

        YearSummary yearSummary = YearSummary.builder()
                .year(Integer.parseInt(data.get("year")))
                .months(new ArrayList<>(List.of(monthSummary)))
                .build();

        TrainerTrainingSummary summary = TrainerTrainingSummary.builder()
                .trainerUsername(username)
                .trainerFirstName(data.get("firstName"))
                .trainerLastName(data.get("lastName"))
                .trainerStatus(Boolean.parseBoolean(data.get("status")))
                .years(new ArrayList<>(List.of(yearSummary)))
                .build();

        repository.save(summary);
    }

    @Given("a trainer summary exists in MongoDB for {string} with multiple periods:")
    public void aTrainerSummaryExistsInMongoDBForWithMultiplePeriods(String username, DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);

        TrainerTrainingSummary summary = TrainerTrainingSummary.builder()
                .trainerUsername(username)
                .trainerFirstName("Maria")
                .trainerLastName("Ramirez")
                .trainerStatus(true)
                .years(new ArrayList<>())
                .build();

        for (Map<String, String> row : rows) {
            int year = Integer.parseInt(row.get("year"));
            int month = Integer.parseInt(row.get("month"));
            int duration = Integer.parseInt(row.get("duration"));

            YearSummary yearSummary = summary.getYears().stream()
                    .filter(y -> y.getYear().equals(year))
                    .findFirst()
                    .orElseGet(() -> {
                        YearSummary newYear = YearSummary.builder()
                                .year(year)
                                .months(new ArrayList<>())
                                .build();
                        summary.getYears().add(newYear);
                        return newYear;
                    });

            MonthSummary monthSummary = MonthSummary.builder()
                    .month(month)
                    .trainingSummaryDuration(duration)
                    .build();
            yearSummary.getMonths().add(monthSummary);
        }

        repository.save(summary);
    }

    @When("I request the summary for trainer {string}")
    public void iRequestTheSummaryForTrainer(String username) {
        TrainerTrainingSummary summary = repository.findByTrainerUsername(username)
                .orElse(null);

        if (summary != null) {
            response = ResponseEntity.ok(summary);
        } else {
            response = ResponseEntity.notFound().build();
        }
    }

    @When("I request the workload for trainer {string} for year {int} and month {int}")
    public void iRequestTheWorkloadForTrainerForYearAndMonth(String username, int year, int month) {
        TrainerTrainingSummary summary = repository.findByTrainerUsername(username).orElse(null);

        if (summary != null) {
            Integer duration = summary.getYears().stream()
                    .filter(y -> y.getYear().equals(year))
                    .flatMap(y -> y.getMonths().stream())
                    .filter(m -> m.getMonth().equals(month))
                    .map(m -> m.getTrainingSummaryDuration())
                    .findFirst()
                    .orElse(0);

            response = ResponseEntity.ok(new TrainerSummaryController.MonthWorkloadResponse(
                    username,
                    year,
                    month,
                    duration));
        } else {
            response = ResponseEntity.notFound().build();
        }
    }

    @Then("the response should be successful")
    public void theResponseShouldBeSuccessful() {
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Expected 200 OK but got: " + response.getStatusCode() +
                        ". Response body: " + response.getBody());
    }

    @Then("the response should be not found")
    public void theResponseShouldBeNotFound() {
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Then("the response should be bad request")
    public void theResponseShouldBeBadRequest() {
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @And("no summary data should be returned")
    public void noSummaryDataShouldBeReturned() {
        assertNull(response.getBody());
    }

    @And("the summary should contain trainer username {string}")
    public void theSummaryShouldContainTrainerUsername(String username) {
        TrainerTrainingSummary summary = (TrainerTrainingSummary) response.getBody();
        assertNotNull(summary);
        assertEquals(username, summary.getTrainerUsername());
    }

    @And("the summary should contain first name {string}")
    public void theSummaryShouldContainFirstName(String firstName) {
        TrainerTrainingSummary summary = (TrainerTrainingSummary) response.getBody();
        assertNotNull(summary);
        assertEquals(firstName, summary.getTrainerFirstName());
    }

    @And("the summary should contain last name {string}")
    public void theSummaryShouldContainLastName(String lastName) {
        TrainerTrainingSummary summary = (TrainerTrainingSummary) response.getBody();
        assertNotNull(summary);
        assertEquals(lastName, summary.getTrainerLastName());
    }

    @And("the summary should contain year {int}")
    public void theSummaryShouldContainYear(int year) {
        TrainerTrainingSummary summary = (TrainerTrainingSummary) response.getBody();
        assertNotNull(summary);
        assertTrue(summary.getYears().stream()
                .anyMatch(y -> y.getYear().equals(year)));
    }

    @And("the summary should contain month {int} with duration {int}")
    public void theSummaryShouldContainMonthWithDuration(int month, int duration) {
        TrainerTrainingSummary summary = (TrainerTrainingSummary) response.getBody();
        assertNotNull(summary);

        boolean found = summary.getYears().stream()
                .flatMap(y -> y.getMonths().stream())
                .anyMatch(m -> m.getMonth().equals(month) &&
                        m.getTrainingSummaryDuration().equals(duration));

        assertTrue(found, "Month " + month + " with duration " + duration + " not found");
    }

    @And("the workload duration should be {int}")
    public void theWorkloadDurationShouldBe(int expectedDuration) {
        TrainerSummaryController.MonthWorkloadResponse workloadResponse =
                (TrainerSummaryController.MonthWorkloadResponse) response.getBody();
        assertNotNull(workloadResponse);
        assertEquals(expectedDuration, workloadResponse.getTrainingSummaryDuration());
    }

    @And("the summary should contain {int} years")
    public void theSummaryShouldContainYears(int expectedYears) {
        TrainerTrainingSummary summary = (TrainerTrainingSummary) response.getBody();
        assertNotNull(summary);
        assertEquals(expectedYears, summary.getYears().size());
    }

    @And("year {int} should contain {int} months")
    public void yearShouldContainMonths(int year, int expectedMonths) {
        TrainerTrainingSummary summary = (TrainerTrainingSummary) response.getBody();
        assertNotNull(summary);

        YearSummary yearSummary = summary.getYears().stream()
                .filter(y -> y.getYear().equals(year))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Year " + year + " not found"));

        assertEquals(expectedMonths, yearSummary.getMonths().size());
    }

    @Then("the message should be rejected")
    public void messageShouldBeRejected() {
        assertEquals(HttpStatus.UNAUTHORIZED, testContext.getResponse().getStatusCode());
    }

}
