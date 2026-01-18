# Demo Guide - GoComet Ride-Hailing Platform
## Step-by-Step UI Walkthrough

**Version:** 1.0  
**Date:** 2026  
**Author:** Tanmay Mallick

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Starting the Application](#starting-the-application)
3. [Demo Scenarios](#demo-scenarios)
   - [Scenario 1: Complete Ride Flow](#scenario-1-complete-ride-flow)
   - [Scenario 2: Driver Management](#scenario-2-driver-management)
   - [Scenario 3: Rider Management](#scenario-3-rider-management)
   - [Scenario 4: Dashboard Overview](#scenario-4-dashboard-overview)
4. [UI Navigation Guide](#ui-navigation-guide)
5. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### 1. Ensure Services Are Running

Before starting the demo, ensure all services are up:

```bash
# Check if Docker containers are running
docker ps

# If not running, start them
docker-compose up -d

# Wait for services to be ready (about 30 seconds)
docker-compose logs -f backend
# Press Ctrl+C once you see "Started RideHailingApplication"
```

### 2. Setup Sample Data

Run the sample data setup script:

```bash
./scripts/setup-sample-data.sh
```

This creates:
- 10 riders across different cities
- 10 drivers with different vehicle types and locations
- All drivers set to AVAILABLE status

### 3. Access the Application

- **Frontend URL:** http://localhost:3000
- **Backend API:** http://localhost:8080
- **API Docs (Swagger):** http://localhost:8080/swagger-ui.html

---

## Starting the Application

### Step 1: Open the Application

1. Open your web browser
2. Navigate to: **http://localhost:3000**
3. You should see the GoComet Ride Hailing homepage

### Step 2: Verify Application Status

The application header should display:
- **Title:** "🚗 GoComet Ride Hailing"
- **Developer Credit:** "Developed by Tanmay Mallick" (with LinkedIn link)
- **Navigation Tabs:** 5 tabs visible:
  - Dashboard
  - Request Ride
  - Driver Panel
  - Rider Details
  - Driver Details

---

## Demo Scenarios

## Scenario 1: Complete Ride Flow

This scenario demonstrates the complete ride lifecycle from request to payment.

### Step 1: Navigate to Request Ride Tab

1. Click on the **"Request Ride"** tab in the navigation bar
2. You should see the ride request form with the following sections:
   - Rider Information
   - Pickup Location
   - Destination
   - Ride Details

### Step 2: Fill Ride Request Form

**Option A: Use Quick Location Button (Recommended)**

1. In the "Quick Locations" section, click **"📍 Delhi to Noida"**
   - This auto-fills:
     - Pickup: Connaught Place, New Delhi (28.6139, 77.2090)
     - Destination: Noida Sector 18 (28.5355, 77.3910)

**Option B: Manual Entry**

1. **Rider Information:**
   - Enter **Rider ID:** `1` (or any existing rider ID: 1-10)

2. **Pickup Location:**
   - **Latitude:** `28.6139`
   - **Longitude:** `77.2090`
   - **Address:** `Connaught Place, New Delhi`

3. **Destination:**
   - **Latitude:** `28.5355`
   - **Longitude:** `77.3910`
   - **Address:** `Noida Sector 18`

4. **Ride Details:**
   - **Vehicle Tier:** Select `Economy` (dropdown)
   - **Payment Method:** Select `Credit Card` (dropdown)
   - **Region:** `Delhi-NCR`

### Step 3: Submit Ride Request

1. Click the **"🚗 Request Ride"** button
2. The button will show **"🔄 Creating Ride..."** while processing
3. Wait for the response (usually 1-2 seconds)

### Step 4: View Ride Creation Result

**Success Case:**

You should see a green success message:
```
✅ Ride Created Successfully!

Ride ID: 45
Status: REQUESTED (or SEARCHING, or MATCHED)
Estimated Fare: ₹366.73
💡 Check the Dashboard tab for real-time updates!
```

**Failure Case (if no drivers available):**

You should see a red error message:
```
❌ Ride Creation Failed

Ride ID: 49
Status: FAILED
⚠️ Reason: [Specific failure reason]
💡 Suggestions:
  • Try a different vehicle tier
  • Check if drivers are online
  • Use the Driver Panel to ensure drivers are AVAILABLE
```

**If you see FAILED status:**
- Go to **Driver Panel** tab
- Ensure drivers are AVAILABLE
- Update driver locations
- Try again

### Step 5: Monitor Ride Status (Dashboard)

1. Click on the **"Dashboard"** tab
2. You should see:
   - **Active Rides:** Count of rides in progress
   - **Total Rides:** Total rides created
   - **Completed Rides:** Count of completed rides
   - **Ride List:** Table showing recent rides with:
     - Ride ID
     - Rider ID
     - Status (REQUESTED, SEARCHING, MATCHED, ACCEPTED, STARTED, COMPLETED)
     - Estimated Fare
     - Created At

3. **Watch for Real-time Updates:**
   - The dashboard updates automatically via WebSocket
   - Ride status changes appear without refreshing
   - Status transitions: REQUESTED → SEARCHING → MATCHED → ACCEPTED

### Step 6: Driver Accepts Ride (Driver Panel)

1. Click on the **"Driver Panel"** tab
2. Enter a **Driver ID:** `1` (or any available driver: 1-10)
3. Click **"Get Driver Info"** or **"Refresh"**
4. You should see:
   - Driver details (name, phone, vehicle, rating)
   - Driver status: **AVAILABLE**
   - **Pending Ride Requests** section

5. **If a ride is matched:**
   - You'll see a ride request in the "Pending Ride Requests" section
   - Click **"Accept Ride"** button
   - Status changes to **ACCEPTED**
   - Driver status changes to **ON_RIDE**

### Step 7: Start Trip

1. In the **Driver Panel**, after accepting a ride:
   - You should see **"Start Trip"** button
   - Click **"Start Trip"**
   - Trip status changes to **STARTED**
   - Ride status changes to **STARTED**

### Step 8: End Trip

1. In the **Driver Panel**, after starting a trip:
   - You should see **"End Trip"** button
   - Click **"End Trip"**
   - A form appears:
     - **End Latitude:** `28.5355` (or destination latitude)
     - **End Longitude:** `77.3910` (or destination longitude)
     - **Distance (km):** `15.5` (or actual distance)
   - Click **"End Trip"**
   - Trip status changes to **ENDED**
   - Final fare is calculated and displayed

### Step 9: Process Payment

1. After ending the trip:
   - Payment is automatically triggered
   - You'll see payment status in the response
   - Payment status: **SUCCESS** or **FAILED**

2. **View Payment Details:**
   - Check the Dashboard for completed rides
   - Payment information is included in ride details

---

## Scenario 2: Driver Management

This scenario demonstrates creating and managing drivers.

### Step 1: Navigate to Driver Details Tab

1. Click on the **"Driver Details"** tab in the navigation bar
2. You should see two modes:
   - **View Mode** (default): View existing driver details
   - **Create Mode**: Create new driver
   - **Edit Mode**: Edit existing driver

### Step 2: View Existing Driver

1. In **View Mode:**
   - Enter **Driver ID:** `1`
   - Click **"Get Driver Info"** or **"Refresh"**
   - You should see:
     - Driver Information:
       - Name, Phone, Email
       - License Number, Vehicle Number
       - Vehicle Tier, Status, Region
       - Rating, Total Rides
     - **Ride History:** Table showing all rides for this driver

### Step 3: Create New Driver

1. Click the **"Create"** tab (next to "View" tab)
2. Fill in the form:
   - **Name:** `Test Driver`
   - **Phone Number:** `+919999999999`
   - **Email:** `test.driver@example.com`
   - **License Number:** `DL-01-2020-TEST01`
   - **Vehicle Number:** `DL-01-TEST-1234`
   - **Vehicle Tier:** Select `Economy` (dropdown)
   - **Region:** `Delhi-NCR`
   - **Rating:** `5.0` (or leave empty for default 5.0)
3. Click **"Create Driver"** button
4. You should see:
   - Success message: **"Driver created successfully! ID: [new_id]"**
   - Automatically switches to View mode
   - Shows the newly created driver

### Step 4: Update Driver Location

1. Go to **Driver Panel** tab
2. Enter the **Driver ID** you just created (or any driver ID)
3. Scroll to **"Update Location"** section
4. Enter:
   - **Latitude:** `28.6139`
   - **Longitude:** `77.2090`
5. Click **"Update Location"** button
6. You should see: **"Location updated successfully"**
7. This location is now used for driver matching

### Step 5: Edit Driver

1. Go back to **Driver Details** tab
2. Enter the **Driver ID** you want to edit
3. Click **"Get Driver Info"**
4. Click the **"Edit"** tab
5. Modify any field (e.g., change Rating to `4.8`)
6. Click **"Update Driver"** button
7. You should see: **"Driver updated successfully"**

---

## Scenario 3: Rider Management

This scenario demonstrates creating and managing riders.

### Step 1: Navigate to Rider Details Tab

1. Click on the **"Rider Details"** tab in the navigation bar
2. Similar to Driver Details, you'll see:
   - **View Mode**: View existing rider
   - **Create Mode**: Create new rider
   - **Edit Mode**: Edit existing rider

### Step 2: View Existing Rider

1. In **View Mode:**
   - Enter **Rider ID:** `1`
   - Click **"Get Rider Info"** or **"Refresh"**
   - You should see:
     - Rider Information:
       - Name, Phone, Email
       - Region, Rating, Total Rides
     - **Ride History:** Table showing all rides for this rider

### Step 3: Create New Rider

1. Click the **"Create"** tab
2. Fill in the form:
   - **Name:** `Test Rider`
   - **Phone Number:** `+918888888888`
   - **Email:** `test.rider@example.com`
   - **Region:** `Delhi-NCR`
   - **Rating:** `5.0` (or leave empty for default 5.0)
3. Click **"Create Rider"** button
4. You should see:
   - Success message: **"Rider created successfully! ID: [new_id]"**
   - Automatically switches to View mode
   - Shows the newly created rider

### Step 4: Edit Rider

1. Enter the **Rider ID** you want to edit
2. Click **"Get Rider Info"**
3. Click the **"Edit"** tab
4. Modify any field
5. Click **"Update Rider"** button
6. You should see: **"Rider updated successfully"**

---

## Scenario 4: Dashboard Overview

This scenario demonstrates the dashboard features and real-time updates.

### Step 1: Navigate to Dashboard

1. Click on the **"Dashboard"** tab
2. You should see:
   - **Statistics Cards:**
     - Active Rides (count)
     - Total Rides (count)
     - Completed Rides (count)
   - **Ride List Table:**
     - Columns: ID, Rider ID, Driver ID, Status, Estimated Fare, Created At
     - Shows all rides in the system

### Step 2: Create a Ride (to see updates)

1. Go to **Request Ride** tab
2. Create a ride (follow Scenario 1, Steps 2-3)
3. Return to **Dashboard** tab
4. You should see:
   - **Total Rides** count increased
   - New ride appears in the **Ride List**
   - Status shows as **REQUESTED** or **SEARCHING**

### Step 3: Watch Real-time Updates

1. Keep the **Dashboard** tab open
2. In another browser tab/window, go to **Driver Panel**
3. Accept a ride in the Driver Panel
4. Return to Dashboard tab
5. You should see:
   - Ride status updated to **ACCEPTED** (without refreshing)
   - **Active Rides** count updated
   - This is powered by WebSocket real-time updates

### Step 4: Filter and Search (if implemented)

- Use the search/filter options to find specific rides
- Filter by status, rider, or driver

---

## UI Navigation Guide

### Navigation Tabs

The application has 5 main navigation tabs:

1. **Dashboard**
   - Overview of all rides
   - Real-time statistics
   - Ride list with status

2. **Request Ride**
   - Create new ride requests
   - Quick location presets
   - Form validation

3. **Driver Panel**
   - View driver information
   - Update driver location
   - Accept rides
   - Manage trips (start, pause, end)

4. **Rider Details**
   - View rider information
   - Create new riders
   - Edit rider details
   - View ride history

5. **Driver Details**
   - View driver information
   - Create new drivers
   - Edit driver details
   - View ride history

### Common UI Elements

**Status Badges:**
- **REQUESTED:** Yellow badge
- **SEARCHING:** Yellow badge
- **MATCHED:** Blue badge
- **ACCEPTED:** Blue badge
- **STARTED:** Blue badge
- **COMPLETED:** Green badge
- **FAILED:** Red badge
- **CANCELLED:** Gray badge

**Buttons:**
- **Primary Actions:** Purple gradient buttons
- **Secondary Actions:** Outlined buttons
- **Disabled State:** Grayed out with reduced opacity

**Forms:**
- **Required Fields:** Marked with asterisk (*)
- **Validation:** Real-time validation with error messages
- **Success Messages:** Green alert boxes
- **Error Messages:** Red alert boxes

**Loading States:**
- Buttons show "Processing..." or spinner during API calls
- Forms are disabled during submission

---

## Troubleshooting

### Issue 1: "Failed to create ride" - Status: FAILED

**Symptoms:**
- Ride creation returns FAILED status
- Error message shows: "No drivers available" or "No drivers found within 5km"

**Solution:**
1. Go to **Driver Panel** tab
2. Check driver status (should be **AVAILABLE**)
3. If driver is **BUSY** or **ON_RIDE**:
   - Go to **Driver Details** tab
   - Edit the driver
   - Change status to **AVAILABLE**
4. Update driver location:
   - Go to **Driver Panel**
   - Enter driver ID
   - Update location to be near your pickup location (within 5km)
5. Try creating the ride again

**Prevention:**
- Run `./scripts/setup-sample-data.sh` before demo
- Run `./scripts/reset-demo.sh` to reset all drivers to AVAILABLE

### Issue 2: Dashboard Shows "0" for All Statistics

**Symptoms:**
- Dashboard shows all zeros
- No rides in the ride list

**Solution:**
1. Check if backend is running:
   ```bash
   docker ps | grep backend
   ```
2. Check backend logs:
   ```bash
   docker-compose logs backend
   ```
3. Create a test ride from **Request Ride** tab
4. Click **"Refresh"** button in Dashboard (if available)

### Issue 3: WebSocket Not Connecting

**Symptoms:**
- Real-time updates not working
- Dashboard not updating automatically

**Solution:**
1. Check browser console for WebSocket errors (F12 → Console)
2. Verify WebSocket endpoint: `ws://localhost:8080/ws`
3. Check if backend WebSocket is enabled:
   ```bash
   docker-compose logs backend | grep WebSocket
   ```
4. Refresh the page
5. Check browser network tab for WebSocket connection

### Issue 4: Driver Panel "Start Trip" Button Missing

**Symptoms:**
- After accepting a ride, "Start Trip" button doesn't appear
- Ride stuck in ACCEPTED status

**Solution:**
1. **Don't switch tabs** immediately after accepting
2. Wait for the ride status to update
3. If button is missing:
   - Refresh the Driver Panel
   - Re-enter the Driver ID
   - The button should appear if ride is in ACCEPTED status

**Note:** The button state is persisted in localStorage, so refreshing should restore it.

### Issue 5: Location Updates Not Working

**Symptoms:**
- Driver location update fails
- Error: "Validation failed"

**Solution:**
1. Ensure **driverId** is included in the request body
2. Check the request format:
   ```json
   {
     "driverId": 1,
     "latitude": 28.6139,
     "longitude": 77.2090
   }
   ```
3. Verify coordinates are valid (latitude: -90 to 90, longitude: -180 to 180)

### Issue 6: Payment Processing Fails

**Symptoms:**
- Payment status shows FAILED
- Error message about payment gateway

**Solution:**
1. This is expected behavior for demo (mock payment gateway)
2. Payment processing is simulated
3. Check payment status in the Dashboard or Trip details
4. For production, integrate with real PSP

### Issue 7: Page Refreshes to Dashboard

**Symptoms:**
- When you refresh, always goes back to Dashboard
- Loses current tab

**Solution:**
- This is expected behavior
- The application remembers the last active tab in localStorage
- If it doesn't persist, check browser localStorage support

### Issue 8: Cannot Clear Input Fields

**Symptoms:**
- Cannot delete numbers in Rider ID or Driver ID fields
- Field always reverts to previous value

**Solution:**
1. This has been fixed in recent updates
2. You should be able to clear and enter new values
3. If issue persists, refresh the page

---

## Demo Script (Quick Reference)

### 5-Minute Demo Flow

1. **Setup (30 seconds)**
   ```bash
   ./scripts/setup-sample-data.sh
   ```

2. **Open Application (10 seconds)**
   - Navigate to http://localhost:3000

3. **Create Ride (30 seconds)**
   - Go to **Request Ride** tab
   - Click **"📍 Delhi to Noida"** quick location
   - Select **Economy** tier
   - Click **"🚗 Request Ride"**

4. **Show Dashboard (20 seconds)**
   - Go to **Dashboard** tab
   - Show statistics and ride list
   - Explain real-time updates

5. **Driver Accepts (30 seconds)**
   - Go to **Driver Panel** tab
   - Enter Driver ID: `1`
   - Click **"Accept Ride"**
   - Show status change

6. **Start Trip (20 seconds)**
   - Click **"Start Trip"** button
   - Show trip started

7. **End Trip (30 seconds)**
   - Click **"End Trip"** button
   - Enter distance: `15.5`
   - Show final fare calculation

8. **Show Other Features (2 minutes)**
   - **Rider Details**: Show rider info and ride history
   - **Driver Details**: Show driver info and ride history
   - **Create New Rider/Driver**: Demonstrate CRUD operations

### 10-Minute Comprehensive Demo

Follow the complete **Scenario 1: Complete Ride Flow** with all steps, including:
- Multiple ride creations
- Different vehicle tiers
- Surge pricing demonstration
- Payment processing
- Error handling (show failure cases)

---

## Tips for Successful Demo

1. **Prepare Before Demo:**
   - Run `setup-sample-data.sh` to ensure drivers are available
   - Test the flow once before the actual demo
   - Have backup driver IDs ready (1-10)

2. **During Demo:**
   - Start with Dashboard to show current state
   - Create a ride and show the matching process
   - Demonstrate real-time updates
   - Show error handling (what happens when no drivers available)

3. **Highlight Key Features:**
   - Real-time WebSocket updates
   - Driver-rider matching algorithm
   - Dynamic surge pricing
   - Complete trip lifecycle
   - CRUD operations for riders/drivers

4. **Common Questions to Address:**
   - "How does driver matching work?" → Explain proximity + rating + tier
   - "What if no drivers are available?" → Show failure reason and suggestions
   - "How is surge pricing calculated?" → Explain demand-based algorithm
   - "How does real-time updates work?" → Explain WebSocket

---

## Additional Resources

- **API Documentation:** http://localhost:8080/swagger-ui.html
- **Backend Logs:** `docker-compose logs -f backend`
- **Frontend Logs:** Browser console (F12)
- **Database:** Connect to PostgreSQL on port 5432
- **Redis:** Connect to Redis on port 6379

---

**Document Version:** 1.0  
**Last Updated:** 2026  
**For Questions:** Contact Tanmay Mallick
