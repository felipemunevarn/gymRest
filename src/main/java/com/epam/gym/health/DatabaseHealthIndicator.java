package com.epam.gym.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final boolean isDatabaseUp = true;

    @Override
    public Health health() {
        if (isDatabaseUp) {
            return Health.up()
                    .withDetail("databaseService", "Running")
                    .withDetail("errorCode", 0)
                    .build();
        } else {
            return Health.down()
                    .withDetail("databaseService", "Not Available")
                    .withDetail("error", "Connection timed out")
                    .withDetail("errorCode", 1)
                    .build();
        }
    }
}