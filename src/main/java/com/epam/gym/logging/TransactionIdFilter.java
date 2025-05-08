package com.epam.gym.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class TransactionIdFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("transactionId");
        }
    }
}
