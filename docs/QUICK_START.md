# 🚀 GoComet Ride Hailing - Quick Start Guide

## Run in 2 Minutes

### Prerequisites
- Docker and Docker Compose installed
- Ports 3000, 8080, 5432, 6379 available

### Start the Application

```bash
# 1. Navigate to project directory
cd /Users/tanmay.mallick/Documents/gocomet

# 2. Start all services
docker-compose up -d

# 3. Wait for services to be ready (30 seconds)
docker-compose ps

# 4. Open your browser
# Frontend: http://localhost:3000
# API Docs: http://localhost:8080/swagger-ui.html
```

That's it! 🎉

## First Demo Flow

### 1. Add Sample Data
```bash
# Create a driver
docker exec -it gocomet-postgres psql -U postgres -d ride_hailing -c \
"INSERT INTO drivers (name, phone_number, email, license_number, vehicle_number, vehicle_tier, status, region, rating, total_rides, created_at, updated_at) VALUES ('John Driver', '+919876543210', 'john@driver.com', 'DL1234567890', 'DL-01-AB-1234', 'ECONOMY', 'AVAILABLE', 'Delhi-NCR', 5.0, 0, NOW(), NOW());"

# Create a rider
docker exec -it gocomet-postgres psql -U postgres -d ride_hailing -c \
"INSERT INTO riders (name, phone_number, email, region, rating, total_rides, created_at, updated_at) VALUES ('Jane Rider', '+919876543211', 'jane@rider.com', 'Delhi-NCR', 5.0, 0, NOW(), NOW());"
```

### 2. Test the Complete Flow

#### Open Frontend: http://localhost:3000

**Step 1: Go to "Dashboard" tab**
- Keep this tab open to see real-time updates

**Step 2: Go to "Request Ride" tab**
- Click any preset location button (e.g., "Delhi to Noida")
- Click "Request Ride"
- ✅ You'll see a success message
- 🔄 Dashboard will show the new ride in "REQUESTED" status

**Step 3: Go to "Driver Panel" tab**
- Enter Driver ID: 1
- Update location to: Latitude 28.6139, Longitude 77.2090
- Click "Update Location"
- ⏳ Wait a few seconds
- 🚗 A "Pending Ride Request" will appear
- Click "Accept Ride"
- ✅ Ride status changes to "ACCEPTED"

**Step 4: Complete the Trip**
- In Driver Panel, click "Start Trip"
- ✅ Trip status: "IN_PROGRESS"
- Click "End Trip"
- ✅ Trip completed!

**Step 5: Check Dashboard**
- See the complete ride lifecycle
- All status changes in real-time
- Final fare calculated

## API Testing

### Create Ride via API
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

## Useful Commands

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
```

### Check Service Status
```bash
docker-compose ps
```

### Stop Services
```bash
docker-compose down
```

### Restart a Service
```bash
docker-compose restart backend
docker-compose restart frontend
```

### Database Access
```bash
# Connect to PostgreSQL
docker exec -it gocomet-postgres psql -U postgres -d ride_hailing

# List tables
\dt

# Query rides
SELECT id, status, rider_id, driver_id FROM rides;

# Exit
\q
```

### Redis Access
```bash
# Connect to Redis
docker exec -it gocomet-redis redis-cli

# Get all driver location keys
KEYS driver:location:*

# Get specific driver location
GET driver:location:1

# Exit
exit
```

## Troubleshooting

### Services won't start
```bash
# Check if ports are in use
lsof -i :8080  # Backend
lsof -i :3000  # Frontend
lsof -i :5432  # PostgreSQL
lsof -i :6379  # Redis

# Stop conflicting services or change ports in docker-compose.yml
```

### Frontend can't connect to backend
```bash
# Check backend is running
curl http://localhost:8080/actuator/health

# Should return: {"status":"UP"}
```

### No drivers found
```bash
# Make sure driver data exists
docker exec -it gocomet-postgres psql -U postgres -d ride_hailing -c \
"SELECT * FROM drivers;"

# If empty, insert sample driver (see "Add Sample Data" section above)
```

### WebSocket not connecting
```bash
# Check WebSocket endpoint
curl http://localhost:8080/ws

# Restart backend
docker-compose restart backend
```

## Performance Testing

### Load Test Location Updates
```bash
# Install Apache Bench (if not already installed)
# macOS: brew install httpd
# Ubuntu: apt-get install apache2-utils

# Create test payload
echo '{"latitude":28.6139,"longitude":77.2090}' > location.json

# Run load test (10,000 requests, 100 concurrent)
ab -n 10000 -c 100 -p location.json -T application/json \
  http://localhost:8080/v1/drivers/1/location
```

## Key Features to Demo

### 1. Real-time Updates
- Open Dashboard
- Create a ride from another tab
- Watch the status update automatically (no refresh needed!)

### 2. Driver Matching
- Create a ride
- System automatically finds nearby available driver
- Match happens within 1 second

### 3. Surge Pricing
- Create multiple rides quickly
- Watch surge multiplier increase
- See estimated fare adjust accordingly

### 4. Trip Lifecycle
- Complete ride flow: Request → Match → Accept → Start → End
- Each state change triggers notifications
- Real-time updates on Dashboard

### 5. WebSocket Notifications
- Open browser console (F12)
- See WebSocket messages in Network tab
- Real-time events flowing

## Next Steps

1. ✅ Run the quick demo
2. 📖 Read [README.md](README.md) for architecture details
3. 🔧 Read [SETUP_GUIDE.md](SETUP_GUIDE.md) for development setup
4. 📊 Read [PERFORMANCE_REPORT.md](PERFORMANCE_REPORT.md) for optimization details
5. 🧪 Run unit tests: `mvn test`
6. 🚀 Deploy to production

## Demo Script for Presentation

```
1. "This is the GoComet Ride Hailing Dashboard showing real-time ride updates"
   → Show Dashboard with live data

2. "Let me create a new ride request"
   → Go to Request Ride tab
   → Use preset location
   → Submit

3. "Notice the Dashboard updated automatically via WebSocket"
   → Show Dashboard with new ride

4. "Now the system is matching with nearby available drivers"
   → Explain matching algorithm (< 1 second)

5. "Let's accept the ride as a driver"
   → Go to Driver Panel
   → Update location
   → Accept ride

6. "Driver accepts and starts the trip"
   → Click Start Trip
   → Show real-time status updates

7. "Trip completed with automatic fare calculation"
   → Click End Trip
   → Show final fare with surge pricing

8. "All of this happened with sub-second latency"
   → Highlight performance metrics
```

## Important URLs

- **Frontend:** http://localhost:3000
- **Backend API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **Health Check:** http://localhost:8080/actuator/health
- **Metrics:** http://localhost:8080/actuator/metrics

## Support

For detailed documentation:
- Architecture: See [README.md](README.md)
- Setup: See [SETUP_GUIDE.md](SETUP_GUIDE.md)
- Performance: See [PERFORMANCE_REPORT.md](PERFORMANCE_REPORT.md)

---

**Happy Demoing! 🎯**
