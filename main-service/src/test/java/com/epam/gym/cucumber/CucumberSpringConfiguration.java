package com.epam.gym.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.profiles.active=test",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class CucumberSpringConfiguration {
}
