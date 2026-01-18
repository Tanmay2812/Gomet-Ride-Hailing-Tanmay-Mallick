# Backend Documentation

## Overview

The GoComet Ride Hailing backend is a production-ready Spring Boot application built with Java 17. It provides RESTful APIs for managing rides, drivers, riders, trips, and payments with real-time capabilities, high performance, and scalability.

## Technology Stack

### Core Framework
- **Java 17** - Modern Java with records, pattern matching, and improved performance
- **Spring Boot 3.2.1** - Rapid application development framework
- **Spring Data JPA** - Database abstraction layer
- **Spring WebSocket** - Real-time bidirectional communication
- **Maven 3.9** - Build and dependency management

### Database & Caching
- **PostgreSQL 15** - Primary relational database for transactional data
- **Redis 7** - In-memory data store for:
  - Geospatial indexing (driver locations)
  - Caching frequently accessed data
  - Session management

### Libraries & Tools
- **Lombok** - Reduces boilerplate code (getters, setters, builders)
- **Jackson** - JSON serialization/deserialization
- **Guava** - Google's core libraries (rate limiting, utilities)
- **Apache Commons Lang** - String and object utilities
- **SpringDoc OpenAPI** - API documentation (Swagger UI)
- **New Relic API** - Application performance monitoring

### Testing
- **JUnit 5** - Unit testing framework
- **Mockito** - Mocking framework
- **Spring Boot Test** - Integration testing
- **Testcontainers** - Docker-based integration tests

## Architecture

### Project Structure

```
src/main/java/com/gocomet/ridehailing/
├── config/              # Configuration classes
│   ├── AsyncConfig.java
│   ├── NewRelicConfig.java
│   ├── RedisConfig.java
│   ├── WebConfig.java
│   └── WebSocketConfig.java
├── controller/          # REST API endpoints
│   ├── DriverController.java
│   ├── PaymentController.java
│   ├── RideController.java
│   ├── RiderController.java
│   └── TripController.java
├── exception/           # Custom exceptions and handlers
│   ├── DriverException.java
│   ├── GlobalExceptionHandler.java
│   ├── PaymentException.java
│   ├── RideException.java
│   ├── RiderException.java
│   └── TripException.java
├── model/
│   ├── dto/            # Data Transfer Objects
│   ├── entity/         # JPA entities
│   └── enums/          # Enumeration types
├── repository/         # Data access layer
│   ├── DriverRepository.java
│   ├── PaymentRepository.java
│   ├── RideRepository.java
│   ├── RiderRepository.java
│   └── TripRepository.java
└── service/            # Business logic layer
    ├── DriverMatchingService.java
    ├── DriverService.java
    ├── FareCalculationService.java
    ├── LocationCacheService.java
    ├── NotificationService.java
    ├── PaymentService.java
    ├── RiderService.java
    ├── RideService.java
    ├── SurgePricingService.java
    └── TripService.java
```

## Key Components

### 1. Controllers (REST API Layer)

#### RideController
- `POST /v1/rides` - Create a new ride request
- `GET /v1/rides/{id}` - Get ride details
- `GET /v1/rides` - Get all rides (with optional filters)
- `DELETE /v1/rides/{id}` - Cancel a ride

#### DriverController
- `GET /v1/drivers/{id}` - Get driver details
- `POST /v1/drivers` - Create a new driver
- `PUT /v1/drivers/{id}` - Update driver information
- `POST /v1/drivers/{id}/location` - Update driver location (real-time)
- `POST /v1/drivers/{id}/accept` - Accept a ride assignment
- `GET /v1/drivers/{id}/pending-rides` - Get pending ride requests

#### RiderController
- `GET /v1/riders/{id}` - Get rider details
- `POST /v1/riders` - Create a new rider
- `PUT /v1/riders/{id}` - Update rider information

