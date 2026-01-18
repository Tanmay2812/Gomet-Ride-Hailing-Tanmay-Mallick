#!/bin/bash

echo "🔄 Refreshing All Driver Locations in Redis..."
echo ""

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

# Check if backend is running
if ! curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "${RED}❌ Backend is not running${NC}"
    exit 1
fi

# Get all drivers from database
echo "${BLUE}📊 Getting all drivers from database...${NC}"
DRIVERS=$(docker exec gocomet-postgres psql -U postgres -d ride_hailing -t -c "SELECT id, region FROM drivers ORDER BY id;" 2>/dev/null)

if [ -z "$DRIVERS" ]; then
    echo "${RED}❌ No drivers found in database${NC}"
    echo "   Run: ./scripts/setup-sample-data.sh"
    exit 1
fi

UPDATED=0
FAILED=0

echo "$DRIVERS" | while IFS='|' read -r driver_id region; do
    # Clean whitespace
    driver_id=$(echo "$driver_id" | tr -d ' ')
    region=$(echo "$region" | tr -d ' ')
    
    if [ -z "$driver_id" ] || [ "$driver_id" = "" ]; then
        continue
    fi
    
    # Get location for this region
    case "$region" in
        "Delhi-NCR")
            lat="28.6145"
            lon="77.2095"
            ;;
        "Mumbai")
            lat="19.0180"
            lon="72.8565"
            ;;
        "Bangalore")
            lat="12.9720"
            lon="77.5950"
            ;;
        "Hyderabad")
            lat="17.4490"
            lon="78.3910"
            ;;
        "Chennai")
            lat="13.0480"
            lon="80.2410"
            ;;
        *)
            echo "   ${YELLOW}⚠️${NC}  Driver $driver_id: Unknown region '$region'"
            FAILED=$((FAILED + 1))
            continue
            ;;
    esac
    
    # Update location
    RESPONSE=$(curl -s -X POST "http://localhost:8080/v1/drivers/$driver_id/location" \
      -H "Content-Type: application/json" \
      -d "{\"driverId\": $driver_id, \"latitude\": $lat, \"longitude\": $lon}")
    
    if echo "$RESPONSE" | jq -e '.success' > /dev/null 2>&1; then
        echo "   ${GREEN}✅${NC} Driver $driver_id ($region): $lat, $lon"
        UPDATED=$((UPDATED + 1))
    else
        ERROR=$(echo "$RESPONSE" | jq -r '.message // .error // "Unknown error"' 2>/dev/null)
        echo "   ${RED}❌${NC} Driver $driver_id: $ERROR"
        FAILED=$((FAILED + 1))
    fi
done

echo ""
echo "${GREEN}✅ Location refresh complete!${NC}"
echo "   Updated: $UPDATED"
if [ $FAILED -gt 0 ]; then
    echo "   ${YELLOW}Failed: $FAILED${NC}"
fi
echo ""
echo "${BLUE}💡 All drivers are now ready for ride matching!${NC}"
