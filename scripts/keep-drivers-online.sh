#!/bin/bash

# Keep drivers' locations updated in Redis
# Run this script to simulate drivers being online

echo "🚗 Starting driver location simulator..."
echo "This will update driver locations every 10 seconds"
echo "Press Ctrl+C to stop"
echo ""

while true; do
    # Update Driver 1
    curl -s -X POST http://localhost:8080/v1/drivers/1/location \
      -H "Content-Type: application/json" \
      -d '{"driverId": 1, "latitude": 28.6139, "longitude": 77.2090}' > /dev/null
    
    # Update Driver 2
    curl -s -X POST http://localhost:8080/v1/drivers/2/location \
      -H "Content-Type: application/json" \
      -d '{"driverId": 2, "latitude": 28.6150, "longitude": 77.2100}' > /dev/null
    
    echo "$(date '+%H:%M:%S') - Driver locations updated ✅"
    
    sleep 10
done
