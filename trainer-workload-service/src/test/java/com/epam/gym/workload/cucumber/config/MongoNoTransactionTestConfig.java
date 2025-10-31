package com.epam.gym.workload.cucumber.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.SessionSynchronization;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

@TestConfiguration
public class MongoNoTransactionTestConfig {

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory factory, MappingMongoConverter converter) {
        MongoTemplate template = new MongoTemplate(factory, converter);
        // Disable any session-related operations
        template.setSessionSynchronization(SessionSynchronization.NEVER);
        return template;
    }
}
