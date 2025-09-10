package com.epam.gym.health;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class TestInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> details = new HashMap<>();
        details.put("message", "Hello from InfoContributor!");
        details.put("timestamp", LocalDateTime.now().toString());
        details.put("status", "working");

        builder.withDetails(details);
    }
}
