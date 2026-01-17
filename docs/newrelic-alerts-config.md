# New Relic Alerts Configuration Guide

This guide explains how to set up alerts in New Relic for monitoring API performance and identifying bottlenecks.

## Prerequisites

1. New Relic account (100GB free tier available)
2. License key configured in `application.yml` or environment variable `NEW_RELIC_LICENSE_KEY`
3. Java agent installed and running (see `README.md` for setup)

## Setting Up Alerts in New Relic UI

### 1. API Response Time Alerts

**Alert Name:** Slow API Response Time  
**Condition:** Average response time > 1 second  
**Threshold:** 1 second  
**Duration:** 5 minutes  

**Steps:**
1. Go to **Alerts & AI** → **Alert Conditions** → **New Alert Condition**
2. Select **Application** → **GoComet-Ride-Hailing**
3. Select metric: **WebTransaction/Java/** (all endpoints)
4. Set threshold: **> 1000ms** (1 second)
5. Set duration: **5 minutes**
6. Add notification channels (email, Slack, PagerDuty)

**Recommended Alerts:**
- **Critical:** p95 response time > 2s
- **Warning:** p95 response time > 1s
- **Info:** p99 response time > 3s

### 2. Database Query Performance Alerts

**Alert Name:** Slow Database Queries  
**Condition:** Database query duration > 100ms  
**Threshold:** 100ms  
**Duration:** 5 minutes  

**Steps:**
1. Go to **Alerts & AI** → **Alert Conditions** → **New Alert Condition**
2. Select **Application** → **GoComet-Ride-Hailing**
3. Select metric: **Datastore/statement/PostgreSQL/** (all queries)
4. Set threshold: **> 100ms**
5. Set duration: **5 minutes**

**Recommended Alerts:**
- **Critical:** Query duration > 500ms
- **Warning:** Query duration > 100ms
- **Info:** Slow query count > 10 per minute

### 3. Error Rate Alerts

**Alert Name:** High Error Rate  
**Condition:** Error rate > 1%  
**Threshold:** 1%  
**Duration:** 5 minutes  

**Steps:**
1. Go to **Alerts & AI** → **Alert Conditions** → **New Alert Condition**
2. Select **Application** → **GoComet-Ride-Hailing**
3. Select metric: **Errors/all**
4. Set threshold: **> 1%** of total requests
5. Set duration: **5 minutes**

### 4. Database Connection Pool Alerts

**Alert Name:** Database Connection Pool Exhaustion  
**Condition:** Connection pool usage > 90%  
**Threshold:** 90%  
**Duration:** 3 minutes  

**Steps:**
1. Go to **Alerts & AI** → **Alert Conditions** → **New Alert Condition**
2. Select **Application** → **GoComet-Ride-Hailing**
3. Select metric: **Datastore/connectionPool/HikariCP/**
4. Set threshold: **> 90%** of max pool size (45/50 connections)
5. Set duration: **3 minutes**

### 5. Redis Performance Alerts

**Alert Name:** High Redis Latency  
**Condition:** Redis operation latency > 10ms  
**Threshold:** 10ms  
**Duration:** 5 minutes  

**Steps:**
1. Go to **Alerts & AI** → **Alert Conditions** → **New Alert Condition**
2. Select **Application** → **GoComet-Ride-Hailing**
3. Select metric: **Datastore/operation/Redis/**
4. Set threshold: **> 10ms**
5. Set duration: **5 minutes**

### 6. JVM Memory Alerts

**Alert Name:** High Memory Usage  
**Condition:** Heap usage > 85%  
**Threshold:** 85%  
**Duration:** 5 minutes  

**Steps:**
1. Go to **Alerts & AI** → **Alert Conditions** → **New Alert Condition**
2. Select **Application** → **GoComet-Ride-Hailing**
3. Select metric: **Memory/Heap/used**
4. Set threshold: **> 85%** of max heap
5. Set duration: **5 minutes**

## Alert Policy Template

Create a policy called **"GoComet-Ride-Hailing-Critical"** with these conditions:

1. ✅ API Response Time > 1s (Warning)
2. ✅ API Response Time > 2s (Critical)
3. ✅ Error Rate > 1% (Critical)
4. ✅ Database Query > 100ms (Warning)
5. ✅ Database Query > 500ms (Critical)
6. ✅ Connection Pool > 90% (Critical)
7. ✅ Memory Usage > 85% (Warning)
8. ✅ Memory Usage > 90% (Critical)

## NRQL Queries for Custom Dashboards

### API Latency by Endpoint
```sql
SELECT average(duration) as 'Avg Duration', 
       percentile(duration, 95) as 'p95',
       percentile(duration, 99) as 'p99'
FROM Transaction 
WHERE appName = 'GoComet-Ride-Hailing'
FACET name
TIMESERIES
```

### Slow Database Queries
```sql
SELECT average(duration) as 'Avg Duration',
       count(*) as 'Query Count'
FROM DatastoreStatement
WHERE appName = 'GoComet-Ride-Hailing'
  AND duration > 0.1
FACET statement
ORDER BY average(duration) DESC
LIMIT 20
```

### Error Rate by Endpoint
```sql
SELECT count(*) as 'Errors'
FROM TransactionError
WHERE appName = 'GoComet-Ride-Hailing'
FACET transactionName
TIMESERIES
```

### Throughput (Requests per Minute)
```sql
SELECT rate(count(*), 1 minute) as 'Requests/min'
FROM Transaction
WHERE appName = 'GoComet-Ride-Hailing'
TIMESERIES
```

## Automated Alert Setup (NRQL)

You can also create alerts using NRQL queries:

### Alert: Slow API Endpoints
```sql
SELECT average(duration) as 'avg_duration'
FROM Transaction
WHERE appName = 'GoComet-Ride-Hailing'
FACET name
HAVING avg_duration > 1.0
```

### Alert: Database Bottlenecks
```sql
SELECT average(duration) as 'avg_query_time'
FROM DatastoreStatement
WHERE appName = 'GoComet-Ride-Hailing'
FACET statement
HAVING avg_query_time > 0.1
```

## Notification Channels

Set up notification channels for alerts:

1. **Email:** Add team email addresses
2. **Slack:** Integrate Slack webhook
3. **PagerDuty:** For critical alerts
4. **SMS:** For on-call engineers

## Verification

After setting up alerts:

1. **Test Alerts:** Trigger a slow query or high error rate
2. **Verify Notifications:** Check that alerts are received
3. **Review Dashboards:** Ensure metrics are visible in New Relic UI
4. **Check Trace Data:** Verify distributed tracing is working

## Monitoring Checklist

- [ ] API latency tracking enabled (@Trace annotations)
- [ ] Database query monitoring enabled
- [ ] Slow query threshold set (100ms)
- [ ] Alerts configured for response time > 1s
- [ ] Alerts configured for slow queries > 100ms
- [ ] Error rate alerts configured (> 1%)
- [ ] Connection pool alerts configured (> 90%)
- [ ] Memory alerts configured (> 85%)
- [ ] Notification channels configured
- [ ] Dashboards created for key metrics

## Additional Resources

- [New Relic Alert Documentation](https://docs.newrelic.com/docs/alerts-applied-intelligence/)
- [New Relic NRQL Guide](https://docs.newrelic.com/docs/query-your-data/nrql-new-relic-query-language/)
- [New Relic Java Agent Setup](https://docs.newrelic.com/docs/apm/agents/java-agent/)
