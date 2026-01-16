package com.gocomet.ridehailing.config;

import com.newrelic.api.agent.NewRelic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
@Slf4j
public class NewRelicConfig {

    @PostConstruct
    public void init() {
        try {
            // Add custom attributes for better monitoring
            NewRelic.addCustomParameter("application", "GoComet-Ride-Hailing");
            NewRelic.addCustomParameter("version", "1.0.0");
            
            log.info("New Relic monitoring initialized successfully");
        } catch (Exception e) {
            log.warn("Failed to initialize New Relic: {}", e.getMessage());
        }
    }
}
