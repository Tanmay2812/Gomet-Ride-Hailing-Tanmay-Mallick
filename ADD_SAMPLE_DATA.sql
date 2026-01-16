-- Add Sample Data for Testing
-- Run this in your database to test the application

-- Add a Rider
INSERT INTO riders (name, phone_number, email, region, rating, total_rides, created_at, updated_at)
VALUES ('Jane Rider', '+919876543211', 'jane@rider.com', 'Delhi-NCR', 5.0, 0, NOW(), NOW())
ON CONFLICT (phone_number) DO NOTHING;

-- Add a Driver
INSERT INTO drivers (name, phone_number, email, license_number, vehicle_number, vehicle_tier, status, region, rating, total_rides, created_at, updated_at)
VALUES ('John Driver', '+919876543210', 'john@driver.com', 'DL1234567890', 'DL-01-AB-1234', 'ECONOMY', 'AVAILABLE', 'Delhi-NCR', 5.0, 0, NOW(), NOW())
ON CONFLICT (phone_number) DO NOTHING;

-- Check what was inserted
SELECT 'Riders:' as info;
SELECT id, name, phone_number, region FROM riders;

SELECT 'Drivers:' as info;
SELECT id, name, phone_number, vehicle_tier, status, region FROM drivers;
