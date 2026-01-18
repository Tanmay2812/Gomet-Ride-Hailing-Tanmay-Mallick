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

# Delhi-NCR Riders
curl -s -X POST http://localhost:8080/v1/riders \
  -H "Content-Type: application/json" \
  -d '{"name": "Rajesh Kumar", "phoneNumber": "+919876543210", "email": "rajesh.kumar@example.com", "region": "Delhi-NCR", "rating": 4.8}' | jq -r '.data.id // "exists"' > /tmp/rider1.txt
echo "   ✅ Rider 1: Rajesh Kumar (Delhi-NCR)"

curl -s -X POST http://localhost:8080/v1/riders \
  -H "Content-Type: application/json" \
  -d '{"name": "Priya Sharma", "phoneNumber": "+919876543211", "email": "priya.sharma@example.com", "region": "Delhi-NCR", "rating": 4.9}' | jq -r '.data.id // "exists"' > /tmp/rider2.txt
echo "   ✅ Rider 2: Priya Sharma (Delhi-NCR)"

# Mumbai Riders
curl -s -X POST http://localhost:8080/v1/riders \
  -H "Content-Type: application/json" \
  -d '{"name": "Amit Patel", "phoneNumber": "+919876543212", "email": "amit.patel@example.com", "region": "Mumbai", "rating": 4.7}' | jq -r '.data.id // "exists"' > /tmp/rider3.txt
echo "   ✅ Rider 3: Amit Patel (Mumbai)"

curl -s -X POST http://localhost:8080/v1/riders \
  -H "Content-Type: application/json" \
  -d '{"name": "Sneha Desai", "phoneNumber": "+919876543213", "email": "sneha.desai@example.com", "region": "Mumbai", "rating": 4.8}' | jq -r '.data.id // "exists"' > /tmp/rider4.txt
echo "   ✅ Rider 4: Sneha Desai (Mumbai)"

# Bangalore Riders
curl -s -X POST http://localhost:8080/v1/riders \
  -H "Content-Type: application/json" \
  -d '{"name": "Vikram Reddy", "phoneNumber": "+919876543214", "email": "vikram.reddy@example.com", "region": "Bangalore", "rating": 4.9}' | jq -r '.data.id // "exists"' > /tmp/rider5.txt
echo "   ✅ Rider 5: Vikram Reddy (Bangalore)"

curl -s -X POST http://localhost:8080/v1/riders \
  -H "Content-Type: application/json" \
  -d '{"name": "Ananya Iyer", "phoneNumber": "+919876543215", "email": "ananya.iyer@example.com", "region": "Bangalore", "rating": 4.8}' | jq -r '.data.id // "exists"' > /tmp/rider6.txt
echo "   ✅ Rider 6: Ananya Iyer (Bangalore)"

# Hyderabad Riders
curl -s -X POST http://localhost:8080/v1/riders \
  -H "Content-Type: application/json" \
  -d '{"name": "Mohammed Ali", "phoneNumber": "+919876543216", "email": "mohammed.ali@example.com", "region": "Hyderabad", "rating": 4.7}' | jq -r '.data.id // "exists"' > /tmp/rider7.txt
echo "   ✅ Rider 7: Mohammed Ali (Hyderabad)"

curl -s -X POST http://localhost:8080/v1/riders \
  -H "Content-Type: application/json" \
  -d '{"name": "Kavita Rao", "phoneNumber": "+919876543217", "email": "kavita.rao@example.com", "region": "Hyderabad", "rating": 4.9}' | jq -r '.data.id // "exists"' > /tmp/rider8.txt
echo "   ✅ Rider 8: Kavita Rao (Hyderabad)"

# Chennai Riders
curl -s -X POST http://localhost:8080/v1/riders \
  -H "Content-Type: application/json" \
  -d '{"name": "Arjun Krishnan", "phoneNumber": "+919876543218", "email": "arjun.krishnan@example.com", "region": "Chennai", "rating": 4.8}' | jq -r '.data.id // "exists"' > /tmp/rider9.txt
echo "   ✅ Rider 9: Arjun Krishnan (Chennai)"

curl -s -X POST http://localhost:8080/v1/riders \
  -H "Content-Type: application/json" \
  -d '{"name": "Meera Nair", "phoneNumber": "+919876543219", "email": "meera.nair@example.com", "region": "Chennai", "rating": 4.7}' | jq -r '.data.id // "exists"' > /tmp/rider10.txt
echo "   ✅ Rider 10: Meera Nair (Chennai)"

echo ""
echo "${BLUE}🚗 Step 2: Creating 10 drivers with different vehicle types and cities...${NC}"
echo ""

