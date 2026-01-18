# Low-Level Design (LLD) Document
## GoComet Ride-Hailing Platform

**Version:** 1.0  
**Date:** 2026  
**Author:** Tanmay Mallick

---

## Table of Contents

1. [Overview](#overview)
2. [Database Schema Design](#database-schema-design)
3. [API Design](#api-design)
4. [Service Layer Design](#service-layer-design)
5. [Data Models](#data-models)
6. [Algorithm Details](#algorithm-details)
7. [Component Interactions](#component-interactions)
8. [Error Handling](#error-handling)
9. [Configuration](#configuration)

---

## Overview

This document provides detailed low-level design specifications for the GoComet ride-hailing platform, including database schemas, API contracts, service implementations, and algorithm details.

---

## Database Schema Design

### Entity Relationship Diagram

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   Rider     │         │    Ride     │         │   Driver    │
├─────────────┤         ├─────────────┤         ├─────────────┤
│ id (PK)     │◄──┐     │ id (PK)     │     ┌──►│ id (PK)     │
│ name        │   │     │ riderId(FK) │     │   │ name        │
│ phoneNumber │   │     │ driverId(FK)├─────┘   │ phoneNumber │
│ email       │   └─────┤ status      │         │ email       │
│ region      │         │ vehicleTier │         │ vehicleTier │
│ rating      │         │ pickupLoc   │         │ status      │
│ totalRides  │         │ destLoc      │         │ region      │
└─────────────┘         │ estimatedFare│        │ rating      │
                        │ idempotencyKey│       └─────────────┘
                        └──────┬────────┘
                               │
                        ┌──────▼────────┐
                        │     Trip      │
                        ├───────────────┤
                        │ id (PK)       │
                        │ rideId (FK)   │
                        │ status        │
                        │ startTime     │
                        │ endTime       │
                        │ distanceKm    │
                        │ totalFare     │
                        └──────┬────────┘
                               │
                        ┌──────▼────────┐
                        │   Payment     │
                        ├───────────────┤
                        │ id (PK)       │
                        │ tripId (FK)   │
                        │ amount        │
                        │ status        │
                        │ paymentMethod │
                        │ transactionId │
                        └───────────────┘
```

### Table: `riders`

```sql
CREATE TABLE riders (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    region VARCHAR(50) NOT NULL,
    rating DECIMAL(3,2) DEFAULT 5.0,
    total_rides INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_rider_region ON riders(region);
CREATE INDEX idx_rider_phone ON riders(phone_number);
```

### Table: `drivers`

```sql
CREATE TABLE drivers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    license_number VARCHAR(50) NOT NULL UNIQUE,
    vehicle_number VARCHAR(20) NOT NULL,
    vehicle_tier VARCHAR(20) NOT NULL, -- ECONOMY, PREMIUM, LUXURY, SUV
    status VARCHAR(20) NOT NULL, -- AVAILABLE, BUSY, ON_RIDE, OFFLINE
    region VARCHAR(50) NOT NULL,
    rating DECIMAL(3,2) DEFAULT 5.0,
    total_rides INTEGER DEFAULT 0,
    last_location_update TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_driver_status ON drivers(status);
CREATE INDEX idx_driver_vehicle_tier ON drivers(vehicle_tier);
CREATE INDEX idx_driver_region ON drivers(region);
CREATE INDEX idx_driver_status_tier_region ON drivers(status, vehicle_tier, region);
```

### Table: `rides`

```sql
CREATE TABLE rides (
    id BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    rider_id BIGINT NOT NULL REFERENCES riders(id),
    driver_id BIGINT REFERENCES drivers(id),
    status VARCHAR(20) NOT NULL, -- REQUESTED, SEARCHING, MATCHED, ACCEPTED, STARTED, COMPLETED, CANCELLED, FAILED
    vehicle_tier VARCHAR(20) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    pickup_latitude DECIMAL(10,8) NOT NULL,
    pickup_longitude DECIMAL(11,8) NOT NULL,
    pickup_address VARCHAR(500) NOT NULL,
    destination_latitude DECIMAL(10,8) NOT NULL,
    destination_longitude DECIMAL(11,8) NOT NULL,
    destination_address VARCHAR(500) NOT NULL,
    region VARCHAR(50) NOT NULL,
    tenant_id VARCHAR(100),
    estimated_fare DECIMAL(10,2),
    surge_multiplier DECIMAL(3,2) DEFAULT 1.0,
    failure_reason VARCHAR(500),
    matched_at TIMESTAMP,
    accepted_at TIMESTAMP,
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    version BIGINT DEFAULT 0, -- Optimistic locking
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ride_rider ON rides(rider_id);
CREATE INDEX idx_ride_driver ON rides(driver_id);
CREATE INDEX idx_ride_status ON rides(status);
CREATE INDEX idx_ride_created ON rides(created_at);
CREATE INDEX idx_ride_region ON rides(region);
CREATE INDEX idx_ride_idempotency ON rides(idempotency_key);
```

### Table: `trips`

```sql
CREATE TABLE trips (
    id BIGSERIAL PRIMARY KEY,
    ride_id BIGINT NOT NULL REFERENCES rides(id),
    driver_id BIGINT NOT NULL REFERENCES drivers(id),
    status VARCHAR(20) NOT NULL, -- STARTED, PAUSED, RESUMED, ENDED
    start_latitude DECIMAL(10,8) NOT NULL,
    start_longitude DECIMAL(11,8) NOT NULL,
    end_latitude DECIMAL(10,8),
    end_longitude DECIMAL(11,8),
    distance_km DECIMAL(10,2),
    duration_minutes INTEGER,
    paused_duration_seconds BIGINT DEFAULT 0,
    total_fare DECIMAL(10,2),
    surge_multiplier DECIMAL(3,2) DEFAULT 1.0,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_trip_ride ON trips(ride_id);
CREATE INDEX idx_trip_driver ON trips(driver_id);
CREATE INDEX idx_trip_status ON trips(status);
```

### Table: `payments`

```sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    trip_id BIGINT NOT NULL REFERENCES trips(id),
    ride_id BIGINT NOT NULL REFERENCES rides(id),
    rider_id BIGINT NOT NULL REFERENCES riders(id),
    driver_id BIGINT NOT NULL REFERENCES drivers(id),
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL, -- PENDING, SUCCESS, FAILED, REFUNDED
    payment_method VARCHAR(20) NOT NULL,
    transaction_id VARCHAR(255),
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    failure_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payment_trip ON payments(trip_id);
CREATE INDEX idx_payment_ride ON payments(ride_id);
CREATE INDEX idx_payment_status ON payments(status);
CREATE INDEX idx_payment_idempotency ON payments(idempotency_key);
```

---

## API Design

### REST API Endpoints

#### 1. Create Ride

**Endpoint:** `POST /v1/rides`

**Request:**
```json
{
  "riderId": 1,
  "pickupLatitude": 28.6139,
  "pickupLongitude": 77.2090,
  "pickupAddress": "Connaught Place, New Delhi",
  "destinationLatitude": 28.5355,
  "destinationLongitude": 77.3910,
  "destinationAddress": "Noida Sector 18",
  "vehicleTier": "ECONOMY",
  "paymentMethod": "CREDIT_CARD",
  "region": "Delhi-NCR",
  "idempotencyKey": "optional-key-123" // Optional
}
```

**Response:**
```json
{
  "success": true,
  "message": "Ride created successfully",
  "data": {
    "id": 45,
    "riderId": 1,
    "status": "REQUESTED",
    "vehicleTier": "ECONOMY",
    "estimatedFare": 366.73,
    "surgeMultiplier": 1.2,
    "region": "Delhi-NCR",
    "createdAt": "2026-01-15T10:30:00"
  }
}
```

**Status Codes:**
- `201 Created`: Ride created successfully
- `200 OK`: Duplicate request (idempotency)
- `400 Bad Request`: Validation error
- `500 Internal Server Error`: Server error

#### 2. Get Ride Status

**Endpoint:** `GET /v1/rides/{id}`

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 45,
    "riderId": 1,
    "driverId": 5,
    "status": "MATCHED",
    "vehicleTier": "ECONOMY",
    "estimatedFare": 366.73,
    "driverInfo": {
      "name": "Rajesh Kumar",
      "phoneNumber": "+919111111111",
      "vehicleNumber": "DL-01-AB-1111",
      "rating": 4.7
    }
  }
}
```

#### 3. Update Driver Location

**Endpoint:** `POST /v1/drivers/{id}/location`

**Request:**
```json
{
  "driverId": 1,
  "latitude": 28.6139,
  "longitude": 77.2090
}
```

**Response:**
```json
{
  "success": true,
  "message": "Location updated successfully"
}
```

#### 4. Accept Ride

**Endpoint:** `POST /v1/drivers/{id}/accept`

**Request:**
```json
{
  "rideId": 45,
  "driverId": 1
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 45,
    "status": "ACCEPTED",
    "acceptedAt": "2026-01-15T10:31:00"
  }
}
```

#### 5. End Trip

**Endpoint:** `POST /v1/trips/{id}/end`

**Request:**
```json
{
  "tripId": 12,
  "endLatitude": 28.5355,
  "endLongitude": 77.3910,
  "distanceKm": 15.5
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 12,
    "status": "ENDED",
    "totalFare": 450.00,
    "distanceKm": 15.5,
    "durationMinutes": 25
  }
}
```

#### 6. Process Payment

**Endpoint:** `POST /v1/payments`

**Request:**
```json
{
  "rideId": 45,
  "tripId": 12,
  "amount": 450.00,
  "paymentMethod": "CREDIT_CARD",
  "idempotencyKey": "payment-123"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 8,
    "status": "SUCCESS",
    "transactionId": "txn-abc123",
    "amount": 450.00
  }
}
```

### WebSocket API

**Connection:** `ws://localhost:8080/ws`

**STOMP Topics:**
- `/topic/rides/{rideId}`: Ride status updates
- `/topic/drivers/{driverId}`: Driver-specific updates
- `/topic/dashboard`: Dashboard updates

**Message Format:**
```json
{
  "type": "RIDE_STATUS_UPDATE",
  "rideId": 45,
  "status": "MATCHED",
  "driverId": 5,
  "timestamp": "2026-01-15T10:31:00"
}
```

---

## Service Layer Design

### RideService

**Class:** `com.gocomet.ridehailing.service.RideService`

**Key Methods:**

1. **createRide(CreateRideRequest request): RideResponse**
   - Generate/validate idempotency key
   - Check for duplicates
   - Calculate surge pricing
   - Calculate estimated fare
   - Save ride to database
   - Trigger async driver matching
   - Return response

2. **matchDriverAsync(Long rideId, CreateRideRequest request): CompletableFuture<Void>**
   - Update status to SEARCHING
   - Call DriverMatchingService.findBestDriver()
   - Assign driver if found
   - Update status to MATCHED or FAILED
   - Broadcast via WebSocket

3. **acceptRide(Long rideId, Long driverId): RideResponse**
   - Validate ride status (must be MATCHED)
   - Validate driver assignment
   - Update ride status to ACCEPTED
   - Update driver status to ON_RIDE
   - Send notifications

### DriverMatchingService

**Class:** `com.gocomet.ridehailing.service.DriverMatchingService`

**Algorithm:**
```java
public Optional<Driver> findBestDriver(
    Double pickupLat, 
    Double pickupLon, 
    VehicleTier vehicleTier, 
    String region
) {
    // Step 1: Get available drivers from DB (filtered by tier and region)
    List<Driver> availableDrivers = driverRepository
        .findAvailableDriversByTierAndRegion(
            DriverStatus.AVAILABLE, 
            vehicleTier, 
            region
        );
    
    // Step 2: Get nearby drivers from Redis (geospatial query)
    List<Long> nearbyDriverIds = locationCacheService
        .findNearbyDrivers(pickupLat, pickupLon, 5.0); // 5km radius
    
    // Step 3: Filter available drivers who are also nearby
    List<Long> candidateIds = availableDrivers.stream()
        .map(Driver::getId)
        .filter(nearbyDriverIds::contains)
        .limit(20) // Max 20 candidates
        .collect(Collectors.toList());
    
    // Step 4: Get locations for candidates
    Map<Long, Location> driverLocations = 
        locationCacheService.getDriverLocations(candidateIds);
    
    // Step 5: Rank by distance, then rating
    return candidateIds.stream()
        .filter(driverLocations::containsKey)
        .map(driverId -> {
            Driver driver = availableDrivers.stream()
                .filter(d -> d.getId().equals(driverId))
                .findFirst()
                .orElse(null);
            return driver;
        })
        .filter(Objects::nonNull)
        .min(Comparator
            .comparingDouble((Driver d) -> {
                Location loc = driverLocations.get(d.getId());
                return Location.calculateDistance(
                    pickupLat, pickupLon, 
                    loc.getLatitude(), loc.getLongitude()
                );
            })
            .thenComparing(
                Comparator.comparingDouble(Driver::getRating).reversed()
            )
        );
}
```

**Time Complexity:** O(N log N) where N = number of nearby drivers
**Space Complexity:** O(N)

### LocationCacheService

**Class:** `com.gocomet.ridehailing.service.LocationCacheService`

**Key Methods:**

1. **updateDriverLocation(Long driverId, Double lat, Double lon)**
   - Store in Redis: `driver:location:{driverId}` → Location object
   - Update geospatial index: `GEOADD location:index:all {lon} {lat} {driverId}`
   - Set TTL: 5 minutes (300 seconds)

2. **findNearbyDrivers(Double lat, Double lon, Double radiusKm): List<Long>**
   - Query Redis GEO: `GEORADIUS location:index:all {lon} {lat} {radius} km`
   - Return list of driver IDs within radius

3. **getDriverLocation(Long driverId): Optional<Location>**
   - Get from Redis: `GET driver:location:{driverId}`
   - Return Location object or empty

### SurgePricingService

**Class:** `com.gocomet.ridehailing.service.SurgePricingService`

**Algorithm:**
```java
public Double calculateSurgeMultiplier(String region) {
    // Get active rides in last 5 minutes
    Long activeRides = rideRepository.countActiveRidesByRegion(
        region, 
        List.of(RideStatus.REQUESTED, RideStatus.SEARCHING, RideStatus.MATCHED),
        LocalDateTime.now().minusMinutes(5)
    );
    
    // Base threshold: 100 rides per 5 minutes
    double baseThreshold = 100.0;
    
    // Calculate multiplier
    if (activeRides < baseThreshold) {
        return 1.0; // No surge
    } else if (activeRides < baseThreshold * 1.5) {
        return 1.2; // 20% surge
    } else if (activeRides < baseThreshold * 2.0) {
        return 1.5; // 50% surge
    } else {
        return 2.0; // 100% surge
    }
}
```

### FareCalculationService

**Class:** `com.gocomet.ridehailing.service.FareCalculationService`

**Fare Formula:**
```
Base Fare = Base Rate (₹50 for ECONOMY)
Distance Fare = Distance (km) × Rate per km (₹10/km)
Time Fare = Duration (minutes) × Rate per minute (₹1/min)
Surge Fare = (Base + Distance + Time) × Surge Multiplier
Total Fare = Surge Fare
```

**Implementation:**
```java
public Double calculateEstimatedFare(
    Double pickupLat, Double pickupLon,
    Double destLat, Double destLon,
    Double surgeMultiplier
) {
    // Calculate distance
    Double distanceKm = Location.calculateDistance(
        pickupLat, pickupLon, destLat, destLon
    );
    
    // Estimate duration (assuming 30 km/h average speed)
    Integer estimatedMinutes = (int) (distanceKm / 0.5); // 0.5 km/min = 30 km/h
    
    // Calculate base fare
    Double baseFare = 50.0; // Base rate
    Double distanceFare = distanceKm * 10.0; // ₹10/km
    Double timeFare = estimatedMinutes * 1.0; // ₹1/min
    
    // Apply surge
    Double totalFare = (baseFare + distanceFare + timeFare) * surgeMultiplier;
    
    return totalFare;
}
```

---

## Data Models

### Location DTO

```java
@Data
@Builder
public class Location {
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
    
    public static Double calculateDistance(
        Double lat1, Double lon1, 
        Double lat2, Double lon2
    ) {
        // Haversine formula
        final int R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * 
                   Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
}
```

### RideResponse DTO

```java
@Data
@Builder
public class RideResponse {
    private Long id;
    private Long riderId;
    private Long driverId;
    private RideStatus status;
    private VehicleTier vehicleTier;
    private PaymentMethod paymentMethod;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private String pickupAddress;
    private Double destinationLatitude;
    private Double destinationLongitude;
    private String destinationAddress;
    private Double estimatedFare;
    private Double surgeMultiplier;
    private String region;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime matchedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private DriverInfo driverInfo;
    
    @Data
    @Builder
    public static class DriverInfo {
        private String name;
        private String phoneNumber;
        private String vehicleNumber;
        private Double rating;
        private Location currentLocation;
    }
}
```

---

## Algorithm Details

### Driver Matching Algorithm

**Input:**
- Pickup location (latitude, longitude)
- Vehicle tier (ECONOMY, PREMIUM, LUXURY, SUV)
- Region (Delhi-NCR, Mumbai, etc.)

**Output:**
- Best matching driver (Optional<Driver>)

**Steps:**
1. **Database Query**: Get all AVAILABLE drivers matching tier and region
   - Query: `SELECT * FROM drivers WHERE status = 'AVAILABLE' AND vehicle_tier = ? AND region = ?`
   - Index: `idx_driver_status_tier_region`

2. **Geospatial Query**: Get drivers within 5km radius
   - Redis: `GEORADIUS location:index:all {lon} {lat} 5 km`
   - Returns: List of driver IDs

3. **Intersection**: Find drivers who are both available and nearby
   - Filter: `availableDrivers.filter(driver -> nearbyDriverIds.contains(driver.getId()))`

4. **Ranking**: Sort by distance, then rating
   - Distance: Calculate using Haversine formula
   - Rating: Higher is better
   - Comparator: `distance ASC, rating DESC`

5. **Selection**: Return best driver (closest with highest rating)

**Time Complexity:**
- Database query: O(N) where N = drivers in region
- Geospatial query: O(log N + M) where M = nearby drivers
- Ranking: O(M log M) where M = candidates
- **Total: O(N + M log M)**

**Optimization:**
- Limit candidates to top 20 before ranking
- Use database indexes for fast filtering
- Redis GEO for fast geospatial queries

### Surge Pricing Algorithm

**Input:**
- Region
- Time window (last 5 minutes)

**Output:**
- Surge multiplier (1.0 to 2.0)

**Formula:**
```
activeRides = count(rides WHERE status IN (REQUESTED, SEARCHING, MATCHED) 
                    AND region = ? 
                    AND createdAt > now() - 5 minutes)

if activeRides < 100:
    multiplier = 1.0
else if activeRides < 150:
    multiplier = 1.2
else if activeRides < 200:
    multiplier = 1.5
else:
    multiplier = 2.0
```

**Caching:**
- Cache surge multiplier for 1 minute
- Recalculate every minute

---

## Component Interactions

### Ride Creation Sequence

```
Client
  │
  ├─► POST /v1/rides
  │
  ▼
RideController
  │
  ├─► RideService.createRide()
  │   │
  │   ├─► Generate idempotency key
  │   ├─► RideRepository.findByIdempotencyKey()
  │   ├─► SurgePricingService.calculateSurgeMultiplier()
  │   ├─► FareCalculationService.calculateEstimatedFare()
  │   ├─► RideRepository.save()
  │   └─► matchDriverAsync() [ASYNC]
  │       │
  │       ├─► DriverMatchingService.findBestDriver()
  │       │   ├─► DriverRepository.findAvailableDriversByTierAndRegion()
  │       │   ├─► LocationCacheService.findNearbyDrivers()
  │       │   └─► LocationCacheService.getDriverLocations()
  │       │
  │       └─► RideRepository.save() [Update status]
  │
  └─► Return RideResponse
```

### Location Update Sequence

```
Client
  │
  ├─► POST /v1/drivers/{id}/location
  │
  ▼
DriverController
  │
  ├─► LocationCacheService.updateDriverLocation()
  │   │
  │   ├─► Redis SET driver:location:{id}
  │   └─► Redis GEOADD location:index:all
  │
  └─► Return success
```

### WebSocket Notification Flow

```
RideService
  │
  ├─► NotificationService.broadcastRideUpdate()
  │   │
  │   └─► SimpMessagingTemplate.convertAndSend()
  │       │
  │       └─► WebSocket: /topic/rides/{rideId}
  │           │
  │           └─► Client receives update
```

---

## Error Handling

### Exception Hierarchy

```
Exception
  │
  ├─► RideException
  │   ├─► RideNotFoundException
  │   ├─► RideStatusException
  │   └─► DriverMatchingException
  │
  ├─► DriverException
  │   ├─► DriverNotFoundException
  │   └─► DriverStatusException
  │
  ├─► TripException
  │   └─► TripStatusException
  │
  └─► PaymentException
      └─► PaymentFailedException
```

### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RideException.class)
    public ResponseEntity<ApiResponse<?>> handleRideException(RideException e) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(e.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Internal server error"));
    }
}
```

### Error Response Format

```json
{
  "success": false,
  "message": "Ride not found",
  "error": "RIDE_NOT_FOUND",
  "timestamp": "2026-01-15T10:30:00"
}
```

---

## Configuration

### Application Properties

```yaml
# Database
spring.datasource.url: jdbc:postgresql://localhost:5432/ride_hailing
spring.datasource.username: postgres
spring.datasource.password: postgres

# Redis
spring.data.redis.host: localhost
spring.data.redis.port: 6379
spring.data.redis.timeout: 2000ms

# Matching
app.matching.search-radius-km: 5.0
app.matching.max-drivers-to-consider: 20

# Location Cache
app.location-cache.ttl-seconds: 300

# Surge Pricing
app.surge-pricing.base-threshold: 100
app.surge-pricing.time-window-minutes: 5

# Async
spring.task.execution.pool.core-size: 5
spring.task.execution.pool.max-size: 10
spring.task.execution.pool.queue-capacity: 100
```

### Redis Configuration

**Keys:**
- `driver:location:{driverId}`: Location object (TTL: 5 min)
- `location:index:all`: Geospatial index (GEOADD)

**Commands:**
- `SET driver:location:1 "{lat, lon, timestamp}" EX 300`
- `GEOADD location:index:all {lon} {lat} {driverId}`
- `GEORADIUS location:index:all {lon} {lat} 5 km`

---

## Conclusion

This LLD document provides detailed specifications for implementing the GoComet ride-hailing platform. All components, algorithms, and interactions are clearly defined to enable consistent implementation across the development team.

**Key Design Principles:**
- **Separation of Concerns**: Clear service boundaries
- **Idempotency**: Safe retries for all operations
- **Performance**: Optimized queries, caching, async processing
- **Reliability**: Error handling, transaction management
- **Scalability**: Stateless services, horizontal scaling ready

---

**Document Version:** 1.0  
**Last Updated:** 2026  
**Next Review:** Quarterly
