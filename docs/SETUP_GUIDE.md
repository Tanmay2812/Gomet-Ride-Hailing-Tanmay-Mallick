# GoComet Ride Hailing - Setup & Running Guide

## Quick Start (5 minutes)

### Prerequisites Check
```bash
# Check Java version (need 17+)
java -version

# Check Maven
mvn -version

# Check Docker (if using Docker)
docker --version
docker-compose --version
```

## Method 1: Docker Compose (Easiest)

### Step 1: Clone Repository
```bash
git clone <your-repo-url>
cd gocomet
```

### Step 2: Start Everything
```bash
docker-compose up -d
```

### Step 3: Wait for Services
```bash
# Check status
docker-compose ps

# View logs
docker-compose logs -f backend
```

### Step 4: Access Application
- **Frontend:** http://localhost:3000
- **Backend API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html

### Stop Services
```bash
docker-compose down
```

## Method 2: Manual Setup (Recommended for Development)

### Step 1: Start Databases

#### PostgreSQL
```bash
docker run -d \
  --name gocomet-postgres \
  -e POSTGRES_DB=ride_hailing \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15-alpine
```

#### Redis
```bash
docker run -d \
  --name gocomet-redis \
  -p 6379:6379 \
  redis:7-alpine
```

### Step 2: Build Backend
```bash
cd /path/to/gocomet
mvn clean package -DskipTests
```

### Step 3: Run Backend
```bash
# Basic run
mvn spring-boot:run

# Or run JAR
java -jar target/ride-hailing-1.0.0.jar
```

Backend will start on: http://localhost:8080

### Step 4: Run Frontend
```bash
cd frontend
npm install
npm start
```

Frontend will start on: http://localhost:3000

## Verification Steps

### 1. Check Backend Health
```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

### 2. Check Database Connection
```bash
# Connect to PostgreSQL
docker exec -it gocomet-postgres psql -U postgres -d ride_hailing

# List tables
\dt

# Exit
\q
```

### 3. Check Redis
```bash
# Connect to Redis
docker exec -it gocomet-redis redis-cli

# Test
PING
# Should return: PONG

# Exit
exit
```

## Testing the Application

### Create Sample Driver
Use a database client or run SQL:

```sql
INSERT INTO drivers (name, phone_number, email, license_number, vehicle_number, vehicle_tier, status, region, rating, total_rides, created_at, updated_at)
VALUES ('John Doe', '+919876543210', 'john@example.com', 'DL1234567890', 'DL-01-AB-1234', 'ECONOMY', 'AVAILABLE', 'Delhi-NCR', 5.0, 0, NOW(), NOW());
```

### Create Sample Rider
```sql
INSERT INTO riders (name, phone_number, email, region, rating, total_rides, created_at, updated_at)
VALUES ('Jane Smith', '+919876543211', 'jane@example.com', 'Delhi-NCR', 5.0, 0, NOW(), NOW());
```

### Test Flow

1. **Go to Frontend:** http://localhost:3000

2. **Open Dashboard Tab:**
   - You'll see real-time ride updates here

3. **Go to Request Ride Tab:**
   - Use preset location or enter custom
   - Click "Request Ride"
   - Watch Dashboard update in real-time

4. **Go to Driver Panel Tab:**
   - Set Driver ID to 1
   - Update location (should be near pickup location)
   - Wait for ride request to appear
   - Click "Accept Ride"
   - Click "Start Trip"
   - Click "End Trip"

5. **Check Dashboard:**
   - See the complete ride lifecycle in real-time

## API Testing with cURL

### Create a Ride
```bash
curl -X POST http://localhost:8080/v1/rides \
  -H "Content-Type: application/json" \
  -d '{
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
  }'
```

### Get Ride Status
```bash
curl http://localhost:8080/v1/rides/1
```

### Update Driver Location
```bash
curl -X POST http://localhost:8080/v1/drivers/1/location \
  -H "Content-Type: application/json" \
  -d '{
    "latitude": 28.6139,
    "longitude": 77.2090
  }'
```

## Load Testing

### Using Apache Bench
```bash
# Test driver location updates (200k/sec target)
ab -n 10000 -c 100 -p location.json -T application/json \
  http://localhost:8080/v1/drivers/1/location
```

### location.json
```json
{
  "latitude": 28.6139,
  "longitude": 77.2090
}
```

## Troubleshooting

### Backend won't start

**Issue:** Port 8080 already in use
```bash
# Find process
lsof -i :8080

# Kill process
kill -9 <PID>
```

**Issue:** Database connection failed
```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Restart PostgreSQL
docker restart gocomet-postgres
```

**Issue:** Redis connection failed
```bash
# Check Redis is running
docker ps | grep redis

# Restart Redis
docker restart gocomet-redis
```

### Frontend won't start

**Issue:** Port 3000 already in use
```bash
# Kill process on port 3000
lsof -i :3000
kill -9 <PID>

# Or use different port
PORT=3001 npm start
```

**Issue:** WebSocket connection failed
- Check backend is running on http://localhost:8080
- Check CORS settings
- Verify WebSocket endpoint: http://localhost:8080/ws

### Database Issues

**Reset Database:**
```bash
# Stop backend
# Drop and recreate database
docker exec -it gocomet-postgres psql -U postgres -c "DROP DATABASE ride_hailing;"
docker exec -it gocomet-postgres psql -U postgres -c "CREATE DATABASE ride_hailing;"

# Restart backend (will auto-create tables)
```

### Redis Issues

**Clear Redis Cache:**
```bash
docker exec -it gocomet-redis redis-cli FLUSHALL
```

## Performance Monitoring

### Enable New Relic

1. Sign up at https://newrelic.com
2. Get license key
3. Set environment variable:
```bash
export NEW_RELIC_LICENSE_KEY=your-key-here
```

4. Restart backend

### View Metrics

**JVM Metrics:**
```bash
curl http://localhost:8080/actuator/metrics
```

**Specific Metric:**
```bash
curl http://localhost:8080/actuator/metrics/http.server.requests
```

## Development Tips

### Hot Reload Backend
```bash
mvn spring-boot:run -Dspring-boot.run.fork=false
```

### Frontend Development Mode
```bash
cd frontend
npm start
# Automatically reloads on file changes
```

### View Logs
```bash
# Backend logs
tail -f logs/application.log

# Docker logs
docker-compose logs -f backend
docker-compose logs -f frontend
```

## Production Deployment

### Build Production JARs
```bash
mvn clean package -Pprod
```

### Build Production Docker Images
```bash
docker build -t gocomet-backend:latest .
cd frontend && docker build -t gocomet-frontend:latest .
```

### Environment Variables for Production
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/ride_hailing
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=secure_password
export SPRING_DATA_REDIS_HOST=prod-redis
export NEW_RELIC_LICENSE_KEY=your-prod-key
```

## Next Steps

1. ✅ Complete setup
2. ✅ Test basic flow
3. ✅ Check real-time updates
4. Configure New Relic monitoring
5. Run load tests
6. Review performance metrics
7. Optimize as needed

## Support

If you encounter issues:
1. Check the logs
2. Verify all services are running
3. Check database connectivity
4. Review application.yml configuration
5. Consult README.md for detailed information

---

**Happy Coding! 🚀**
