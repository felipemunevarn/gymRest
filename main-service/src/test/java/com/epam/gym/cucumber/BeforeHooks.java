package com.epam.gym.cucumber;

import io.cucumber.java.Before;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Slf4j
//@Component
public class BeforeHooks {

    @Autowired
    RequestMappingHandlerMapping handlerMapping;

    static boolean printed = false;

    @Before(order = 0)
    public void printEndpointsOnce() {
        if (printed) return;
        printed = true;
        log.info("==== Registered Spring MVC endpoints (main-service) ====");
        handlerMapping.getHandlerMethods().forEach((info, method) -> {
            log.info("{} -> {}", info, method.getMethod().toGenericString());
        });
        log.info("========================================================");
    }
}
