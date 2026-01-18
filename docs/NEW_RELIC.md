# New Relic Setup & Configuration

## Overview

New Relic is integrated into the GoComet Ride Hailing application to provide comprehensive application performance monitoring (APM), database query monitoring, transaction tracing, and error tracking.

## What is New Relic?

New Relic is an observability platform that provides:
- **Application Performance Monitoring (APM)** - Track API latencies, response times, throughput
- **Database Monitoring** - Monitor slow queries, query performance
- **Transaction Tracing** - Detailed traces of requests through the application
- **Error Tracking** - Capture and analyze application errors
- **Custom Metrics** - Track business-specific metrics
- **Alerts** - Set up alerts for performance issues

## Setup Steps

### 1. Create New Relic Account

1. Go to https://newrelic.com
2. Click "Sign up" or "Start free"
3. Complete the registration process
4. Verify your email address

### 2. Get License Key

**Option A: During Onboarding**
1. On the "Tell us about your stack" page
2. Select "Java" in Application monitoring section
3. Click "Generate and copy license key"
4. Copy the license key immediately

**Option B: From Account Settings**
1. Go to https://one.newrelic.com
2. Click your profile icon (top right)
3. Select "Account settings"
4. Navigate to "API keys" or "License keys"
5. Copy your license key (starts with `NRAL-...`)

### 3. Download New Relic Java Agent

1. Download the agent:
   ```bash
   cd /path/to/project
   mkdir -p newrelic
   cd newrelic
   curl -L -o newrelic-java.zip "https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip"
   unzip newrelic-java.zip
   ```

2. The agent files will be in `newrelic/newrelic/`:
   - `newrelic.jar` - Java agent JAR file
   - `newrelic.yml` - Configuration file

### 4. Configure New Relic Agent

Edit `newrelic/newrelic/newrelic.yml`:

```yaml
common: &default_settings
  license_key: 'YOUR_LICENSE_KEY_HERE'
  app_name: GoComet-Ride-Hailing
  agent_enabled: true
  # ... other settings
```

**Key Settings**:
- `license_key`: Your New Relic license key
- `app_name`: Application name as it appears in New Relic
- `agent_enabled`: Set to `true` to enable the agent

### 5. Configure Application

#### Option A: Using .env File (Recommended)

Create `.env` file in project root:
```bash
NEW_RELIC_LICENSE_KEY=your-license-key-here
NEW_RELIC_ACCOUNT_ID=your-account-id
```

#### Option B: Environment Variables

Set environment variables:
```bash
export NEW_RELIC_LICENSE_KEY=your-license-key-here
export NEW_RELIC_ACCOUNT_ID=your-account-id
```

### 6. Update Dockerfile

The Dockerfile is already configured to use New Relic:

```dockerfile
# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Add New Relic agent
COPY newrelic/newrelic/newrelic.jar /app/newrelic.jar
COPY newrelic/newrelic/newrelic.yml /app/newrelic.yml

COPY --from=build /app/target/ride-hailing-1.0.0.jar app.jar

EXPOSE 8080

# With New Relic agent
ENTRYPOINT ["java", "-javaagent:/app/newrelic.jar", "-jar", "app.jar"]
```

### 7. Update docker-compose.yml

The `docker-compose.yml` is configured to use the `.env` file:

```yaml
backend:
  env_file:
    - .env
  environment:
    NEW_RELIC_LICENSE_KEY: ${NEW_RELIC_LICENSE_KEY:-}
    NEW_RELIC_ACCOUNT_ID: ${NEW_RELIC_ACCOUNT_ID:-}
```

### 8. Application Configuration

The `application.yml` includes New Relic configuration:

```yaml
newrelic:
  config:
    app_name: GoComet-Ride-Hailing
    license_key: ${NEW_RELIC_LICENSE_KEY:your-license-key}
    log_level: info
    distributed_tracing:
      enabled: true
    datastore_tracer:
      database_statement_reporting:
        enabled: true
        record_sql: obfuscated
      slow_sql:
        enabled: true
        threshold_ms: 100
    transaction_tracer:
      enabled: true
      record_sql: obfuscated
      explain_threshold: 0.5
    error_collector:
      enabled: true
```

## Custom Instrumentation

### @Trace Annotation

The application uses `@Trace` annotations on controllers and services:

```java
@Trace(dispatcher = true)
@GetMapping("/v1/rides/{id}")
public ResponseEntity<ApiResponse<RideResponse>> getRide(@PathVariable Long id) {
    // ...
}
```

**Benefits**:
- Custom transaction naming
- Detailed method-level tracing
- Performance metrics per method

### NewRelicConfig.java

Custom configuration class that sets up monitoring:

```java
@Configuration
@Slf4j
public class NewRelicConfig {
    @PostConstruct
    public void init() {
        NewRelic.addCustomParameter("application", "GoComet-Ride-Hailing");
        NewRelic.addCustomParameter("version", "1.0.0");
        // ... more custom parameters
    }
}
```

