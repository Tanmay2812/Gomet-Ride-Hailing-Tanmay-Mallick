#!/bin/bash

echo "🚀 Setting up comprehensive sample data (10 riders + 10 drivers)..."
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if backend is running
if ! curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "❌ Backend is not running. Please start it first with: docker-compose up -d"
    exit 1
fi

echo "${BLUE}📝 Step 1: Creating 10 riders across different cities...${NC}"
echo ""

# Helper function to create rider (skip if exists)
create_rider() {
    local name=$1
    local phone=$2
    local email=$3
    local region=$4
    local rating=$5
    
    local response=$(curl -s -X POST http://localhost:8080/v1/riders \
      -H "Content-Type: application/json" \
      -d "{\"name\": \"$name\", \"phoneNumber\": \"$phone\", \"email\": \"$email\", \"region\": \"$region\", \"rating\": $rating}")
    
    local rider_id=$(echo $response | jq -r '.data.id // empty')
    local error=$(echo $response | jq -r '.message // empty')
    
    if [ -n "$rider_id" ] && [ "$rider_id" != "null" ] && [ "$rider_id" != "empty" ]; then
        echo "   ✅ Created: $name ($region) - ID: $rider_id"
        echo $rider_id
    elif [[ "$error" == *"already exists"* ]]; then
        echo "   ⏭️  Skipped: $name ($region) - already exists"
        echo ""
    else
        echo "   ⚠️  Error creating $name: $error"
        echo ""
    fi
}

# Delhi-NCR Riders
create_rider "Rajesh Kumar" "+919876543210" "rajesh.kumar@example.com" "Delhi-NCR" "4.8" > /tmp/rider1.txt

create_rider "Priya Sharma" "+919876543211" "priya.sharma@example.com" "Delhi-NCR" "4.9" > /tmp/rider2.txt

# Mumbai Riders
create_rider "Amit Patel" "+919876543212" "amit.patel@example.com" "Mumbai" "4.7" > /tmp/rider3.txt
create_rider "Sneha Desai" "+919876543213" "sneha.desai@example.com" "Mumbai" "4.8" > /tmp/rider4.txt

# Bangalore Riders
create_rider "Vikram Reddy" "+919876543214" "vikram.reddy@example.com" "Bangalore" "4.9" > /tmp/rider5.txt
create_rider "Ananya Iyer" "+919876543215" "ananya.iyer@example.com" "Bangalore" "4.8" > /tmp/rider6.txt

# Hyderabad Riders
create_rider "Mohammed Ali" "+919876543216" "mohammed.ali@example.com" "Hyderabad" "4.7" > /tmp/rider7.txt
create_rider "Kavita Rao" "+919876543217" "kavita.rao@example.com" "Hyderabad" "4.9" > /tmp/rider8.txt

# Chennai Riders
create_rider "Arjun Krishnan" "+919876543218" "arjun.krishnan@example.com" "Chennai" "4.8" > /tmp/rider9.txt
create_rider "Meera Nair" "+919876543219" "meera.nair@example.com" "Chennai" "4.7" > /tmp/rider10.txt

echo ""
echo "${BLUE}🚗 Step 2: Creating 10 drivers with different vehicle types and cities...${NC}"
echo ""

# Helper function to create driver (skip if exists)
create_driver() {
    local name=$1
    local phone=$2
    local email=$3
    local license=$4
    local vehicle=$5
    local tier=$6
    local region=$7
    local rating=$8
    
    # First, try to find existing driver by phone number in database
    local existing_id=$(docker exec gocomet-postgres psql -U postgres -d ride_hailing -t -c "SELECT id FROM drivers WHERE \"phoneNumber\" = '$phone' LIMIT 1;" 2>/dev/null | tr -d ' ')
    
    if [ -n "$existing_id" ] && [ "$existing_id" != "" ]; then
        echo "   ⏭️  Skipped: $name - $tier ($region) - using existing ID: $existing_id" >&2
        echo $existing_id
        return
    fi
    
    # Try to create new driver
    local response=$(curl -s -X POST http://localhost:8080/v1/drivers \
      -H "Content-Type: application/json" \
      -d "{\"name\": \"$name\", \"phoneNumber\": \"$phone\", \"email\": \"$email\", \"licenseNumber\": \"$license\", \"vehicleNumber\": \"$vehicle\", \"vehicleTier\": \"$tier\", \"status\": \"AVAILABLE\", \"region\": \"$region\", \"rating\": $rating}")
    
    local driver_id=$(echo $response | jq -r '.data.id // empty')
    local error=$(echo $response | jq -r '.message // empty')
    
    if [ -n "$driver_id" ] && [ "$driver_id" != "null" ] && [ "$driver_id" != "empty" ]; then
        echo "   ✅ Created: $name - $tier ($region) - ID: $driver_id" >&2
        echo $driver_id
    elif [[ "$error" == *"already exists"* ]]; then
        # Try again to find by phone
        existing_id=$(docker exec gocomet-postgres psql -U postgres -d ride_hailing -t -c "SELECT id FROM drivers WHERE \"phoneNumber\" = '$phone' LIMIT 1;" 2>/dev/null | tr -d ' ')
        if [ -n "$existing_id" ] && [ "$existing_id" != "" ]; then
            echo "   ⏭️  Skipped: $name - $tier ($region) - using existing ID: $existing_id" >&2
            echo $existing_id
        else
            echo "   ⏭️  Skipped: $name - $tier ($region) - already exists" >&2
            echo ""
        fi
    else
        echo "   ⚠️  Error creating $name: $error" >&2
        echo ""
    fi
}

