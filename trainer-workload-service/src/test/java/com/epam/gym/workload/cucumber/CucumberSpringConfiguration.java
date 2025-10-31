package com.epam.gym.workload.cucumber;

import com.epam.gym.workload.cucumber.config.MongoNoTransactionTestConfig;
import com.epam.gym.workload.cucumber.config.TestSecurityConfig;
import com.mongodb.client.MongoClients;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

@CucumberContextConfiguration
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {com.epam.gym.workload.cucumber.config.TestSecurityConfig.class})
@ActiveProfiles("test")
@Import({MongoNoTransactionTestConfig.class, TestSecurityConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CucumberSpringConfiguration {

    private final ApplicationContext applicationContext;

    public CucumberSpringConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0")
            .withReuse(false);

    static {
        mongoDBContainer.start();
        try {
            mongoDBContainer.execInContainer(
                    "mongosh",
                    "--eval",
                    "rs.initiate({_id: 'rs0', members: [{_id: 0, host: 'localhost:27017'}]})"
            );
            Thread.sleep(5000);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MongoDB replica set", e);
        }
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        if (!mongoDBContainer.isRunning()) {
            mongoDBContainer.start();
        }

        String connectionString = mongoDBContainer.getReplicaSetUrl();

        waitForMongo(connectionString);

        registry.add("spring.data.mongodb.uri", () -> connectionString);
        registry.add("spring.data.mongodb.database", () -> "testdb");
        registry.add("spring.data.mongodb.auto-index-creation", () -> "true");
    }

    private static void waitForMongo(String connectionString) {
        try (var mongoClient = MongoClients.create(connectionString)) {
            int attempts = 0;
            while (attempts < 30) {
                try {
                    mongoClient.getDatabase("test").runCommand(new org.bson.Document("ping", 1));
                    System.out.println(">>> MongoDB is ready!");
                    return;
                } catch (Exception e) {
                    attempts++;
                    System.out.println(">>> Waiting for MongoDB... attempt " + attempts);
                    Thread.sleep(1000);
                }
            }
            throw new RuntimeException("MongoDB failed to become ready within 30 seconds");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("MongoDB wait interrupted", e);
        }
    }

}