## Verification

### 1. Check Agent Connection

After starting the application, check logs:
```bash
docker-compose logs backend | grep -i "newrelic"
```

You should see:
```
New Relic Agent v9.0.0 has started
Agent connected to collector.newrelic.com:443
Reporting to: https://rpm.newrelic.com/accounts/XXXXX/applications/XXXXX
```

### 2. Check New Relic Dashboard

1. Go to https://one.newrelic.com
2. Click "APM & Services" in left sidebar
3. Look for "GoComet-Ride-Hailing" application
4. Click on it to see detailed metrics

### 3. Generate Traffic

Make some API calls to generate data:
```bash
curl http://localhost:8080/v1/rides
curl http://localhost:8080/actuator/health
```

Wait 1-2 minutes for data to appear in New Relic.

## What You'll See in New Relic

### 1. Application Overview
- Response time graphs
- Throughput (requests per minute)
- Error rate
- Apdex score

### 2. Transaction Details
- Individual API endpoint performance
- Slowest transactions
- Transaction traces
- Database query times

### 3. Database Monitoring
- Slow queries (>100ms threshold)
- Query performance trends
- Database connection pool metrics
- Query execution plans

### 4. Error Tracking
- Error rates
- Error details and stack traces
- Error trends over time

### 5. Custom Metrics
- Business-specific metrics
- Custom attributes
- Performance indicators

## Configuration Options

### Slow Query Threshold

Configured in `application.yml`:
```yaml
datastore_tracer:
  slow_sql:
    enabled: true
    threshold_ms: 100  # Report queries slower than 100ms
```

### Transaction Tracing

Configured in `application.yml`:
```yaml
transaction_tracer:
  enabled: true
  record_sql: obfuscated  # obfuscated, raw, or off
  explain_threshold: 0.5  # Explain queries taking > 500ms
```

### Distributed Tracing

Enabled for microservices communication:
```yaml
distributed_tracing:
  enabled: true
```

## Setting Up Alerts

### 1. Navigate to Alerts

1. Go to https://one.newrelic.com
2. Click "Alerts & AI" in left sidebar
3. Click "Alert conditions"

### 2. Create Alert Policy

1. Click "New alert policy"
2. Name it (e.g., "GoComet Performance Alerts")
3. Add notification channels (email, Slack, etc.)

### 3. Create Alert Conditions

**Example: High Response Time Alert**
1. Click "New alert condition"
2. Select "APM" → "GoComet-Ride-Hailing"
3. Select metric: "Response time (web)"
4. Set threshold: > 1000ms for 5 minutes
5. Add to policy

**Example: High Error Rate Alert**
1. Click "New alert condition"
2. Select "APM" → "GoComet-Ride-Hailing"
3. Select metric: "Error rate"
4. Set threshold: > 5% for 5 minutes
5. Add to policy

**Example: Slow Database Query Alert**
1. Click "New alert condition"
2. Select "APM" → "GoComet-Ride-Hailing"
3. Select metric: "Database time"
4. Set threshold: > 500ms for 5 minutes
5. Add to policy

## Troubleshooting

### Agent Not Connecting

**Symptoms**:
- No data in New Relic dashboard
- Logs show connection errors

**Solutions**:
1. Verify license key is correct
2. Check network connectivity
3. Verify agent JAR is in Docker image
4. Check Docker logs for errors

### No Data Appearing

**Symptoms**:
- Agent connected but no metrics

**Solutions**:
1. Generate some traffic (make API calls)
2. Wait 2-3 minutes for data to appear
3. Check application is processing requests
4. Verify `@Trace` annotations are present

### High Overhead

**Symptoms**:
- Application performance degraded

**Solutions**:
1. Adjust sampling rate
2. Disable unnecessary features
3. Optimize transaction tracing
4. Review slow query threshold

## Best Practices

1. **License Key Security**
   - Never commit license key to Git
   - Use environment variables
   - Add `.env` to `.gitignore`

2. **Monitoring Strategy**
   - Monitor critical endpoints
   - Set up alerts for important metrics
   - Review slow queries regularly

3. **Performance Impact**
   - Monitor agent overhead
   - Adjust sampling if needed
   - Use async instrumentation where possible

4. **Data Retention**
   - Understand data retention policies
   - Export important data if needed
   - Set up dashboards for key metrics

## Useful Links

- **New Relic Dashboard**: https://one.newrelic.com
- **Documentation**: https://docs.newrelic.com
- **Java Agent Docs**: https://docs.newrelic.com/docs/agents/java-agent/
- **API Keys**: https://one.newrelic.com/admin-portal/api-keys/home

## Summary

New Relic is fully integrated and configured to monitor:
- ✅ API endpoint latencies
- ✅ Database query performance
- ✅ Slow queries (>100ms)
- ✅ Transaction traces
- ✅ Error rates
- ✅ Custom metrics

The agent is automatically started with the application and begins reporting data immediately after connection.