# Driver 1: ECONOMY - Delhi-NCR (Connaught Place)
DRIVER1=$(curl -s -X POST http://localhost:8080/v1/drivers \
  -H "Content-Type: application/json" \
  -d '{"name": "Rajesh Kumar", "phoneNumber": "+919111111111", "email": "rajesh.driver@example.com", "licenseNumber": "DL-01-2020-111111", "vehicleNumber": "DL-01-AB-1111", "vehicleTier": "ECONOMY", "region": "Delhi-NCR", "rating": 4.7}')
DRIVER1_ID=$(echo $DRIVER1 | jq -r '.data.id // empty')
if [ -z "$DRIVER1_ID" ] || [ "$DRIVER1_ID" = "null" ]; then
    DRIVER1_ID=1
    echo "   ℹ️  Driver 1 already exists (ID: $DRIVER1_ID)"
else
    echo "   ✅ Driver 1: Rajesh Kumar - ECONOMY (Delhi-NCR)"
fi

# Driver 2: PREMIUM - Delhi-NCR (Connaught Place)
DRIVER2=$(curl -s -X POST http://localhost:8080/v1/drivers \
  -H "Content-Type: application/json" \
  -d '{"name": "Amit Singh", "phoneNumber": "+919111111112", "email": "amit.driver@example.com", "licenseNumber": "DL-01-2020-222222", "vehicleNumber": "DL-01-CD-2222", "vehicleTier": "PREMIUM", "region": "Delhi-NCR", "rating": 4.8}')
DRIVER2_ID=$(echo $DRIVER2 | jq -r '.data.id // empty')
if [ -z "$DRIVER2_ID" ] || [ "$DRIVER2_ID" = "null" ]; then
    DRIVER2_ID=2
    echo "   ℹ️  Driver 2 already exists (ID: $DRIVER2_ID)"
else
    echo "   ✅ Driver 2: Amit Singh - PREMIUM (Delhi-NCR)"
fi

# Driver 3: LUXURY - Delhi-NCR (Connaught Place)
DRIVER3=$(curl -s -X POST http://localhost:8080/v1/drivers \
  -H "Content-Type: application/json" \
  -d '{"name": "Vikram Mehta", "phoneNumber": "+919111111113", "email": "vikram.driver@example.com", "licenseNumber": "DL-01-2020-333333", "vehicleNumber": "DL-01-EF-3333", "vehicleTier": "LUXURY", "region": "Delhi-NCR", "rating": 4.9}')
DRIVER3_ID=$(echo $DRIVER3 | jq -r '.data.id // empty')
if [ -z "$DRIVER3_ID" ] || [ "$DRIVER3_ID" = "null" ]; then
    DRIVER3_ID=3
    echo "   ℹ️  Driver 3 already exists (ID: $DRIVER3_ID)"
else
    echo "   ✅ Driver 3: Vikram Mehta - LUXURY (Delhi-NCR)"
fi

# Driver 4: SUV - Delhi-NCR (Noida)
DRIVER4=$(curl -s -X POST http://localhost:8080/v1/drivers \
  -H "Content-Type: application/json" \
  -d '{"name": "Suresh Yadav", "phoneNumber": "+919111111114", "email": "suresh.driver@example.com", "licenseNumber": "DL-01-2020-444444", "vehicleNumber": "DL-01-GH-4444", "vehicleTier": "SUV", "region": "Delhi-NCR", "rating": 4.6}')
DRIVER4_ID=$(echo $DRIVER4 | jq -r '.data.id // empty')
if [ -z "$DRIVER4_ID" ] || [ "$DRIVER4_ID" = "null" ]; then
    DRIVER4_ID=4
    echo "   ℹ️  Driver 4 already exists (ID: $DRIVER4_ID)"
else
    echo "   ✅ Driver 4: Suresh Yadav - SUV (Delhi-NCR)"
fi

# Driver 5: ECONOMY - Mumbai (Mumbai Central)
DRIVER5=$(curl -s -X POST http://localhost:8080/v1/drivers \
  -H "Content-Type: application/json" \
  -d '{"name": "Ramesh Joshi", "phoneNumber": "+919111111115", "email": "ramesh.driver@example.com", "licenseNumber": "MH-01-2020-555555", "vehicleNumber": "MH-01-AB-5555", "vehicleTier": "ECONOMY", "region": "Mumbai", "rating": 4.7}')
DRIVER5_ID=$(echo $DRIVER5 | jq -r '.data.id // empty')
if [ -z "$DRIVER5_ID" ] || [ "$DRIVER5_ID" = "null" ]; then
    DRIVER5_ID=5
    echo "   ℹ️  Driver 5 already exists (ID: $DRIVER5_ID)"
else
    echo "   ✅ Driver 5: Ramesh Joshi - ECONOMY (Mumbai)"
fi

