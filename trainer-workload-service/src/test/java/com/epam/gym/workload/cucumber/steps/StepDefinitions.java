package com.epam.gym.workload.cucumber.steps;

import io.cucumber.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

public class StepDefinitions {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Given("MongoDB is connected")
    public void mongodb_is_connected() {
        String databaseName = mongoTemplate.getDb().getName();
    }
}
