package com.epam.gym.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ExternalServiceHealthIndicator implements HealthIndicator {

    private final boolean isExternalServiceReachable = true;

    @Override
    public Health health() {
        if (isExternalServiceReachable) {
            return Health.up()
                    .withDetail("externalServiceUrl", "[https://api.example.com](https://api.example.com)")
                    .withDetail("responseTimeMs", 50)
                    .build();
        } else {
            return Health.down()
                    .withDetail("externalServiceUrl", "[https://api.example.com](https://api.example.com)")
                    .withDetail("error", "Service unreachable")
                    .build();
        }
    }
}

