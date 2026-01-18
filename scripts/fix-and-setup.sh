#!/bin/bash

echo "🔧 Fixing and Setting Up Sample Data..."
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

# Check if backend is running
if ! curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "${RED}❌ Backend is not running. Please start it first:${NC}"
    echo "   docker-compose up -d"
    exit 1
fi

echo "${BLUE}📊 Step 1: Checking current state...${NC}"
DRIVER_COUNT=$(curl -s http://localhost:8080/v1/drivers 2>/dev/null | jq -r '.data | length' 2>/dev/null || echo "0")
RIDER_COUNT=$(curl -s http://localhost:8080/v1/riders 2>/dev/null | jq -r '.data | length' 2>/dev/null || echo "0")

echo "   Drivers: $DRIVER_COUNT"
echo "   Riders: $RIDER_COUNT"
echo ""

if [ "$DRIVER_COUNT" -eq 0 ] || [ "$RIDER_COUNT" -eq 0 ]; then
    echo "${YELLOW}⚠️  Missing data. Setting up sample data...${NC}"
    echo ""
    ./scripts/setup-sample-data.sh
    echo ""
fi

echo "${BLUE}📊 Step 2: Ensuring all drivers are AVAILABLE...${NC}"
docker exec gocomet-postgres psql -U postgres -d ride_hailing -c "UPDATE drivers SET status = 'AVAILABLE';" > /dev/null 2>&1
echo "   ${GREEN}✅ All drivers set to AVAILABLE${NC}"
echo ""

echo "${BLUE}📊 Step 3: Refreshing driver locations in Redis...${NC}"
echo ""

# Get all drivers from database directly
DRIVERS=$(docker exec gocomet-postgres psql -U postgres -d ride_hailing -t -c "SELECT id, region FROM drivers;" 2>/dev/null | sed 's/^[[:space:]]*//;s/[[:space:]]*$//' | grep -v '^$' | sed 's/[[:space:]]*|[[:space:]]*/|/g')

if [ -z "$DRIVERS" ]; then
    echo "${RED}❌ No drivers found. Please run setup-sample-data.sh first.${NC}"
    exit 1
fi

LOCATION_MAP=(
    "Delhi-NCR|28.6145|77.2095"
    "Mumbai|19.0180|72.8565"
    "Bangalore|12.9720|77.5950"
    "Hyderabad|17.4490|78.3910"
    "Chennai|13.0480|80.2410"
)

UPDATED=0
echo "$DRIVERS" | while IFS='|' read -r driver_id region; do
    if [ -z "$driver_id" ] || [ "$driver_id" = "null" ]; then
        continue
    fi
    
    # Find location for this region
    LOCATION=""
    for loc in "${LOCATION_MAP[@]}"; do
        IFS='|' read -r loc_region lat lon <<< "$loc"
        if [ "$loc_region" = "$region" ]; then
            LOCATION="$lat|$lon"
            break
        fi
    done
    
    if [ -n "$LOCATION" ]; then
        IFS='|' read -r lat lon <<< "$LOCATION"
        RESPONSE=$(curl -s -X POST "http://localhost:8080/v1/drivers/$driver_id/location" \
          -H "Content-Type: application/json" \
          -d "{\"driverId\": $driver_id, \"latitude\": $lat, \"longitude\": $lon}")
        
        if echo "$RESPONSE" | jq -e '.success' > /dev/null 2>&1; then
            echo "   ${GREEN}✅${NC} Driver $driver_id ($region): $lat, $lon"
            UPDATED=$((UPDATED + 1))
        else
            echo "   ${YELLOW}⚠️${NC}  Driver $driver_id: Failed to update"
        fi
    else
        echo "   ${YELLOW}⚠️${NC}  Driver $driver_id: Unknown region '$region'"
    fi
done

echo ""
echo "${GREEN}✅ Setup complete!${NC}"
echo ""
echo "${BLUE}📋 Summary:${NC}"
echo "   • Drivers: $DRIVER_COUNT"
echo "   • Riders: $RIDER_COUNT"
echo "   • All drivers set to AVAILABLE"
echo "   • Locations refreshed in Redis"
echo ""
echo "${YELLOW}💡 Test Ride Creation:${NC}"
echo ""
echo "   ${BLUE}Delhi-NCR:${NC}"
echo "   • Rider ID: 1 or 2"
echo "   • Pickup: 28.6139, 77.2090 (Connaught Place)"
echo "   • Tier: ECONOMY, PREMIUM, LUXURY, or SUV"
echo ""
echo "   ${BLUE}Mumbai:${NC}"
echo "   • Rider ID: 3 or 4"
echo "   • Pickup: 19.0176, 72.8562 (Mumbai Central)"
echo "   • Tier: ECONOMY or PREMIUM"
echo ""
echo "   ${BLUE}Bangalore:${NC}"
echo "   • Rider ID: 5 or 6"
echo "   • Pickup: 12.9716, 77.5946 (MG Road)"
echo "   • Tier: ECONOMY or LUXURY"
echo ""
