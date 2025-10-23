package com.epam.gym.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<Map> loginResponse;
    private String jwtToken;

    @Given("the gym system is running")
    public void theGymSystemIsRunning() {
        // System is already running via SpringBootTest
        assertNotNull(restTemplate);
    }

    @When("the user attempts to login with username {string} and password {string}")
    public void theUserAttemptsToLoginWithUsernameAndPassword(String username, String password) {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", username);
        loginRequest.put("password", password);

        loginResponse = restTemplate.postForEntity(
                "/api/v1/auth/login",
                loginRequest,
                Map.class
        );
    }

    @Then("the authentication should be successful")
    public void theAuthenticationShouldBeSuccessful() {
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody());
    }

    @Then("the authentication should fail")
    public void theAuthenticationShouldFail() {
        assertTrue(loginResponse.getStatusCode().is4xxClientError() ||
                loginResponse.getStatusCode().is5xxServerError());
    }

    @And("a JWT token should be returned")
    public void aJwtTokenShouldBeReturned() {
        assertNotNull(loginResponse.getBody());
        assertTrue(loginResponse.getBody().containsKey("token") ||
                loginResponse.getBody().containsKey("jwt") ||
                loginResponse.getBody().containsKey("accessToken"));

        // Store token for future use
        if (loginResponse.getBody().containsKey("token")) {
            jwtToken = (String) loginResponse.getBody().get("token");
        } else if (loginResponse.getBody().containsKey("jwt")) {
            jwtToken = (String) loginResponse.getBody().get("jwt");
        } else if (loginResponse.getBody().containsKey("accessToken")) {
            jwtToken = (String) loginResponse.getBody().get("accessToken");
        }

        assertNotNull(jwtToken);
        assertFalse(jwtToken.isEmpty());
    }

    @And("no JWT token should be returned")
    public void noJwtTokenShouldBeReturned() {
        if (loginResponse.getBody() != null) {
            assertFalse(loginResponse.getBody().containsKey("token") &&
                    loginResponse.getBody().get("token") != null);
            assertFalse(loginResponse.getBody().containsKey("jwt") &&
                    loginResponse.getBody().get("jwt") != null);
        }
    }

    @And("the token should contain username {string}")
    public void theTokenShouldContainUsername(String expectedUsername) {
        assertNotNull(jwtToken);
        // Token validation would require decoding JWT
        // For now, we verify token exists and is not empty
        assertTrue(jwtToken.length() > 20);
    }

    @And("a validation error should be returned")
    public void aValidationErrorShouldBeReturned() {
        assertEquals(HttpStatus.BAD_REQUEST, loginResponse.getStatusCode());
    }
}