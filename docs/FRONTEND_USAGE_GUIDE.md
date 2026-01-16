# 🚗 Frontend Usage Guide - Complete Demo Flow

## Important Notes

### Before Creating Rides

**Always run this first to ensure drivers are available:**

```bash
# Reset drivers to AVAILABLE and update locations
docker exec gocomet-postgres psql -U postgres -d ride_hailing -c "UPDATE drivers SET status = 'AVAILABLE';"

# Update driver locations (lasts 5 minutes)
curl -s -X POST http://localhost:8080/v1/drivers/1/location -H "Content-Type: application/json" -d '{"driverId": 1, "latitude": 28.6139, "longitude": 77.2090}'
curl -s -X POST http://localhost:8080/v1/drivers/2/location -H "Content-Type: application/json" -d '{"driverId": 2, "latitude": 28.6150, "longitude": 77.2100}'
```

Or simply run:
```bash
cd /Users/tanmay.mallick/Documents/gocomet
./keep-drivers-online.sh
# Keep this running in a terminal
```

---

## Complete Demo Flow

### Step 1: Open Dashboard Tab ✅
- Keep this tab open
- You'll see real-time updates here as rides are created and matched

### Step 2: Request a Ride 🚗
- Go to **"Request Ride"** tab
- Rider ID should be **1** (or 2, 3)
- Click any preset location OR use custom coordinates
- Click **"Request Ride"** button

**Expected Result:**
```
✅ Ride Created Successfully!
Ride ID: XX
Status: MATCHED (or SEARCHING → MATCHED within 1 second)
Estimated Fare: ₹XXX.XX
```

### Step 3: Accept the Ride (Driver Panel) 👨‍✈️
- Go to **"Driver Panel"** tab
- Set Driver ID to **1** or **2**
- Update location (if needed)
- **Wait 2-3 seconds** for the ride request to appear in "Pending Ride Requests"
- Click **"Accept Ride"** button

**Expected Result:**
```
Ride moved to "Active Ride" section
Status changes to: ACCEPTED
```

### Step 4: Start the Trip 🏁
- In Driver Panel (Active Ride section)
- Click **"Start Trip"** button

**Expected Result:**
```
Status: IN_PROGRESS
Dashboard shows trip is active
```

### Step 5: End the Trip ✅
- In Driver Panel
- Click **"End Trip"** button

**Expected Result:**
```
Status: COMPLETED
Trip completed message
Final fare calculated
Driver becomes AVAILABLE again
```

### Step 6: Check Dashboard 📊
- Go back to Dashboard tab
- See the complete ride lifecycle
- All status changes displayed in real-time

---

## Common Issues & Fixes

### Issue: Status shows "FAILED"

**Cause:** No drivers available or driver locations expired

**Fix:**
```bash
# Reset all drivers to AVAILABLE
docker exec gocomet-postgres psql -U postgres -d ride_hailing -c "UPDATE drivers SET status = 'AVAILABLE';"

# Update driver locations
curl -s -X POST http://localhost:8080/v1/drivers/1/location -H "Content-Type: application/json" -d '{"driverId": 1, "latitude": 28.6139, "longitude": 77.2090}'
curl -s -X POST http://localhost:8080/v1/drivers/2/location -H "Content-Type: application/json" -d '{"driverId": 2, "latitude": 28.6150, "longitude": 77.2100}'

# Try creating ride again
```

### Issue: No pending ride requests in Driver Panel

**Causes:**
1. WebSocket not connected
2. Driver ID doesn't match the assigned driver
3. Ride was already accepted or expired

**Fix:**
- Refresh the page
- Check browser console (F12) for WebSocket errors
- Make sure you're using the correct driver ID (1 or 2)

### Issue: "Failed to create ride" error

**Causes:**
1. Backend not running
2. Database connection issue
3. Invalid data

**Fix:**
```bash
# Check backend is running
curl http://localhost:8080/actuator/health

# Should return: {"status":"UP"}

# If not, restart backend
docker-compose restart backend
```

---

## Quick Reset Script

Save this as `reset-demo.sh`:

```bash
#!/bin/bash

echo "🔄 Resetting demo environment..."

# Reset all drivers to AVAILABLE
docker exec gocomet-postgres psql -U postgres -d ride_hailing -c "UPDATE drivers SET status = 'AVAILABLE';" > /dev/null

# Clear old ride data (optional)
# docker exec gocomet-postgres psql -U postgres -d ride_hailing -c "DELETE FROM payments; DELETE FROM trips; DELETE FROM rides;"

# Update driver locations
curl -s -X POST http://localhost:8080/v1/drivers/1/location -H "Content-Type: application/json" -d '{"driverId": 1, "latitude": 28.6139, "longitude": 77.2090}' > /dev/null
curl -s -X POST http://localhost:8080/v1/drivers/2/location -H "Content-Type: application/json" -d '{"driverId": 2, "latitude": 28.6150, "longitude": 77.2100}' > /dev/null

echo "✅ Demo environment reset complete!"
echo "   - All drivers are AVAILABLE"
echo "   - Driver locations updated"
echo ""
echo "You can now create rides from the frontend!"
```

Then run:
```bash
chmod +x reset-demo.sh
./reset-demo.sh
```

---

## Pro Tips

1. **Keep driver location script running** in background:
   ```bash
   ./keep-drivers-online.sh
   ```

2. **Reset between demos:**
   ```bash
   ./reset-demo.sh
   ```

3. **Monitor in real-time:**
   - Keep Dashboard tab open
   - Open browser console (F12) to see WebSocket messages
   - Watch status changes happen live

4. **Test multiple scenarios:**
   - Different vehicle tiers (ECONOMY, PREMIUM, LUXURY)
   - Different regions
   - Multiple concurrent rides

---

## Demo Script for Presentation

```
1. Run: ./reset-demo.sh
2. Open: http://localhost:3000
3. Dashboard: "Here's the real-time dashboard"
4. Request Ride: "Creating a ride request"
5. Dashboard: "See it appear instantly - no refresh!"
6. Driver Panel: "Driver accepts the ride"
7. Driver Panel: "Start trip → End trip"
8. Dashboard: "Complete lifecycle in real-time"
```

---

**Remember:** Always ensure drivers are AVAILABLE and locations are updated before creating rides!
