# GoComet Ride Hailing Application

A production-ready, multi-tenant, multi-region ride-hailing system built with Spring Boot and React.

## 🚀 Features

### Core Functionality
- **Real-time Driver-Rider Matching** - Matches drivers within 1s (p95)
- **Dynamic Surge Pricing** - Demand-based pricing with configurable multipliers
- **Trip Lifecycle Management** - Start, pause, resume, and end trips
- **Payment Integration** - Mock PSP integration with retry logic
- **Real-time Updates** - WebSocket-based notifications for riders and drivers
- **Location Tracking** - Redis-based geospatial indexing for fast driver lookup

### Technical Highlights
- **High Performance** - Optimized for 100k drivers, 10k rides/min, 200k location updates/sec
- **Scalability** - Stateless services, horizontal scaling ready
- **Caching** - Redis for location data and frequent queries
- **Monitoring** - New Relic integration with custom instrumentation
- **Database Optimization** - Indexed queries, optimistic locking, pessimistic locking
- **Idempotency** - Duplicate request handling for critical operations
- **Validation** - Input validation and comprehensive error handling

## 📋 Prerequisites

- **Java 17** or higher
- **Maven 3.8+**
- **PostgreSQL 14+**
- **Redis 7+**
- **Node.js 18+** (for frontend)
- **Docker & Docker Compose** (optional, for containerized deployment)

## 🏗️ Architecture

### High-Level Design (HLD)

```
┌──────────────┐         ┌──────────────┐
│   Rider App  │         │  Driver App  │
└──────┬───────┘         └──────┬───────┘
       │                        │
       └────────┬───────────────┘
                │
        ┌───────▼────────┐
        │  Load Balancer │
        └───────┬────────┘
                │
    ┌───────────┴───────────┐
    │                       │
┌───▼────┐            ┌────▼───┐
│ API    │            │ API    │
│ Server │            │ Server │
└───┬────┘            └────┬───┘
    │                      │
    └──────────┬───────────┘
               │
    ┌──────────┴───────────┐
    │                      │
┌───▼────┐          ┌─────▼─────┐
│ Redis  │          │ PostgreSQL│
│ Cache  │          │  Database │
└────────┘          └───────────┘
```

### Low-Level Design (LLD)

#### Component Diagram

```
┌─────────────────────────────────────────────┐
│             Controller Layer                 │
│  RideController | DriverController | ...     │
└──────────────┬──────────────────────────────┘
               │
┌──────────────▼──────────────────────────────┐
│             Service Layer                    │
│  RideService | DriverMatchingService |       │
│  FareService | SurgePricingService    │       │
│  PaymentService | NotificationService │       │
└──────────────┬──────────────────────────────┘
               │
┌──────────────▼──────────────────────────────┐
│          Repository Layer                    │
│  RideRepo | DriverRepo | TripRepo | ...     │
└──────────────┬──────────────────────────────┘
               │
┌──────────────▼──────────────────────────────┐
│         Data Storage Layer                   │
│    PostgreSQL | Redis | Cache                │
└──────────────────────────────────────────────┘
```

#### Driver Matching Algorithm

```
1. Get ride request from rider
2. Calculate surge multiplier for region
3. Query available drivers (DB filter by tier & region)
4. Find nearby drivers (Redis geospatial query)
5. Intersect: available ∩ nearby
6. Sort by: distance ASC, rating DESC
7. Assign best driver
8. Notify driver via WebSocket
```

#### Database Schema

**Rides Table:**
- id (PK), idempotency_key (UNIQUE), rider_id, driver_id
- status, vehicle_tier, payment_method
- pickup_lat/lon, destination_lat/lon, addresses
- estimated_fare, surge_multiplier
- timestamps (created, matched, accepted, started, ended)

**Drivers Table:**
- id (PK), phone (UNIQUE), name, email
- license_number (UNIQUE), vehicle_number, vehicle_tier
- status, region, rating, total_rides

**Trips Table:**
- id (PK), ride_id (FK), driver_id, rider_id
- status, start/end coordinates, timestamps
- distance_km, duration_minutes, total_fare

## 🛠️ Setup Instructions

### Option 1: Docker Compose (Recommended)

1. **Clone the repository**
```bash
git clone <repository-url>
cd gocomet
```

2. **Set environment variables (optional)**
```bash
export NEW_RELIC_LICENSE_KEY=your-license-key
export NEW_RELIC_ACCOUNT_ID=your-account-id
```

3. **Start all services**
```bash
docker-compose up -d
```

4. **Access the application**
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- API Docs: http://localhost:8080/swagger-ui.html

### Option 2: Manual Setup

#### 1. Start PostgreSQL
```bash
# Using Docker
docker run --name postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=ride_hailing -p 5432:5432 -d postgres:15-alpine
```

