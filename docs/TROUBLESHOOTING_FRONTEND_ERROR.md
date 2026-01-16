# 🔧 Fix "Failed to create ride" Error

## Common Causes & Solutions

### 1. **Missing Rider in Database** (Most Common)

The error occurs because `riderId: 1` doesn't exist in the database.

**Solution:** Add a rider to the database:

```sql
-- Connect to your database
-- If using Docker:
docker exec -it gocomet-postgres psql -U postgres -d ride_hailing

-- Or if using local PostgreSQL:
psql -U postgres -d ride_hailing

-- Then run:
INSERT INTO riders (name, phone_number, email, region, rating, total_rides, created_at, updated_at)
VALUES ('Jane Rider', '+919876543211', 'jane@rider.com', 'Delhi-NCR', 5.0, 0, NOW(), NOW())
ON CONFLICT (phone_number) DO NOTHING;

-- Verify it was created:
SELECT id, name, phone_number FROM riders;
```

**Note:** Use the `id` from the query result as your `riderId` in the frontend!

### 2. **CORS Issue**

If you see CORS errors in browser console, add CORS configuration to backend.

**Check:** Open browser console (F12) and look for CORS errors.

**Solution:** Add CORS config to `WebConfig.java` (create if doesn't exist):

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

### 3. **Response Structure Issue**

The frontend has been updated to handle the response correctly. Make sure you're using the latest code.

### 4. **Backend Not Running**

**Check:**
```bash
curl http://localhost:8080/actuator/health
```

Should return: `{"status":"UP"}`

**If not running:**
```bash
# Start backend
cd /Users/tanmay.mallick/Documents/gocomet
mvn spring-boot:run
```

### 5. **Check Browser Console**

Open browser DevTools (F12) → Console tab, and look for:
- Network errors
- CORS errors
- API response details

The console will show the exact error message.

## Quick Test

### Test API Directly:
```bash
# First, get a valid rider ID from database
# Then test:
curl -X POST http://localhost:8080/v1/rides \
  -H "Content-Type: application/json" \
  -d '{
    "riderId": 1,
    "pickupLatitude": 28.6139,
    "pickupLongitude": 77.2090,
    "pickupAddress": "Connaught Place",
    "destinationLatitude": 28.5355,
    "destinationLongitude": 77.3910,
    "destinationAddress": "Noida Sector 18",
    "vehicleTier": "ECONOMY",
    "paymentMethod": "CREDIT_CARD",
    "region": "Delhi-NCR"
  }'
```

If this works but frontend doesn't, it's a frontend/CORS issue.

## Most Likely Fix

**90% of the time, it's missing rider data:**

1. Add rider to database (see SQL above)
2. Use the correct `riderId` in the frontend form
3. Refresh the page
4. Try again

## Still Not Working?

1. **Check browser console** for exact error
2. **Check backend logs** for validation errors
3. **Verify database connection** is working
4. **Test API with curl** to isolate frontend vs backend issue
