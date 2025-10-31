package com.epam.gym.workload.cucumber;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {
                "com.epam.gym.workload.cucumber.steps",
                "com.epam.gym.workload.cucumber",
                "com.epam.gym"
        },
        plugin = {"pretty", "html:target/cucumber-reports/cucumber.html"},
        tags = "@focus"
)
public class CucumberIT {
}
