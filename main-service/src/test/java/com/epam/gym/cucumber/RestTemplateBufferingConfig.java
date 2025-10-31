package com.epam.gym.cucumber;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@TestConfiguration
public class RestTemplateBufferingConfig {
    @Bean
    RestTemplateCustomizer bufferRequests() {
        return (RestTemplate rt) ->
            rt.setRequestFactory(
                new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory()));
    }
}