# Driver 1: ECONOMY - Delhi-NCR (Connaught Place)
DRIVER1_ID=$(create_driver "Rajesh Kumar" "+919111111111" "rajesh.driver@example.com" "DL-01-2020-111111" "DL-01-AB-1111" "ECONOMY" "Delhi-NCR" "4.7")

# Driver 2: PREMIUM - Delhi-NCR (Connaught Place)
DRIVER2_ID=$(create_driver "Amit Singh" "+919111111112" "amit.driver@example.com" "DL-01-2020-222222" "DL-01-CD-2222" "PREMIUM" "Delhi-NCR" "4.8")

# Driver 3: LUXURY - Delhi-NCR (Connaught Place)
DRIVER3_ID=$(create_driver "Vikram Mehta" "+919111111113" "vikram.driver@example.com" "DL-01-2020-333333" "DL-01-EF-3333" "LUXURY" "Delhi-NCR" "4.9")

# Driver 4: SUV - Delhi-NCR (Noida)
DRIVER4_ID=$(create_driver "Suresh Yadav" "+919111111114" "suresh.driver@example.com" "DL-01-2020-444444" "DL-01-GH-4444" "SUV" "Delhi-NCR" "4.6")

# Driver 5: ECONOMY - Mumbai (Mumbai Central)
DRIVER5_ID=$(create_driver "Ramesh Joshi" "+919111111115" "ramesh.driver@example.com" "MH-01-2020-555555" "MH-01-AB-5555" "ECONOMY" "Mumbai" "4.7")

# Driver 6: PREMIUM - Mumbai (BKC)
DRIVER6_ID=$(create_driver "Kiran Deshmukh" "+919111111116" "kiran.driver@example.com" "MH-01-2020-666666" "MH-01-CD-6666" "PREMIUM" "Mumbai" "4.8")

# Driver 7: ECONOMY - Bangalore (MG Road)
DRIVER7_ID=$(create_driver "Suresh Reddy" "+919111111117" "suresh.reddy@example.com" "KA-01-2020-777777" "KA-01-AB-7777" "ECONOMY" "Bangalore" "4.7")

# Driver 8: LUXURY - Bangalore (Airport)
DRIVER8_ID=$(create_driver "Ravi Iyer" "+919111111118" "ravi.iyer@example.com" "KA-01-2020-888888" "KA-01-CD-8888" "LUXURY" "Bangalore" "4.9")

# Driver 9: PREMIUM - Hyderabad (Hitech City)
DRIVER9_ID=$(create_driver "Srinivas Rao" "+919111111119" "srinivas.rao@example.com" "TS-01-2020-999999" "TS-01-AB-9999" "PREMIUM" "Hyderabad" "4.8")

# Driver 10: ECONOMY - Chennai (T Nagar)
DRIVER10_ID=$(create_driver "Karthik Nair" "+919111111120" "karthik.nair@example.com" "TN-01-2020-101010" "TN-01-AB-1010" "ECONOMY" "Chennai" "4.7")

echo ""
echo "${BLUE}📍 Step 3: Setting driver locations based on their cities...${NC}"
echo ""

# Helper function to set driver location (skip if driver ID is empty)
set_driver_location() {
    local driver_id=$1
    local lat=$2
    local lon=$3
    local tier=$4
    local region=$5
    local location=$6
    
    if [ -z "$driver_id" ] || [ "$driver_id" = "null" ] || [ "$driver_id" = "empty" ]; then
        echo "   ⏭️  Skipped location update (driver not found)"
        return
    fi
    
    curl -s -X POST http://localhost:8080/v1/drivers/$driver_id/location \
      -H "Content-Type: application/json" \
      -d "{\"driverId\": $driver_id, \"latitude\": $lat, \"longitude\": $lon}" > /dev/null
    echo "   ✅ Driver $driver_id ($tier, $region): $lat, $lon ($location)"
}

# Delhi-NCR Drivers (near Connaught Place: 28.6139, 77.2090)
set_driver_location "$DRIVER1_ID" "28.6145" "77.2095" "ECONOMY" "Delhi-NCR" "Connaught Place"

