package com.epam.gym.cucumber;

import com.epam.gym.messaging.TrainerWorkloadMessageProducer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class ComponentTestConfig {
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("main_test")
                    .withUsername("test")
                    .withPassword("test");

    static { POSTGRES.start(); }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);

        r.add("app.queue.trainer-workload", () -> "trainer.workload.updates");
    }

    @MockBean
    TrainerWorkloadMessageProducer trainerWorkloadMessageProducer;
}
