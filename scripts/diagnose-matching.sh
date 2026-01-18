#!/bin/bash

echo "🔍 Diagnosing Driver Matching Issues..."
echo ""

# Colors
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

echo "${BLUE}📊 Step 1: Checking all drivers in database...${NC}"
echo ""
DRIVER_RESPONSE=$(curl -s http://localhost:8080/v1/drivers)
if [ -z "$DRIVER_RESPONSE" ] || [ "$DRIVER_RESPONSE" = "null" ]; then
    echo "${RED}   ❌ API returned empty response${NC}"
    echo "   Response: $DRIVER_RESPONSE"
    exit 1
fi

ALL_DRIVERS=$(echo "$DRIVER_RESPONSE" | jq -r '.data[]? | "\(.id)|\(.name)|\(.status)|\(.vehicleTier)|\(.region)"' 2>/dev/null)
if [ -z "$ALL_DRIVERS" ]; then
    echo "${RED}   ❌ No drivers found in database${NC}"
    echo "   ${YELLOW}💡 Run: ./scripts/setup-sample-data.sh${NC}"
    exit 1
fi

echo "   ${GREEN}Found drivers:${NC}"
echo "$ALL_DRIVERS" | while IFS='|' read -r id name status tier region; do
    echo "   • ID: $id | $name | Status: $status | Tier: $tier | Region: $region"
done

echo ""
echo "${BLUE}📊 Step 2: Checking driver locations in Redis...${NC}"
echo ""
DRIVER_COUNT=$(echo "$ALL_DRIVERS" | wc -l | tr -d ' ')
echo "   Checking locations for $DRIVER_COUNT drivers..."
FOUND_LOCATIONS=0
NO_LOCATIONS=0

echo "$ALL_DRIVERS" | while IFS='|' read -r id name status tier region; do
    LOCATION=$(curl -s "http://localhost:8080/v1/drivers/$id" | jq -r '.data.currentLocation // empty')
    if [ -n "$LOCATION" ] && [ "$LOCATION" != "null" ] && [ "$LOCATION" != "empty" ]; then
        LAT=$(echo $LOCATION | jq -r '.latitude // empty')
        LON=$(echo $LOCATION | jq -r '.longitude // empty')
        if [ -n "$LAT" ] && [ -n "$LON" ]; then
            echo "   ${GREEN}✅${NC} Driver $id ($name): $LAT, $LON"
            FOUND_LOCATIONS=$((FOUND_LOCATIONS + 1))
        else
            echo "   ${RED}❌${NC} Driver $id ($name): No location"
            NO_LOCATIONS=$((NO_LOCATIONS + 1))
        fi
    else
        echo "   ${RED}❌${NC} Driver $id ($name): No location in Redis"
        NO_LOCATIONS=$((NO_LOCATIONS + 1))
    fi
done

echo ""
echo "${BLUE}📊 Step 3: Testing matching with sample pickup locations...${NC}"
echo ""

# Test locations for each region
test_matching() {
    local region=$1
    local lat=$2
    local lon=$3
    local tier=$4
    
    echo "   ${YELLOW}Testing:${NC} Region=$region, Tier=$tier, Pickup=($lat, $lon)"
    
    # Get available drivers for this tier and region
    DRIVER_DATA=$(curl -s "http://localhost:8080/v1/drivers")
    AVAILABLE=$(echo "$DRIVER_DATA" | jq -r --arg region "$region" --arg tier "$tier" '.data[]? | select(.region == $region and .vehicleTier == $tier and .status == "AVAILABLE") | .id' 2>/dev/null)
    
    if [ -z "$AVAILABLE" ]; then
        echo "      ${RED}❌ No AVAILABLE drivers for tier $tier in region $region${NC}"
        return
    fi
    
    AVAILABLE_COUNT=$(echo "$AVAILABLE" | wc -l | tr -d ' ')
    echo "      ${GREEN}Found $AVAILABLE_COUNT AVAILABLE drivers${NC}"
    
    # Check if any have locations
    HAS_LOCATION=0
    echo "$AVAILABLE" | while read driver_id; do
        LOCATION=$(curl -s "http://localhost:8080/v1/drivers/$driver_id" | jq -r '.data.currentLocation // empty')
        if [ -n "$LOCATION" ] && [ "$LOCATION" != "null" ]; then
            HAS_LOCATION=$((HAS_LOCATION + 1))
        fi
    done
    
    if [ $HAS_LOCATION -eq 0 ]; then
        echo "      ${RED}❌ None of these drivers have locations in Redis!${NC}"
        echo "      ${YELLOW}💡 Run: ./scripts/setup-sample-data.sh${NC}"
    else
        echo "      ${GREEN}✅ $HAS_LOCATION drivers have locations${NC}"
    fi
}

# Delhi-NCR (Connaught Place)
test_matching "Delhi-NCR" "28.6139" "77.2090" "ECONOMY"
test_matching "Delhi-NCR" "28.6139" "77.2090" "PREMIUM"

# Mumbai (Mumbai Central)
test_matching "Mumbai" "19.0176" "72.8562" "ECONOMY"
test_matching "Mumbai" "19.0176" "72.8562" "PREMIUM"

# Bangalore (MG Road)
test_matching "Bangalore" "12.9716" "77.5946" "ECONOMY"
test_matching "Bangalore" "12.9716" "77.5946" "LUXURY"

echo ""
echo "${BLUE}📊 Step 4: Recommendations...${NC}"
echo ""

# Count AVAILABLE drivers
DRIVER_DATA=$(curl -s "http://localhost:8080/v1/drivers")
AVAILABLE_COUNT=$(echo "$DRIVER_DATA" | jq -r '.data[]? | select(.status == "AVAILABLE") | .id' 2>/dev/null | wc -l | tr -d ' ')
BUSY_COUNT=$(echo "$DRIVER_DATA" | jq -r '.data[]? | select(.status == "BUSY") | .id' 2>/dev/null | wc -l | tr -d ' ')

echo "   ${GREEN}AVAILABLE drivers: $AVAILABLE_COUNT${NC}"
echo "   ${YELLOW}BUSY drivers: $BUSY_COUNT${NC}"

if [ "$BUSY_COUNT" -gt 0 ]; then
    echo ""
    echo "   ${YELLOW}⚠️  Some drivers are BUSY. Reset them with:${NC}"
    echo "   ${BLUE}docker exec gocomet-postgres psql -U postgres -d ride_hailing -c \"UPDATE drivers SET status = 'AVAILABLE';\"${NC}"
fi

echo ""
echo "${GREEN}✅ Diagnosis complete!${NC}"
echo ""
echo "${YELLOW}💡 Common Issues:${NC}"
echo "   1. ${RED}Region mismatch:${NC} Rider region must match driver region"
echo "   2. ${RED}Tier mismatch:${NC} Requested tier must match driver's vehicle tier"
echo "   3. ${RED}No locations:${NC} Drivers need locations in Redis (expires after 5 min)"
echo "   4. ${RED}Status:${NC} Drivers must be AVAILABLE (not BUSY or OFFLINE)"
echo "   5. ${RED}Distance:${NC} Pickup must be within 5km of driver location"
echo ""
echo "${BLUE}🔧 Quick Fix:${NC}"
echo "   ./scripts/setup-sample-data.sh  # Re-setup locations and reset status"