# Driver 6: PREMIUM - Mumbai (BKC)
DRIVER6=$(curl -s -X POST http://localhost:8080/v1/drivers \
  -H "Content-Type: application/json" \
  -d '{"name": "Kiran Deshmukh", "phoneNumber": "+919111111116", "email": "kiran.driver@example.com", "licenseNumber": "MH-01-2020-666666", "vehicleNumber": "MH-01-CD-6666", "vehicleTier": "PREMIUM", "region": "Mumbai", "rating": 4.8}')
DRIVER6_ID=$(echo $DRIVER6 | jq -r '.data.id // empty')
if [ -z "$DRIVER6_ID" ] || [ "$DRIVER6_ID" = "null" ]; then
    DRIVER6_ID=6
    echo "   ℹ️  Driver 6 already exists (ID: $DRIVER6_ID)"
else
    echo "   ✅ Driver 6: Kiran Deshmukh - PREMIUM (Mumbai)"
fi

# Driver 7: ECONOMY - Bangalore (MG Road)
DRIVER7=$(curl -s -X POST http://localhost:8080/v1/drivers \
  -H "Content-Type: application/json" \
  -d '{"name": "Suresh Reddy", "phoneNumber": "+919111111117", "email": "suresh.reddy@example.com", "licenseNumber": "KA-01-2020-777777", "vehicleNumber": "KA-01-AB-7777", "vehicleTier": "ECONOMY", "region": "Bangalore", "rating": 4.7}')
DRIVER7_ID=$(echo $DRIVER7 | jq -r '.data.id // empty')
if [ -z "$DRIVER7_ID" ] || [ "$DRIVER7_ID" = "null" ]; then
    DRIVER7_ID=7
    echo "   ℹ️  Driver 7 already exists (ID: $DRIVER7_ID)"
else
    echo "   ✅ Driver 7: Suresh Reddy - ECONOMY (Bangalore)"
fi

# Driver 8: LUXURY - Bangalore (Airport)
DRIVER8=$(curl -s -X POST http://localhost:8080/v1/drivers \
  -H "Content-Type: application/json" \
  -d '{"name": "Ravi Iyer", "phoneNumber": "+919111111118", "email": "ravi.iyer@example.com", "licenseNumber": "KA-01-2020-888888", "vehicleNumber": "KA-01-CD-8888", "vehicleTier": "LUXURY", "region": "Bangalore", "rating": 4.9}')
DRIVER8_ID=$(echo $DRIVER8 | jq -r '.data.id // empty')
if [ -z "$DRIVER8_ID" ] || [ "$DRIVER8_ID" = "null" ]; then
    DRIVER8_ID=8
    echo "   ℹ️  Driver 8 already exists (ID: $DRIVER8_ID)"
else
    echo "   ✅ Driver 8: Ravi Iyer - LUXURY (Bangalore)"
fi

# Driver 9: PREMIUM - Hyderabad (Hitech City)
DRIVER9=$(curl -s -X POST http://localhost:8080/v1/drivers \
  -H "Content-Type: application/json" \
  -d '{"name": "Srinivas Rao", "phoneNumber": "+919111111119", "email": "srinivas.rao@example.com", "licenseNumber": "TS-01-2020-999999", "vehicleNumber": "TS-01-AB-9999", "vehicleTier": "PREMIUM", "region": "Hyderabad", "rating": 4.8}')
DRIVER9_ID=$(echo $DRIVER9 | jq -r '.data.id // empty')
if [ -z "$DRIVER9_ID" ] || [ "$DRIVER9_ID" = "null" ]; then
    DRIVER9_ID=9
    echo "   ℹ️  Driver 9 already exists (ID: $DRIVER9_ID)"
else
    echo "   ✅ Driver 9: Srinivas Rao - PREMIUM (Hyderabad)"
fi

# Driver 10: ECONOMY - Chennai (T Nagar)
DRIVER10=$(curl -s -X POST http://localhost:8080/v1/drivers \
  -H "Content-Type: application/json" \
  -d '{"name": "Karthik Nair", "phoneNumber": "+919111111120", "email": "karthik.nair@example.com", "licenseNumber": "TN-01-2020-101010", "vehicleNumber": "TN-01-AB-1010", "vehicleTier": "ECONOMY", "region": "Chennai", "rating": 4.7}')
DRIVER10_ID=$(echo $DRIVER10 | jq -r '.data.id // empty')
if [ -z "$DRIVER10_ID" ] || [ "$DRIVER10_ID" = "null" ]; then
    DRIVER10_ID=10
    echo "   ℹ️  Driver 10 already exists (ID: $DRIVER10_ID)"
else
    echo "   ✅ Driver 10: Karthik Nair - ECONOMY (Chennai)"
fi

echo ""
echo "${BLUE}📍 Step 3: Setting driver locations based on their cities...${NC}"
echo ""

