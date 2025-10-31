package com.epam.gym.workload.cucumber;

import io.cucumber.java.AfterAll;
import lombok.Setter;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class CucumberShutdownHook {

    @Setter
    private static ConfigurableApplicationContext context;

    @AfterAll
    public static void tearDown() {
        if (context != null) {
            SpringApplication.exit(context, () -> 0);
            context.close();
        }
    }
}

