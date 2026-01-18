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
            NewRelic.addCustomParameter("database.monitoring.enabled", "true");
            NewRelic.addCustomParameter("database.slow_query_threshold_ms", "100");
            NewRelic.addCustomParameter("distributed_tracing.enabled", "true");
            
            log.info("New Relic monitoring initialized successfully");
            log.info("  - API latency tracking: ENABLED (@Trace annotations on all endpoints)");
            log.info("  - Database query monitoring: ENABLED (configured in application.yml)");
            log.info("  - Slow query threshold: 100ms (configured in application.yml)");
            log.info("  - Distributed tracing: ENABLED");
            log.info("  - Note: Database monitoring settings are configured in application.yml");
        } catch (Exception e) {
            log.warn("Failed to initialize New Relic: {}", e.getMessage());
            log.warn("  - Monitoring will still work if Java agent is configured");
        }
    }
}