# Delhi-NCR Drivers (near Connaught Place: 28.6139, 77.2090)
curl -s -X POST http://localhost:8080/v1/drivers/$DRIVER1_ID/location \
  -H "Content-Type: application/json" \
  -d "{\"driverId\": $DRIVER1_ID, \"latitude\": 28.6145, \"longitude\": 77.2095}" > /dev/null
echo "   ✅ Driver $DRIVER1_ID (ECONOMY, Delhi-NCR): 28.6145, 77.2095 (Connaught Place)"

curl -s -X POST http://localhost:8080/v1/drivers/$DRIVER2_ID/location \
  -H "Content-Type: application/json" \
  -d "{\"driverId\": $DRIVER2_ID, \"latitude\": 28.6150, \"longitude\": 77.2100}" > /dev/null
echo "   ✅ Driver $DRIVER2_ID (PREMIUM, Delhi-NCR): 28.6150, 77.2100 (Connaught Place)"

curl -s -X POST http://localhost:8080/v1/drivers/$DRIVER3_ID/location \
  -H "Content-Type: application/json" \
  -d "{\"driverId\": $DRIVER3_ID, \"latitude\": 28.6160, \"longitude\": 77.2110}" > /dev/null
echo "   ✅ Driver $DRIVER3_ID (LUXURY, Delhi-NCR): 28.6160, 77.2110 (Connaught Place)"

# Delhi-NCR Driver (Noida: 28.5355, 77.3910)
curl -s -X POST http://localhost:8080/v1/drivers/$DRIVER4_ID/location \
  -H "Content-Type: application/json" \
  -d "{\"driverId\": $DRIVER4_ID, \"latitude\": 28.5360, \"longitude\": 77.3915}" > /dev/null
echo "   ✅ Driver $DRIVER4_ID (SUV, Delhi-NCR): 28.5360, 77.3915 (Noida)"

# Mumbai Drivers (Mumbai Central: 19.0176, 72.8562)
curl -s -X POST http://localhost:8080/v1/drivers/$DRIVER5_ID/location \
  -H "Content-Type: application/json" \
  -d "{\"driverId\": $DRIVER5_ID, \"latitude\": 19.0180, \"longitude\": 72.8565}" > /dev/null
echo "   ✅ Driver $DRIVER5_ID (ECONOMY, Mumbai): 19.0180, 72.8565 (Mumbai Central)"

# Mumbai Driver (BKC: 19.0653, 72.8683)
curl -s -X POST http://localhost:8080/v1/drivers/$DRIVER6_ID/location \
  -H "Content-Type: application/json" \
  -d "{\"driverId\": $DRIVER6_ID, \"latitude\": 19.0655, \"longitude\": 72.8685}" > /dev/null
echo "   ✅ Driver $DRIVER6_ID (PREMIUM, Mumbai): 19.0655, 72.8685 (BKC)"

# Bangalore Drivers (MG Road: 12.9716, 77.5946)
curl -s -X POST http://localhost:8080/v1/drivers/$DRIVER7_ID/location \
  -H "Content-Type: application/json" \
  -d "{\"driverId\": $DRIVER7_ID, \"latitude\": 12.9720, \"longitude\": 77.5950}" > /dev/null
echo "   ✅ Driver $DRIVER7_ID (ECONOMY, Bangalore): 12.9720, 77.5950 (MG Road)"

# Bangalore Driver (Airport: 13.1986, 77.7066)
curl -s -X POST http://localhost:8080/v1/drivers/$DRIVER8_ID/location \
  -H "Content-Type: application/json" \
  -d "{\"driverId\": $DRIVER8_ID, \"latitude\": 13.1990, \"longitude\": 77.7070}" > /dev/null
echo "   ✅ Driver $DRIVER8_ID (LUXURY, Bangalore): 13.1990, 77.7070 (Airport)"

# Hyderabad Driver (Hitech City: 17.4486, 78.3908)
curl -s -X POST http://localhost:8080/v1/drivers/$DRIVER9_ID/location \
  -H "Content-Type: application/json" \
  -d "{\"driverId\": $DRIVER9_ID, \"latitude\": 17.4490, \"longitude\": 78.3910}" > /dev/null
echo "   ✅ Driver $DRIVER9_ID (PREMIUM, Hyderabad): 17.4490, 78.3910 (Hitech City)"

# Chennai Driver (T Nagar: 13.0475, 80.2406)
curl -s -X POST http://localhost:8080/v1/drivers/$DRIVER10_ID/location \
  -H "Content-Type: application/json" \
  -d "{\"driverId\": $DRIVER10_ID, \"latitude\": 13.0480, \"longitude\": 80.2410}" > /dev/null
echo "   ✅ Driver $DRIVER10_ID (ECONOMY, Chennai): 13.0480, 80.2410 (T Nagar)"

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
