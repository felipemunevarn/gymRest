package com.epam.gym.health;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = "externalServiceHealthIndicator")
public class ExternalServiceHealthIndicator implements HealthIndicator {

    private final boolean isExternalServiceReachable = true;

    @Override
    @ReadOperation
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