#### TripController
- `POST /v1/trips/{id}/start` - Start a trip
- `POST /v1/trips/{id}/pause` - Pause a trip
- `POST /v1/trips/{id}/resume` - Resume a paused trip
- `POST /v1/trips/{id}/end` - End a trip and calculate fare

#### PaymentController
- `POST /v1/payments` - Process payment for a trip

### 2. Services (Business Logic Layer)

#### RideService
- Handles ride creation, acceptance, cancellation
- Manages ride state transitions
- Coordinates with driver matching service

#### DriverMatchingService
- Finds nearby available drivers using Redis geospatial queries
- Implements matching algorithm with distance calculation
- Filters by vehicle tier, status, and region
- Target: < 1s p95 latency

#### LocationCacheService
- Updates driver locations in Redis (GEO index)
- Retrieves nearby drivers within radius
- TTL management (5 minutes default)
- Handles 200k+ location updates/sec

#### SurgePricingService
- Calculates dynamic pricing based on demand
- Configurable multipliers (1.0x to 3.0x)
- Demand threshold-based pricing

#### FareCalculationService
- Calculates trip fare based on:
  - Base fare
  - Distance (per km rate)
  - Duration (per minute rate)
  - Surge multiplier
  - Minimum fare guarantee

#### PaymentService
- Processes payments via mock PSP
- Implements retry logic (3 attempts)
- Idempotency handling
- Payment status tracking

#### TripService
- Manages trip lifecycle (start, pause, resume, end)
- Tracks trip duration and paused time
- Calculates final fare

#### NotificationService
- Sends real-time WebSocket notifications
- Notifies riders and drivers of:
  - Ride matched
  - Driver arrived
  - Trip started/ended
  - Payment status

### 3. Data Models

#### Entities
- **Rider** - User requesting rides
- **Driver** - Driver providing rides
- **Ride** - Ride request/booking
- **Trip** - Active ride in progress
- **Payment** - Payment transaction

#### Enums
- `DriverStatus` - AVAILABLE, BUSY, OFFLINE
- `RideStatus` - PENDING, MATCHED, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED, FAILED
- `TripStatus` - NOT_STARTED, IN_PROGRESS, PAUSED, COMPLETED
- `VehicleTier` - ECONOMY, PREMIUM, LUXURY
- `PaymentMethod` - CREDIT_CARD, DEBIT_CARD, UPI, WALLET
- `PaymentStatus` - PENDING, SUCCESS, FAILED

### 4. Configuration

#### RedisConfig
- Configures RedisTemplate with custom serializers
- Sets up connection pooling
- Configures TTL for cached data

#### WebSocketConfig
- Configures STOMP over WebSocket
- Sets up message broker
- Configures CORS for frontend

#### WebConfig
- CORS configuration
- Allowed origins and methods
- Request/response interceptors

#### AsyncConfig
- Thread pool configuration for async operations
- Executor service setup

#### NewRelicConfig
- Custom New Relic parameters
- Monitoring configuration

## Implementation Details

### Database Design

#### Indexes
- `idx_rider_phone` - Fast rider lookup by phone
- `idx_rider_region` - Region-based queries
- `idx_driver_status` - Filter available drivers
- `idx_driver_vehicle_tier` - Tier-based filtering
- `idx_driver_region` - Region-based queries

#### Connection Pooling
- HikariCP with optimized settings:
  - Max pool size: 50
  - Min idle: 10
  - Connection timeout: 30s
  - Idle timeout: 10 minutes
  - Max lifetime: 30 minutes

### Performance Optimizations

1. **Redis Geospatial Indexing**
   - Uses Redis GEO commands for fast location queries
   - Manual distance calculation for filtering
   - TTL of 5 minutes for location data

2. **Database Query Optimization**
   - Indexed queries on frequently accessed columns
   - Batch operations where possible
   - Optimistic locking for concurrent updates

3. **Caching Strategy**
   - Redis for location data
   - Cache frequently accessed entities
   - TTL-based cache invalidation

4. **Async Processing**
   - Background tasks for non-critical operations
   - WebSocket notifications sent asynchronously
   - Payment retries handled asynchronously

