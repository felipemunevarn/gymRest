package com.epam.gym.cucumber.steps;

import io.cucumber.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import com.epam.gym.support.TestAuthState;

public class AuthSteps {

    @Autowired
    TestAuthState auth;

    @Given("I am authenticated as {string} with password {string}")
    public void i_am_authenticated_as_with_password(String username, String password) {
        // We’re not validating the password in tests; we inject the principal.
        auth.setUsername(username);

        // Let our test security filter pick up the username
        HttpHeaders h = auth.getHeaders();
        h.set("X-Username", username);

        // If you also need a role header or similar, set it here as well.
        // h.set("X-Role", "TRAINER");
    }

}
