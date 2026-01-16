# 🚀 Run Application Without Docker

If you don't want to use Docker, you can run everything locally. Here's how:

## Prerequisites

Install these on your Mac:

```bash
# Install Homebrew (if not already installed)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install PostgreSQL
brew install postgresql@15

# Install Redis
brew install redis

# Install Node.js (for frontend)
brew install node
```

## Step 1: Start PostgreSQL

```bash
# Start PostgreSQL service
brew services start postgresql@15

# Create database
createdb ride_hailing

# Verify it's running
psql -d ride_hailing -c "SELECT version();"
```

## Step 2: Start Redis

```bash
# Start Redis service
brew services start redis

# Verify it's running
redis-cli ping
# Should return: PONG
```

## Step 3: Run Backend

```bash
# Navigate to project
cd /Users/tanmay.mallick/Documents/gocomet

# Build the project
mvn clean install -DskipTests

# Run Spring Boot application
mvn spring-boot:run
```

Backend will start on: **http://localhost:8080**

Wait for: `Started RideHailingApplication in X.XXX seconds`

## Step 4: Run Frontend (New Terminal)

Open a **new terminal window** and run:

```bash
# Navigate to frontend
cd /Users/tanmay.mallick/Documents/gocomet/frontend

# Install dependencies (first time only)
npm install

# Start React app
npm start
```

Frontend will start on: **http://localhost:3000**

It will automatically open in your browser!

## Verify Everything is Running

### Check Backend
```bash
curl http://localhost:8080/actuator/health
# Should return: {"status":"UP"}
```

### Check Frontend
- Open browser: http://localhost:3000
- You should see the GoComet dashboard

### Check Database
```bash
psql -d ride_hailing -c "\dt"
# Should show tables: drivers, riders, rides, trips, payments
```

### Check Redis
```bash
redis-cli ping
# Should return: PONG
```

## Add Sample Data

### Create a Driver
```bash
psql -d ride_hailing -c "INSERT INTO drivers (name, phone_number, email, license_number, vehicle_number, vehicle_tier, status, region, rating, total_rides, created_at, updated_at) VALUES ('John Driver', '+919876543210', 'john@driver.com', 'DL1234567890', 'DL-01-AB-1234', 'ECONOMY', 'AVAILABLE', 'Delhi-NCR', 5.0, 0, NOW(), NOW());"
```

### Create a Rider
```bash
psql -d ride_hailing -c "INSERT INTO riders (name, phone_number, email, region, rating, total_rides, created_at, updated_at) VALUES ('Jane Rider', '+919876543211', 'jane@rider.com', 'Delhi-NCR', 5.0, 0, NOW(), NOW());"
```

## Stop Services

### Stop Backend
- Press `Ctrl + C` in the backend terminal

### Stop Frontend
- Press `Ctrl + C` in the frontend terminal

### Stop PostgreSQL
```bash
brew services stop postgresql@15
```

### Stop Redis
```bash
brew services stop redis
```

## Troubleshooting

### Port Already in Use

**Backend (8080):**
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

**Frontend (3000):**
```bash
# Find process using port 3000
lsof -i :3000

# Kill the process
kill -9 <PID>
```

**PostgreSQL (5432):**
```bash
# Find process using port 5432
lsof -i :5432

# Kill the process
kill -9 <PID>
```

### Database Connection Error

```bash
# Check PostgreSQL is running
brew services list | grep postgresql

# Restart PostgreSQL
brew services restart postgresql@15

# Check connection
psql -d ride_hailing -c "SELECT 1;"
```

### Redis Connection Error

```bash
# Check Redis is running
brew services list | grep redis

# Restart Redis
brew services restart redis

# Test connection
redis-cli ping
```

### Frontend Can't Connect to Backend

1. Make sure backend is running on http://localhost:8080
2. Check browser console for errors (F12)
3. Verify CORS settings in backend

### Maven Build Fails

```bash
# Clean and rebuild
mvn clean install

# If still fails, check Java version
java -version
# Should be Java 17 or higher
```

## Quick Start Script

Create a file `start-local.sh`:

```bash
#!/bin/bash

echo "Starting PostgreSQL..."
brew services start postgresql@15
sleep 2

echo "Starting Redis..."
brew services start redis
sleep 2

echo "Starting Backend..."
cd /Users/tanmay.mallick/Documents/gocomet
mvn spring-boot:run &
BACKEND_PID=$!

echo "Waiting for backend to start..."
sleep 10

echo "Starting Frontend..."
cd frontend
npm start &
FRONTEND_PID=$!

echo "✅ Services started!"
echo "Backend: http://localhost:8080"
echo "Frontend: http://localhost:3000"
echo ""
echo "Press Ctrl+C to stop all services"

# Wait for user interrupt
trap "kill $BACKEND_PID $FRONTEND_PID; brew services stop postgresql@15; brew services stop redis; exit" INT
wait
```

Make it executable:
```bash
chmod +x start-local.sh
```

Run it:
```bash
./start-local.sh
```

## Environment Variables (Optional)

You can create a `.env` file in the project root:

```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/ride_hailing
DATABASE_USER=postgres
DATABASE_PASSWORD=postgres

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# New Relic (optional)
NEW_RELIC_LICENSE_KEY=your-key-here
NEW_RELIC_ACCOUNT_ID=your-account-id
```

Then update `application.yml` to use these variables.

---

**That's it! You can now run the application without Docker! 🎉**
