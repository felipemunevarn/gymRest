package com.epam.gym.cucumber.steps;

import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import com.epam.gym.support.TestApiState;
import static org.assertj.core.api.Assertions.assertThat;

public class CommonApiSteps {

    @Autowired TestApiState api;

    @Then("the API responds with status {int}")
    public void the_api_responds_with_status(Integer expected) {
        assertThat(api.getLastResponse()).as("No response captured").isNotNull();
        assertThat(api.getLastResponse().getStatusCodeValue()).isEqualTo(expected);
    }
}