### Security Features

1. **Input Validation**
   - Jakarta Validation annotations
   - Custom validators for business rules
   - Sanitization of user inputs

2. **Idempotency**
   - Idempotency keys for critical operations
   - Duplicate request detection
   - Idempotent payment processing

3. **Error Handling**
   - Global exception handler
   - Custom exception types
   - Consistent error response format

### Real-time Features

1. **WebSocket Integration**
   - STOMP protocol over WebSocket
   - Topic-based messaging
   - Real-time notifications for:
     - Ride status changes
     - Driver location updates
     - Payment status

2. **Location Updates**
   - High-frequency updates (1-2 per second)
   - Redis GEO index for fast lookups
   - Efficient distance calculations

## API Response Format

All APIs return a consistent response structure:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2026-01-18T12:00:00Z"
}
```

Error responses:

```json
{
  "success": false,
  "message": "Error description",
  "errors": [ ... ],
  "timestamp": "2026-01-18T12:00:00Z"
}
```

## Configuration Files

### application.yml
- Database connection settings
- Redis configuration
- Application-specific settings (matching radius, fare rates, etc.)
- New Relic configuration

### application-test.yml
- Test database configuration
- Test-specific settings

### application-prod.yml
- Production database configuration
- Production-specific optimizations

## Build & Run

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- PostgreSQL 14+
- Redis 7+

### Build
```bash
mvn clean package
```

### Run
```bash
java -jar target/ride-hailing-1.0.0.jar
```

### Run with New Relic
```bash
java -javaagent:newrelic/newrelic/newrelic.jar -jar target/ride-hailing-1.0.0.jar
```

## Testing

### Unit Tests
- Service layer tests with mocked dependencies
- Repository tests with in-memory database
- Controller tests with MockMvc

### Integration Tests
- Testcontainers for PostgreSQL
- Redis integration tests
- End-to-end API tests

### Run Tests
```bash
mvn test
```

## Monitoring & Observability

- **New Relic Integration**
  - Custom instrumentation with `@Trace` annotations
  - Database query monitoring
  - Slow query detection (>100ms)
  - Transaction tracing
  - Error tracking

- **Actuator Endpoints**
  - `/actuator/health` - Health check
  - `/actuator/metrics` - Application metrics
  - `/actuator/info` - Application information

## Scalability Considerations

1. **Stateless Design**
   - No session state in application
   - Horizontal scaling ready

2. **Database Optimization**
   - Indexed queries
   - Connection pooling
   - Query optimization

3. **Caching**
   - Redis for hot data
   - Reduces database load

4. **Async Processing**
   - Non-blocking operations
   - Background task processing

## Performance Targets

- **Driver Matching**: < 1s p95 latency
- **Location Updates**: 200k+ updates/sec
- **Ride Requests**: 10k requests/min
- **Concurrent Drivers**: 100k+ drivers

## API Documentation

Swagger UI available at:
```
http://localhost:8080/swagger-ui.html
```

## Development Workflow

1. **Local Development**
   - Run PostgreSQL and Redis locally
   - Use `application.yml` for configuration
   - Hot reload with Spring Boot DevTools

2. **Testing**
   - Write unit tests for new features
   - Integration tests for API endpoints
   - Performance tests for critical paths

3. **Deployment**
   - Build Docker image
   - Deploy with Docker Compose
   - Monitor with New Relic

## Best Practices Implemented

1. **Clean Architecture**
   - Separation of concerns
   - Dependency injection
   - Interface-based design

2. **Error Handling**
   - Global exception handler
   - Consistent error responses
   - Proper HTTP status codes

3. **Validation**
   - Input validation at API layer
   - Business rule validation in services
   - Database constraints

4. **Documentation**
   - Swagger/OpenAPI documentation
   - Code comments
   - README files

5. **Testing**
   - Unit tests for business logic
   - Integration tests for APIs
   - Test coverage > 80%
