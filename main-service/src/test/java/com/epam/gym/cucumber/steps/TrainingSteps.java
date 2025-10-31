package com.epam.gym.cucumber.steps;

import io.cucumber.java.en.When;
import io.cucumber.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import com.epam.gym.support.TestAuthState;
import com.epam.gym.support.TestApiState;
import com.epam.gym.util.DataTableUtils;

import java.util.Map;

public class TrainingSteps {

    @Autowired TestRestTemplate rest;
    @Autowired TestAuthState auth;
    @Autowired TestApiState api;

    @When("I create a training with following details:")
    public void i_create_a_training_with_following_details(DataTable dataTable) {
        Map<String, String> m = DataTableUtils.toMap(dataTable);

        // Accept common keys; feel free to rename to match your DTO
        String traineeUsername = m.getOrDefault("traineeUsername", auth.getUsername());
        String trainerUsername = m.get("trainerUsername");       // optional if implied by auth
        String type            = m.getOrDefault("type", "STRENGTH");
        String date            = m.getOrDefault("date", "2025-01-01"); // ISO yyyy-MM-dd
        String duration        = m.getOrDefault("duration", "60");     // minutes as string
        String description     = m.getOrDefault("description", "");

        // Persist request for later assertions if needed
        api.put("traineeUsername", traineeUsername);
        api.put("trainerUsername", trainerUsername);
        api.put("type", type);
        api.put("date", date);
        api.put("duration", duration);
        api.put("description", description);

        // Build JSON payload. Adjust fields to your controller's DTO.
        // Example DTO:
        // {
        //   "traineeUsername": "...",
        //   "trainerUsername": "...",
        //   "trainingType": "STRENGTH",
        //   "trainingDate": "2025-01-01",
        //   "trainingDuration": 60,
        //   "description": "Upper body"
        // }
        String body = """
      {
        "traineeUsername": %s,
        "trainerUsername": %s,
        "trainingType": "%s",
        "trainingDate": "%s",
        "trainingDuration": %s,
        "description": %s
      }
      """.formatted(
                toJsonString(traineeUsername),
                toJsonStringOrNull(trainerUsername),
                type, date, duration,
                toJsonString(description)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        auth.applyTo(headers); // injects X-Username or Bearer auth

        // 🔁 Choose the correct endpoint for your API:
        // 1) If your API is POST /api/v1/trainings:
        String url = "/api/v1/trainings";

        // 2) If your API is nested under trainee:
        // String url = "/api/v1/trainees/" + traineeUsername + "/trainings";

        ResponseEntity<String> resp =
                rest.postForEntity(url, new HttpEntity<>(body, headers), String.class);

        api.setLastResponse(resp);
    }

    private static String toJsonString(String v) {
        if (v == null) return "null";
        return "\"" + v.replace("\"","\\\"") + "\"";
    }
    private static String toJsonStringOrNull(String v) {
        return v == null || v.isBlank() ? "null" : toJsonString(v);
    }
}