set_driver_location "$DRIVER2_ID" "28.6150" "77.2100" "PREMIUM" "Delhi-NCR" "Connaught Place"
set_driver_location "$DRIVER3_ID" "28.6160" "77.2110" "LUXURY" "Delhi-NCR" "Connaught Place"

# Delhi-NCR Driver (Noida: 28.5355, 77.3910)
set_driver_location "$DRIVER4_ID" "28.5360" "77.3915" "SUV" "Delhi-NCR" "Noida"

# Mumbai Drivers (Mumbai Central: 19.0176, 72.8562)
set_driver_location "$DRIVER5_ID" "19.0180" "72.8565" "ECONOMY" "Mumbai" "Mumbai Central"

# Mumbai Driver (BKC: 19.0653, 72.8683)
set_driver_location "$DRIVER6_ID" "19.0655" "72.8685" "PREMIUM" "Mumbai" "BKC"

# Bangalore Drivers (MG Road: 12.9716, 77.5946)
set_driver_location "$DRIVER7_ID" "12.9720" "77.5950" "ECONOMY" "Bangalore" "MG Road"

# Bangalore Driver (Airport: 13.1986, 77.7066)
set_driver_location "$DRIVER8_ID" "13.1990" "77.7070" "LUXURY" "Bangalore" "Airport"

# Hyderabad Driver (Hitech City: 17.4486, 78.3908)
set_driver_location "$DRIVER9_ID" "17.4490" "78.3910" "PREMIUM" "Hyderabad" "Hitech City"

# Chennai Driver (T Nagar: 13.0475, 80.2406)
set_driver_location "$DRIVER10_ID" "13.0480" "80.2410" "ECONOMY" "Chennai" "T Nagar"

echo ""
echo "${BLUE}🔄 Step 4: Ensuring all drivers are AVAILABLE...${NC}"
echo ""

# Reset all drivers to AVAILABLE using database
docker exec gocomet-postgres psql -U postgres -d ride_hailing -c "UPDATE drivers SET status = 'AVAILABLE';" > /dev/null 2>&1
echo "   ✅ All drivers set to AVAILABLE status"

echo ""
echo "${GREEN}✅ Comprehensive sample data setup complete!${NC}"
echo ""
echo "${YELLOW}📊 Summary:${NC}"
echo ""
echo "${BLUE}Riders (10):${NC}"
echo "   • Delhi-NCR: 2 riders"
echo "   • Mumbai: 2 riders"
echo "   • Bangalore: 2 riders"
echo "   • Hyderabad: 2 riders"
echo "   • Chennai: 2 riders"
echo ""
echo "${BLUE}Drivers (10):${NC}"
echo "   • Delhi-NCR: 4 drivers (ECONOMY, PREMIUM, LUXURY, SUV)"
echo "   • Mumbai: 2 drivers (ECONOMY, PREMIUM)"
echo "   • Bangalore: 2 drivers (ECONOMY, LUXURY)"
echo "   • Hyderabad: 1 driver (PREMIUM)"
echo "   • Chennai: 1 driver (ECONOMY)"
echo ""
echo "${BLUE}Vehicle Tiers:${NC}"
echo "   • ECONOMY: 4 drivers"
echo "   • PREMIUM: 3 drivers"
echo "   • LUXURY: 2 drivers"
echo "   • SUV: 1 driver"
echo ""
echo "${GREEN}🚀 You can now create rides from any city!${NC}"
echo ""
echo "${YELLOW}💡 Test Examples:${NC}"
echo ""
echo "   ${BLUE}Delhi-NCR:${NC}"
echo "   • Rider ID: 1 or 2"
echo "   • Pickup: Connaught Place (28.6139, 77.2090)"
echo "   • Vehicle Tier: ECONOMY, PREMIUM, LUXURY, or SUV"
echo ""
echo "   ${BLUE}Mumbai:${NC}"
echo "   • Rider ID: 3 or 4"
echo "   • Pickup: Mumbai Central (19.0176, 72.8562)"
echo "   • Vehicle Tier: ECONOMY or PREMIUM"
echo ""
echo "   ${BLUE}Bangalore:${NC}"
echo "   • Rider ID: 5 or 6"
echo "   • Pickup: MG Road (12.9716, 77.5946)"
echo "   • Vehicle Tier: ECONOMY or LUXURY"
echo ""
echo "   ${BLUE}Hyderabad:${NC}"
echo "   • Rider ID: 7 or 8"
echo "   • Pickup: Hitech City (17.4486, 78.3908)"
echo "   • Vehicle Tier: PREMIUM"
echo ""
echo "   ${BLUE}Chennai:${NC}"
echo "   • Rider ID: 9 or 10"
echo "   • Pickup: T Nagar (13.0475, 80.2406)"
echo "   • Vehicle Tier: ECONOMY"
echo ""

# Cleanup
rm -f /tmp/rider*.txt
