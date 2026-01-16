#!/bin/bash

echo "🔄 Resetting demo environment..."

# Reset all drivers to AVAILABLE
docker exec gocomet-postgres psql -U postgres -d ride_hailing -c "UPDATE drivers SET status = 'AVAILABLE';" > /dev/null 2>&1

# Update driver locations (will be cached for 5 minutes)
curl -s -X POST http://localhost:8080/v1/drivers/1/location -H "Content-Type: application/json" -d '{"driverId": 1, "latitude": 28.6139, "longitude": 77.2090}' > /dev/null 2>&1
curl -s -X POST http://localhost:8080/v1/drivers/2/location -H "Content-Type: application/json" -d '{"driverId": 2, "latitude": 28.6150, "longitude": 77.2100}' > /dev/null 2>&1

echo "✅ Demo environment reset complete!"
echo ""
echo "   ✓ All drivers are AVAILABLE"
echo "   ✓ Driver locations updated (valid for 5 minutes)"
echo ""
echo "🚀 You can now create rides from the frontend!"
echo "   Open: http://localhost:3000"
