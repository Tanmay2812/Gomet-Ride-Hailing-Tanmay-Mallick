# High-Level Design (HLD) Document
## GoComet Ride-Hailing Platform

**Version:** 1.0  
**Date:** 2026  
**Author:** Tanmay Mallick

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [System Overview](#system-overview)
3. [Architecture Design](#architecture-design)
4. [Technology Stack & Rationale](#technology-stack--rationale)
5. [Design Decisions & Tradeoffs](#design-decisions--tradeoffs)
6. [System Components](#system-components)
7. [Data Flow](#data-flow)
8. [Scalability & Performance](#scalability--performance)
9. [Reliability & Fault Tolerance](#reliability--fault-tolerance)
10. [Security Considerations](#security-considerations)

---

## Executive Summary

GoComet is a multi-tenant, multi-region ride-hailing platform designed to handle:
- **~100,000 drivers** across multiple regions
- **~10,000 ride requests per minute** at peak
- **~200,000 location updates per second** from drivers
- **Sub-second driver matching** (p95 < 1s)
- **Real-time updates** for riders and drivers

This document outlines the high-level architecture, technology choices, and design tradeoffs.

---

## System Overview

### Core Requirements

1. **Driver-Rider Matching**: Match riders with nearby available drivers within 1 second (p95)
2. **Real-time Location Tracking**: Handle 1-2 location updates per second per driver
3. **Dynamic Surge Pricing**: Calculate surge multipliers based on demand
4. **Trip Lifecycle Management**: Handle ride states (REQUESTED → MATCHED → ACCEPTED → STARTED → COMPLETED)
5. **Payment Processing**: Integrate with external Payment Service Providers (PSPs)
6. **Multi-region Support**: Support multiple geographic regions with region-local data
7. **Multi-tenant Support**: Support multiple tenants/organizations

### Non-Functional Requirements

- **Latency**: API response time < 200ms (p95)
- **Availability**: 99.9% uptime
- **Scalability**: Horizontal scaling capability
- **Consistency**: Strong consistency for critical operations (payments, ride assignment)
- **Observability**: Full monitoring with New Relic integration

---

## Architecture Design

### Architecture Pattern: **Microservices-Ready Monolith**

We chose a **monolithic architecture** with clear service boundaries that can be split into microservices later if needed.

**Rationale:**
- **Simplicity**: Faster development and deployment for MVP
- **Lower Operational Overhead**: Single deployment unit, easier debugging
- **Performance**: No network latency between services
- **Cost-Effective**: Lower infrastructure costs initially
- **Future-Proof**: Service boundaries are clearly defined for future microservices migration

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         Frontend (React)                        │
│  - Dashboard | Ride Request | Driver Panel | Rider/Driver     │
│  - WebSocket Client for Real-time Updates                      │
└──────────────────────┬──────────────────────────────────────────┘
                       │ HTTPS / WebSocket
                       │
┌──────────────────────▼──────────────────────────────────────────┐
│                    API Gateway / Load Balancer                  │
│                    (Nginx / Cloud Load Balancer)                │
└──────────────────────┬──────────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────────┐
│              Backend Application (Spring Boot)                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  REST APIs   │  │  WebSocket  │  │  Services    │          │
│  │  /v1/rides  │  │  /ws        │  │  Layer       │          │
│  │  /v1/drivers│  │             │  │              │          │
│  │  /v1/trips  │  │             │  │              │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└──────┬──────────────────┬──────────────────┬──────────────────┘
       │                  │                  │
       │                  │                  │
┌──────▼──────┐  ┌───────▼───────┐  ┌──────▼──────┐
│ PostgreSQL  │  │     Redis     │  │  External   │
│ (Primary DB)│  │   (Cache +    │  │   Payment   │
│             │  │   Geospatial) │  │   Gateway   │
│ - Rides     │  │               │  │             │
│ - Drivers   │  │ - Driver      │  │ - PSP APIs  │
│ - Riders    │  │   Locations   │  │             │
│ - Trips     │  │ - Surge Data  │  │             │
│ - Payments  │  │ - Rate Limits │  │             │
└─────────────┘  └───────────────┘  └─────────────┘
```

---

## Technology Stack & Rationale

### Backend Framework: **Spring Boot 3.2.1**

**Why Spring Boot?**
- ✅ **Mature Ecosystem**: Battle-tested framework with extensive community support
- ✅ **Rapid Development**: Auto-configuration, embedded server, production-ready features
- ✅ **Enterprise-Grade**: Built-in security, monitoring (Actuator), transaction management
- ✅ **Java 17**: Modern language features, excellent performance
- ✅ **Rich Ecosystem**: Spring Data JPA, Spring WebSocket, Spring Cache
- ✅ **Observability**: Easy integration with New Relic, Prometheus, etc.

**Tradeoffs:**
- ❌ Higher memory footprint compared to Go/Rust
- ❌ Slower startup time than native languages
- ✅ **Acceptable for our use case**: JVM warm-up is acceptable for long-running services

### Database: **PostgreSQL 15**

**Why PostgreSQL?**
- ✅ **ACID Compliance**: Strong consistency for critical operations (payments, ride assignment)
- ✅ **Rich Data Types**: JSONB for flexible schemas, geospatial extensions (PostGIS ready)
- ✅ **Advanced Features**: Full-text search, array types, custom functions
- ✅ **Reliability**: Proven track record, excellent backup/replication
- ✅ **Open Source**: No licensing costs
- ✅ **Scalability**: Read replicas, partitioning support

**Alternatives Considered:**
- **MongoDB**: ❌ Rejected - We need ACID transactions for payments
- **MySQL**: ✅ Considered - PostgreSQL has better JSON support and extensibility
- **Cassandra**: ❌ Rejected - Overkill for our scale, eventual consistency not suitable

**Tradeoffs:**
- ❌ Vertical scaling limits (mitigated by read replicas)
- ✅ **Acceptable**: Our write volume is manageable, reads can be scaled horizontally

### Cache & Geospatial Index: **Redis 7**

**Why Redis?**
- ✅ **In-Memory Performance**: Sub-millisecond latency for location lookups
- ✅ **Geospatial Data Structures**: Built-in GEO commands (GEOADD, GEORADIUS)
- ✅ **Pub/Sub Support**: Can be used for real-time notifications (alternative to WebSocket)
- ✅ **TTL Support**: Automatic expiration for stale location data
- ✅ **High Throughput**: Can handle 200k+ ops/sec on single instance
- ✅ **Persistence Options**: RDB snapshots, AOF for durability

**Alternatives Considered:**
- **Memcached**: ❌ Rejected - No geospatial support, no pub/sub
- **Hazelcast**: ✅ Considered - More complex, overkill for our needs
- **ElastiCache**: ✅ Future option - AWS managed Redis

**Tradeoffs:**
- ❌ Data loss risk if not configured with persistence (mitigated by TTL and DB backup)
- ❌ Memory cost for large datasets (mitigated by TTL expiration)
- ✅ **Acceptable**: Location data is ephemeral, can be regenerated

### Frontend: **React 18**

**Why React?**
- ✅ **Component-Based**: Modular, reusable UI components
- ✅ **Rich Ecosystem**: Extensive library ecosystem
- ✅ **Real-time Updates**: Easy WebSocket integration
- ✅ **Performance**: Virtual DOM, efficient re-rendering
- ✅ **Developer Experience**: Hot reload, excellent tooling
- ✅ **Industry Standard**: Large talent pool, extensive documentation

**Tradeoffs:**
- ❌ Bundle size (mitigated by code splitting)
- ❌ SEO limitations (not a concern for authenticated app)
- ✅ **Acceptable**: Modern browsers handle React apps efficiently

### Web Server: **Nginx**

**Why Nginx?**
- ✅ **Reverse Proxy**: Routes API calls to backend, serves static files
- ✅ **WebSocket Support**: Proxies WebSocket connections
- ✅ **Load Balancing**: Ready for horizontal scaling
- ✅ **Performance**: High concurrency, low memory footprint
- ✅ **SSL Termination**: Can handle HTTPS termination

---

## Design Decisions & Tradeoffs

### 1. Synchronous APIs vs Asynchronous APIs

**Decision: Synchronous REST APIs for Core Operations**

**Why Synchronous?**
1. **User Experience**: Users expect immediate feedback
   - Ride creation: User needs instant confirmation
   - Payment: User must know if payment succeeded/failed immediately
   - Driver acceptance: Rider needs to know if driver accepted

2. **Simplicity**: Easier to implement, debug, and test
   - No need for complex message queues
   - Straightforward error handling
   - Standard HTTP status codes

3. **Idempotency**: Can be handled with idempotency keys
   - Prevents duplicate operations
   - Retry-safe

4. **Transaction Management**: ACID transactions for critical operations
   - Payment processing requires strong consistency
   - Ride assignment must be atomic

**When We Use Asynchronous:**
- **Driver Matching**: Async background task (doesn't block user)
- **Notifications**: WebSocket for real-time updates (non-blocking)
- **Analytics**: Can be async (not user-facing)

**Tradeoffs:**
- ✅ **Pros**: Simpler, immediate feedback, easier error handling
- ❌ **Cons**: Blocking operations, potential timeout issues
- ✅ **Mitigation**: Async for long-running tasks, timeouts configured

**Alternative Considered: Event-Driven Architecture (Kafka)**
- ❌ **Rejected**: Overkill for our scale, adds complexity
- ❌ **Rejected**: Eventual consistency not suitable for payments
- ✅ **Future Considered**: For analytics, audit logs, cross-service communication

### 2. WebSocket vs Kafka vs Server-Sent Events (SSE)

**Decision: WebSocket for Real-time Updates**

**Why WebSocket?**
1. **Bidirectional Communication**: Both client and server can send messages
   - Server pushes ride status updates
   - Client can send commands (e.g., cancel ride)

2. **Low Latency**: Persistent connection, no HTTP overhead
   - Sub-100ms latency for updates
   - No polling overhead

3. **Efficient**: Single connection per client
   - Lower bandwidth than HTTP polling
   - Reduced server load

4. **Real-time Requirements**: Ride status changes need immediate notification
   - Driver matched → Notify rider instantly
   - Trip started → Update dashboard immediately
   - Payment completed → Real-time confirmation

**Why Not Kafka?**
1. **Different Use Case**: Kafka is for event streaming between services
   - We need client-server real-time communication
   - Kafka requires additional infrastructure (Zookeeper, brokers)
   - Kafka consumers are typically backend services, not browsers

2. **Complexity**: Kafka adds operational overhead
   - Need to manage Kafka cluster
   - Need WebSocket bridge service anyway
   - Overkill for our scale

3. **Latency**: Kafka introduces additional hop
   - Backend → Kafka → WebSocket Bridge → Client
   - Direct WebSocket: Backend → Client

**When Kafka Would Be Appropriate:**
- ✅ **Analytics Pipeline**: Stream ride events to analytics systems
- ✅ **Microservices Communication**: When we split into microservices
- ✅ **Event Sourcing**: For audit logs, event replay
- ✅ **Cross-Region Replication**: For multi-region data sync

**Why Not Server-Sent Events (SSE)?**
- ❌ **Unidirectional**: Only server → client
- ❌ **HTTP Overhead**: Less efficient than WebSocket
- ✅ **Considered**: For read-only updates, but WebSocket is more flexible

**Tradeoffs:**
- ✅ **Pros**: Low latency, bidirectional, efficient
- ❌ **Cons**: Connection management, stateful (harder to scale horizontally)
- ✅ **Mitigation**: Use sticky sessions (session affinity) for load balancing

### 3. Database: PostgreSQL vs NoSQL

**Decision: PostgreSQL (Relational Database)**

**Why PostgreSQL?**
1. **ACID Transactions**: Critical for payments and ride assignment
   - Payment processing must be atomic
   - Driver assignment must be consistent

2. **Complex Queries**: Need joins, aggregations, transactions
   - "Find all rides for a rider with driver details"
   - "Calculate total revenue by region"
   - "Get ride history with payment status"

3. **Data Integrity**: Foreign keys, constraints, referential integrity
   - Ensure driver exists before assigning to ride
   - Prevent orphaned records

4. **Mature Ecosystem**: Excellent tooling, monitoring, backup solutions

**Why Not NoSQL (MongoDB/Cassandra)?**
- ❌ **Eventual Consistency**: Not suitable for payments
- ❌ **No ACID Transactions**: Can't ensure atomicity across documents
- ❌ **Complex Queries**: Harder to do joins, aggregations
- ✅ **Considered**: For analytics, but PostgreSQL JSONB is sufficient

**Tradeoffs:**
- ✅ **Pros**: Strong consistency, ACID, rich querying
- ❌ **Cons**: Vertical scaling limits, schema changes require migrations
- ✅ **Mitigation**: Read replicas for scaling, careful schema design

### 4. Caching Strategy: Redis

**Decision: Redis for Location Cache + Geospatial Index**

**Why Redis?**
1. **Geospatial Queries**: Built-in GEO commands
   - `GEOADD`: Add driver locations
   - `GEORADIUS`: Find drivers within radius
   - Sub-millisecond query performance

2. **TTL Support**: Automatic expiration
   - Driver locations expire after 5 minutes (if not updated)
   - Prevents stale data

3. **High Throughput**: Can handle 200k+ location updates/sec
   - Single Redis instance sufficient for our scale
   - Can shard by region if needed

**Caching Strategy:**
- **Write-Through**: Update Redis immediately when driver location changes
- **TTL-Based Expiration**: Locations expire after 5 minutes
- **Fallback**: If Redis miss, query database (rare, only for offline drivers)

**Tradeoffs:**
- ✅ **Pros**: Fast, geospatial support, TTL
- ❌ **Cons**: Memory cost, data loss risk (mitigated by TTL)
- ✅ **Acceptable**: Location data is ephemeral

### 5. Driver Matching Algorithm

**Decision: Proximity + Rating + Tier Matching**

**Algorithm:**
1. **Filter by Status**: Only AVAILABLE drivers
2. **Filter by Region**: Drivers in same region as ride
3. **Filter by Vehicle Tier**: Match requested tier (ECONOMY, PREMIUM, LUXURY, SUV)
4. **Geospatial Query**: Find drivers within 5km radius (Redis GEO)
5. **Ranking**: Sort by distance (closest first), then by rating (highest first)
6. **Select Best**: Pick top driver

**Why This Approach?**
- ✅ **Fast**: Redis GEO query is O(log N)
- ✅ **Fair**: Considers distance and rating
- ✅ **Scalable**: Can handle 100k+ drivers

**Alternatives Considered:**
- **Round-Robin**: ❌ Unfair, doesn't consider distance
- **Pure Distance**: ✅ Considered, but rating adds fairness
- **Machine Learning**: ❌ Overkill, adds complexity

**Tradeoffs:**
- ✅ **Pros**: Fast, fair, simple
- ❌ **Cons**: Doesn't consider driver preferences, traffic conditions
- ✅ **Future Enhancement**: Add ML-based matching

### 6. Payment Processing: External PSP Integration

**Decision: Integrate with External Payment Service Providers (PSPs)**

**Why External PSPs?**
1. **Compliance**: PSPs handle PCI-DSS compliance
2. **Payment Methods**: Support multiple payment methods (cards, UPI, wallets)
3. **Risk Management**: PSPs handle fraud detection
4. **Global Reach**: Support international payments

**Architecture:**
- **Synchronous API Call**: Call PSP API, wait for response
- **Idempotency**: Use idempotency keys to prevent duplicate charges
- **Retry Logic**: Retry on network failures (with idempotency)
- **Status Tracking**: Store payment status in database

**Tradeoffs:**
- ✅ **Pros**: Compliance handled, multiple payment methods
- ❌ **Cons**: Dependency on external service, network latency
- ✅ **Mitigation**: Timeout handling, retry logic, fallback options

### 7. Multi-Region Architecture

**Decision: Region-Local Data with Shared Services**

**Architecture:**
- **Region-Local Databases**: Each region has its own PostgreSQL instance
- **Shared Redis**: Can be region-local or shared (depending on latency)
- **Stateless Backend**: Backend instances can serve any region
- **Region Routing**: Load balancer routes requests to appropriate region

**Why This Approach?**
1. **Data Locality**: Reduces latency for region-specific queries
2. **Compliance**: Data residency requirements (GDPR, etc.)
3. **Scalability**: Each region scales independently
4. **Fault Isolation**: Failure in one region doesn't affect others

**Tradeoffs:**
- ✅ **Pros**: Low latency, compliance, fault isolation
- ❌ **Cons**: Data synchronization complexity, cross-region queries
- ✅ **Mitigation**: Async replication for analytics, clear region boundaries

---

## System Components

### 1. API Layer (REST Controllers)

**Components:**
- `RideController`: Ride creation, status, cancellation
- `DriverController`: Driver location updates, ride acceptance
- `TripController`: Trip start, pause, end
- `PaymentController`: Payment processing
- `RiderController`: Rider management

**Responsibilities:**
- Request validation
- Authentication/Authorization (future)
- Response formatting
- Error handling

### 2. Service Layer

**Components:**
- `RideService`: Core ride business logic
- `DriverMatchingService`: Driver-rider matching algorithm
- `LocationCacheService`: Redis location management
- `SurgePricingService`: Dynamic pricing calculation
- `FareCalculationService`: Fare computation
- `PaymentService`: Payment processing
- `TripService`: Trip lifecycle management
- `NotificationService`: Real-time notifications

**Responsibilities:**
- Business logic
- Transaction management
- Service orchestration

### 3. Data Access Layer

**Components:**
- `RideRepository`: Ride data access
- `DriverRepository`: Driver data access
- `RiderRepository`: Rider data access
- `TripRepository`: Trip data access
- `PaymentRepository`: Payment data access

**Responsibilities:**
- Database queries
- Entity management
- Query optimization

### 4. Real-time Communication

**Components:**
- `WebSocketConfig`: WebSocket configuration
- `NotificationService`: Broadcasts updates via WebSocket

**Protocol:**
- STOMP over WebSocket
- Topics: `/topic/rides/{rideId}`, `/topic/drivers/{driverId}`

### 5. External Integrations

**Components:**
- Payment Gateway Integration (mock implementation)
- New Relic APM (monitoring)

---

## Data Flow

### Ride Creation Flow

```
1. Rider submits ride request (POST /v1/rides)
   ↓
2. RideService.createRide()
   - Generate idempotency key
   - Check for duplicates
   - Calculate surge pricing
   - Calculate estimated fare
   - Save ride to PostgreSQL (status: REQUESTED)
   ↓
3. Async driver matching (matchDriverAsync)
   - Update status to SEARCHING
   - Query Redis for nearby drivers (GEO query)
   - Filter by status, region, tier
   - Rank by distance + rating
   - Assign best driver
   ↓
4. Update ride status to MATCHED
   - Save to PostgreSQL
   - Broadcast via WebSocket
   ↓
5. Driver accepts ride (POST /v1/drivers/{id}/accept)
   - Update status to ACCEPTED
   - Update driver status to ON_RIDE
   - Broadcast via WebSocket
```

### Location Update Flow

```
1. Driver sends location update (POST /v1/drivers/{id}/location)
   ↓
2. LocationCacheService.updateDriverLocation()
   - Store in Redis (key: driver:location:{driverId})
   - Update geospatial index (GEOADD)
   - Set TTL (5 minutes)
   ↓
3. Location available for matching queries
```

### Payment Flow

```
1. Trip ends (POST /v1/trips/{id}/end)
   ↓
2. Calculate final fare
   ↓
3. Create payment record (status: PENDING)
   ↓
4. Call external PSP API (synchronous)
   ↓
5. Update payment status (SUCCESS/FAILED)
   ↓
6. Send notifications via WebSocket
```

---

## Scalability & Performance

### Horizontal Scaling Strategy

**Stateless Backend:**
- Backend instances are stateless
- Can scale horizontally behind load balancer
- Session affinity (sticky sessions) for WebSocket connections

**Database Scaling:**
- **Read Replicas**: Scale reads horizontally
- **Connection Pooling**: HikariCP for efficient connection management
- **Query Optimization**: Indexes on frequently queried columns

**Redis Scaling:**
- **Single Instance**: Sufficient for current scale (200k ops/sec)
- **Future**: Redis Cluster for sharding by region

### Performance Optimizations

1. **Caching:**
   - Redis for driver locations (sub-ms latency)
   - Spring Cache for frequently accessed data

2. **Async Processing:**
   - Driver matching runs asynchronously
   - Doesn't block ride creation API

3. **Database Indexes:**
   - Indexes on: riderId, driverId, status, region, createdAt
   - Composite indexes for common query patterns

4. **Connection Pooling:**
   - HikariCP for PostgreSQL connections
   - Jedis connection pool for Redis

5. **Geospatial Queries:**
   - Redis GEO commands (O(log N) complexity)
   - Pre-filtered by region and tier before geospatial query

### Performance Targets

- **API Latency**: < 200ms (p95)
- **Driver Matching**: < 1s (p95)
- **Location Updates**: < 50ms (p95)
- **WebSocket Latency**: < 100ms (p95)

---

## Reliability & Fault Tolerance

### Database Reliability

- **ACID Transactions**: Ensure data consistency
- **Connection Pooling**: Handle connection failures gracefully
- **Retry Logic**: Retry transient failures
- **Backup Strategy**: Regular database backups (future)

### Redis Reliability

- **TTL Expiration**: Prevents stale data
- **Fallback**: Query database if Redis unavailable
- **Persistence**: RDB snapshots for recovery (future)

### Service Reliability

- **Health Checks**: Actuator endpoints for monitoring
- **Circuit Breaker**: (Future) Prevent cascading failures
- **Graceful Degradation**: System continues with reduced functionality

### Error Handling

- **Global Exception Handler**: Consistent error responses
- **Idempotency**: Safe retries
- **Logging**: Comprehensive logging for debugging

---

## Security Considerations

### Current Implementation

- **Input Validation**: Jakarta Validation annotations
- **SQL Injection Prevention**: JPA parameterized queries
- **CORS Configuration**: Restricted to frontend domain

### Future Enhancements

- **Authentication**: JWT tokens, OAuth2
- **Authorization**: Role-based access control (RBAC)
- **Rate Limiting**: Prevent abuse
- **Encryption**: TLS for all communications
- **Data Encryption**: Encrypt sensitive data at rest
- **Audit Logging**: Track all sensitive operations

---

## Monitoring & Observability

### New Relic Integration

- **APM**: Application Performance Monitoring
- **Custom Metrics**: Track business metrics (rides created, matching time)
- **Alerts**: Set up alerts for slow APIs, errors
- **Transaction Tracing**: Trace requests across services

### Logging

- **Structured Logging**: JSON format for easy parsing
- **Log Levels**: DEBUG, INFO, WARN, ERROR
- **Correlation IDs**: Track requests across services

### Metrics

- **Application Metrics**: Via Spring Actuator
- **Custom Metrics**: Business KPIs
- **Infrastructure Metrics**: CPU, memory, disk (via New Relic)

---

## Future Enhancements

### Short-term (3-6 months)

1. **Authentication & Authorization**: JWT, RBAC
2. **Rate Limiting**: Prevent API abuse
3. **Database Read Replicas**: Scale reads
4. **Enhanced Monitoring**: More custom metrics

### Medium-term (6-12 months)

1. **Microservices Migration**: Split into services
2. **Event-Driven Architecture**: Kafka for analytics
3. **Machine Learning**: ML-based driver matching
4. **Multi-Region Deployment**: Full multi-region support

### Long-term (12+ months)

1. **GraphQL API**: More flexible querying
2. **Mobile Apps**: Native iOS/Android apps
3. **Advanced Analytics**: Real-time analytics pipeline
4. **AI Features**: Demand prediction, route optimization

---

## Conclusion

This architecture provides a solid foundation for a scalable, reliable ride-hailing platform. The technology choices balance simplicity, performance, and future extensibility. The system is designed to handle current scale requirements while providing a clear path for future growth.

**Key Strengths:**
- Simple, maintainable architecture
- Proven technology stack
- Clear service boundaries for future microservices
- Real-time capabilities with WebSocket
- Strong consistency for critical operations

**Areas for Future Improvement:**
- Microservices migration for independent scaling
- Event-driven architecture for analytics
- Advanced monitoring and alerting
- Enhanced security features

---

**Document Version:** 1.0  
**Last Updated:** 2026  
**Next Review:** Quarterly