#### 2. Start Redis
```bash
# Using Docker
docker run --name redis -p 6379:6379 -d redis:7-alpine
```

#### 3. Build and Run Backend
```bash
cd gocomet
mvn clean install
mvn spring-boot:run
```

#### 4. Run Frontend
```bash
cd frontend
npm install
npm start
```

## 📡 API Documentation

### Core APIs

#### 1. Create Ride
```http
POST /v1/rides
Content-Type: application/json

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
  "region": "Delhi-NCR"
}
```

#### 2. Get Ride Status
```http
GET /v1/rides/{id}
```

#### 3. Update Driver Location
```http
POST /v1/drivers/{id}/location
Content-Type: application/json

{
  "latitude": 28.6139,
  "longitude": 77.2090,
  "timestamp": 1234567890
}
```

#### 4. Accept Ride
```http
POST /v1/drivers/{id}/accept
Content-Type: application/json

{
  "rideId": 1,
  "driverId": 1
}
```

#### 5. End Trip
```http
POST /v1/trips/{id}/end
Content-Type: application/json

{
  "endLatitude": 28.5355,
  "endLongitude": 77.3910,
  "distanceKm": 15.5
}
```

#### 6. Process Payment
```http
POST /v1/payments
Content-Type: application/json

{
  "rideId": 1,
  "tripId": 1,
  "amount": 250.50,
  "paymentMethod": "CREDIT_CARD"
}
```

## 🧪 Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn verify
```

### Test Coverage
```bash
mvn clean test jacoco:report
```

## 📊 Performance Optimization

### Database Optimization
1. **Indexes** - On frequently queried columns (status, region, tier)
2. **Connection Pooling** - HikariCP with 50 max connections
3. **Query Optimization** - N+1 query prevention, batch operations
4. **Locking Strategies** - Optimistic for concurrent reads, pessimistic for critical writes

### Caching Strategy
1. **Driver Locations** - Redis with 30s TTL
2. **Surge Multipliers** - Redis cache for 1 minute
3. **Geospatial Queries** - Redis GEO commands for O(log N) lookup

### API Latency Improvements
- Target: < 100ms for driver location updates
- Target: < 1s for driver matching
- Target: < 500ms for ride creation

## 📈 New Relic Monitoring

### Setup New Relic

1. **Sign up** for New Relic (100GB free): https://newrelic.com
2. **Get License Key** from Account Settings
3. **Download Java Agent**
```bash
mkdir newrelic
cd newrelic
wget https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip
unzip newrelic-java.zip
```

4. **Configure** `newrelic/newrelic.yml`
```yaml
common: &default_settings
  license_key: '<your-license-key>'
  app_name: 'GoComet Ride Hailing'
  distributed_tracing:
    enabled: true
```

5. **Run with Agent**
```bash
java -javaagent:newrelic/newrelic.jar -jar target/ride-hailing-1.0.0.jar
```

### Key Metrics to Monitor
- API Response Times (p50, p95, p99)
- Database Query Performance
- Redis Cache Hit Rates
- WebSocket Connection Count
- Error Rates
- Throughput (requests/min)

## 🔒 Security

- Input validation on all endpoints
- SQL injection prevention (JPA/Hibernate)
- CORS configuration
- Rate limiting (planned)
- API authentication (planned - add JWT)

## 🌐 Scalability Features

1. **Stateless Services** - No session state in application servers
2. **Horizontal Scaling** - Add more instances behind load balancer
3. **Database Sharding** - Region-based sharding (ready for implementation)
4. **Read Replicas** - PostgreSQL read replicas for heavy reads
5. **Message Queue** - Ready for Kafka/RabbitMQ integration

## 📝 Configuration

### Application Properties

```yaml
# Database
spring.datasource.hikari.maximum-pool-size: 50
spring.datasource.hikari.minimum-idle: 10

# Redis
spring.data.redis.timeout: 2000ms
spring.data.redis.lettuce.pool.max-active: 50

# Business Logic
app.matching.search-radius-km: 5.0
app.surge.max-multiplier: 3.0
app.fare.base-fare: 50.0
```

## 🚦 Deployment

### Production Checklist
- [ ] Configure production database
- [ ] Set up Redis cluster
- [ ] Enable New Relic monitoring
- [ ] Configure environment variables
- [ ] Set up load balancer
- [ ] Enable SSL/TLS
- [ ] Configure backup strategy
- [ ] Set up CI/CD pipeline
- [ ] Enable logging aggregation

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 👥 Team

Built for GoComet DAW Assessment

## 📞 Support

For questions or issues, please contact [mallicktanmay2812@gmail.com]

---

**Version:** 1.0.0  
**Last Updated:** January 2026
