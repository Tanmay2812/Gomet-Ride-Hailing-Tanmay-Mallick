import React, { useState, useEffect } from 'react';
import './DriverPanel.css';
import { acceptRide, updateDriverLocation, startTrip, endTrip, getPendingRides, getTripByRideId } from '../services/api';
import wsService from '../services/websocket';

function DriverPanel() {
  const [driverId, setDriverId] = useState(1);
  const [location, setLocation] = useState({
    latitude: 28.6139,
    longitude: 77.2090
  });
  const [pendingRides, setPendingRides] = useState([]);
  const [activeRide, setActiveRide] = useState(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null);

  // Fetch pending rides on mount and when driverId changes
  useEffect(() => {
    console.log(`[DriverPanel] ⚡ useEffect triggered - Driver ID: ${driverId}`);
    if (driverId && driverId > 0) {
      console.log(`[DriverPanel] ✅ Valid driver ID, fetching pending rides...`);
      fetchPendingRides();
      
      // Poll for pending rides every 5 seconds
      const interval = setInterval(() => {
        console.log(`[DriverPanel] 🔄 Polling for pending rides (driver ${driverId})...`);
        fetchPendingRides();
      }, 5000);
      
      return () => {
        console.log(`[DriverPanel] 🧹 Cleaning up polling interval for driver ${driverId}`);
        clearInterval(interval);
      };
    } else {
      console.log(`[DriverPanel] ⚠️ Invalid driver ID (${driverId}), not fetching`);
      setPendingRides([]);
    }
  }, [driverId]);

  const fetchPendingRides = async () => {
    if (!driverId || driverId <= 0) {
      console.log('[DriverPanel] ⚠️ Driver ID not set, skipping fetch');
      return;
    }
    
    const apiUrl = `http://localhost:8080/v1/drivers/${driverId}/pending-rides`;
    console.log(`[DriverPanel] 🔍 Fetching from: ${apiUrl}`);
    
    try {
      // Try axios first (with interceptors)
      console.log(`[DriverPanel] 📡 Calling API via axios...`);
      const response = await getPendingRides(driverId);
      console.log('[DriverPanel] 📦 Axios response received:', response);
      console.log('[DriverPanel] 📦 Response type:', typeof response);
      console.log('[DriverPanel] 📦 Is array?', Array.isArray(response));
      
      // Handle axios interceptor - it returns response.data directly
      // So response is already {success: true, data: [...], message: "..."}
      let rides = [];
      
      if (response) {
        if (response.success !== undefined && response.data !== undefined) {
          // Standard API response format
          rides = Array.isArray(response.data) ? response.data : [];
          console.log(`[DriverPanel] ✅ Found ${rides.length} pending ride(s) (standard format)`);
        } else if (Array.isArray(response)) {
          // Direct array response
          rides = response;
          console.log(`[DriverPanel] ✅ Found ${rides.length} pending ride(s) (direct array)`);
        } else if (response.data && Array.isArray(response.data)) {
          // Nested data
          rides = response.data;
          console.log(`[DriverPanel] ✅ Found ${rides.length} pending ride(s) (nested data)`);
        } else {
          console.log('[DriverPanel] ⚠️ Unexpected response format');
          console.log('[DriverPanel] Full response:', JSON.stringify(response, null, 2));
          
          // Fallback: Try direct fetch
          console.log('[DriverPanel] 🔄 Trying direct fetch as fallback...');
          const fetchResponse = await fetch(apiUrl);
          const fetchData = await fetchResponse.json();
          console.log('[DriverPanel] 📦 Direct fetch response:', fetchData);
          
          if (fetchData.success && Array.isArray(fetchData.data)) {
            rides = fetchData.data;
            console.log(`[DriverPanel] ✅ Found ${rides.length} pending ride(s) via direct fetch`);
          }
        }
      }
      
      if (rides.length > 0) {
        console.log(`[DriverPanel] 🎉 Setting ${rides.length} pending ride(s) to state`);
        rides.forEach(ride => {
          console.log(`[DriverPanel]   📍 Ride #${ride.id || ride.rideId}: ${ride.pickupAddress || ride.pickup} → ${ride.destinationAddress || ride.destination}`);
        });
        setPendingRides(rides);
      } else {
        console.log('[DriverPanel] ⚠️ No pending rides found - setting empty array');
        setPendingRides([]);
      }
    } catch (error) {
      console.error('[DriverPanel] ❌ Error fetching pending rides:', error);
      console.error('[DriverPanel] Error name:', error.name);
      console.error('[DriverPanel] Error message:', error.message);
      console.error('[DriverPanel] Error response:', error.response?.data);
      console.error('[DriverPanel] Error status:', error.response?.status);
      
      // Try direct fetch as fallback
      try {
        console.log('[DriverPanel] 🔄 Trying direct fetch as error fallback...');
        const fetchResponse = await fetch(apiUrl);
        if (fetchResponse.ok) {
          const fetchData = await fetchResponse.json();
          console.log('[DriverPanel] 📦 Direct fetch success:', fetchData);
          if (fetchData.success && Array.isArray(fetchData.data)) {
            console.log(`[DriverPanel] ✅ Found ${fetchData.data.length} pending ride(s) via direct fetch fallback`);
            setPendingRides(fetchData.data);
            return;
          }
        }
      } catch (fetchError) {
        console.error('[DriverPanel] ❌ Direct fetch also failed:', fetchError);
      }
      
      setPendingRides([]);
    }
  };

  useEffect(() => {
    // Connect to WebSocket for driver notifications
    wsService.connect(
      () => {
        console.log('Driver panel connected to WebSocket');
        
        // Subscribe to driver-specific notifications
        wsService.subscribe(`/topic/driver/${driverId}`, (notification) => {
          console.log('Driver notification:', notification);
          
          if (notification.eventType === 'NEW_RIDE_REQUEST') {
            showMessage('New ride request received!', 'success');
            fetchPendingRides(); // Refresh the list
          }
        });
      },
      (error) => {
        console.error('WebSocket connection error:', error);
      }
    );

    return () => {
      wsService.unsubscribe(`/topic/driver/${driverId}`);
    };
  }, [driverId]);

  const showMessage = (text, type = 'info') => {
    setMessage({ text, type });
    setTimeout(() => setMessage(null), 5000);
  };

  const handleAcceptRide = async (rideId) => {
    setLoading(true);
    try {
      const response = await acceptRide(driverId, {
        rideId: rideId,
        driverId: driverId,
        currentLatitude: location.latitude,
        currentLongitude: location.longitude
      });
      
      // Handle response structure
      const rideData = response.success ? response.data : response;
      setActiveRide(rideData);
      
      // Remove from pending rides (check both id and rideId)
      setPendingRides(prev => prev.filter(r => (r.id || r.rideId) !== rideId));
      
      // Refresh pending rides list
      fetchPendingRides();
      
      showMessage('Ride accepted successfully!', 'success');
    } catch (error) {
      console.error('Accept ride error:', error);
      showMessage('Failed to accept ride: ' + (error.response?.data?.message || error.message), 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleStartTrip = async () => {
    if (!activeRide) return;
    
    setLoading(true);
    try {
      await startTrip(activeRide.id);
      setActiveRide(prev => ({ ...prev, status: 'IN_PROGRESS' }));
      showMessage('Trip started!', 'success');
    } catch (error) {
      showMessage('Failed to start trip', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleEndTrip = async () => {
    if (!activeRide) return;
    
    setLoading(true);
    try {
      // Get trip ID from ride ID
      const tripResponse = await getTripByRideId(activeRide.id);
      const tripId = tripResponse.success ? tripResponse.data.id : tripResponse.data?.id;
      
      if (!tripId) {
        throw new Error('Trip not found for this ride');
      }
      
      await endTrip(tripId, {
        tripId: tripId,
        endLatitude: location.latitude,
        endLongitude: location.longitude,
        distanceKm: 15.5 // In real app, this would be calculated
      });
      
      showMessage('Trip completed!', 'success');
      setActiveRide(null);
    } catch (error) {
      console.error('End trip error:', error);
      showMessage('Failed to end trip: ' + (error.response?.data?.message || error.message), 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateLocation = async () => {
    try {
      await updateDriverLocation(driverId, {
        driverId: driverId,
        latitude: location.latitude,
        longitude: location.longitude,
        timestamp: Date.now()
      });
      showMessage('Location updated', 'success');
    } catch (error) {
      showMessage('Failed to update location', 'error');
    }
  };

  return (
    <div className="driver-panel">
      <h2>Driver Panel</h2>

      {message && (
        <div className={`message ${message.type}`}>
          {message.text}
        </div>
      )}

      <div className="driver-info-card">
        <h3>Driver Information</h3>
        <div className="form-group">
          <label>Driver ID</label>
          <input
            type="number"
            value={driverId}
            onChange={(e) => setDriverId(Number(e.target.value))}
          />
        </div>
        <div className="location-group">
          <h4>Current Location</h4>
          <div className="form-row">
            <div className="form-group">
              <label>Latitude</label>
              <input
                type="number"
                step="any"
                value={location.latitude}
                onChange={(e) => setLocation(prev => ({ ...prev, latitude: parseFloat(e.target.value) }))}
              />
            </div>
            <div className="form-group">
              <label>Longitude</label>
              <input
                type="number"
                step="any"
                value={location.longitude}
                onChange={(e) => setLocation(prev => ({ ...prev, longitude: parseFloat(e.target.value) }))}
              />
            </div>
          </div>
          <button className="update-btn" onClick={handleUpdateLocation}>
            📍 Update Location
          </button>
        </div>
      </div>

      {activeRide && (
        <div className="active-ride-card">
          <h3>Active Ride</h3>
          <div className="ride-info">
            <p><strong>Ride ID:</strong> {activeRide.id}</p>
            <p><strong>Status:</strong> {activeRide.status}</p>
            <p><strong>Pickup:</strong> {activeRide.pickupAddress}</p>
            <p><strong>Destination:</strong> {activeRide.destinationAddress}</p>
            <p><strong>Estimated Fare:</strong> ₹{activeRide.estimatedFare?.toFixed(2)}</p>
          </div>
          <div className="action-buttons">
            {activeRide.status === 'ACCEPTED' && (
              <button 
                className="action-btn start" 
                onClick={handleStartTrip}
                disabled={loading}
              >
                🚗 Start Trip
              </button>
            )}
            {activeRide.status === 'IN_PROGRESS' && (
              <button 
                className="action-btn end" 
                onClick={handleEndTrip}
                disabled={loading}
              >
                🏁 End Trip
              </button>
            )}
          </div>
        </div>
      )}

      <div className="pending-rides-card">
        <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem'}}>
          <h3>Pending Ride Requests ({pendingRides.length})</h3>
          <div style={{display: 'flex', gap: '0.5rem'}}>
            <button 
              onClick={() => {
                console.log('[DriverPanel] Manual refresh clicked for driver', driverId);
                fetchPendingRides();
              }}
              style={{
                padding: '0.5rem 1rem',
                background: '#667eea',
                color: 'white',
                border: 'none',
                borderRadius: '5px',
                cursor: 'pointer',
                fontSize: '0.9rem'
              }}
            >
              🔄 Refresh
            </button>
            <button 
              onClick={async () => {
                console.log('[DriverPanel] Testing API directly...');
                try {
                  const url = `http://localhost:8080/v1/drivers/${driverId}/pending-rides`;
                  console.log('[DriverPanel] Calling:', url);
                  const response = await fetch(url);
                  const data = await response.json();
                  console.log('[DriverPanel] Direct API response:', JSON.stringify(data, null, 2));
                  alert(`API Test:\nStatus: ${response.status}\nRides: ${data.data?.length || 0}\nCheck console for details`);
                } catch (err) {
                  console.error('[DriverPanel] Direct API test failed:', err);
                  alert('API Test Failed - Check console');
                }
              }}
              style={{
                padding: '0.5rem 1rem',
                background: '#10b981',
                color: 'white',
                border: 'none',
                borderRadius: '5px',
                cursor: 'pointer',
                fontSize: '0.9rem'
              }}
            >
              🧪 Test API
            </button>
          </div>
        </div>
        {pendingRides.length === 0 ? (
          <div className="empty-state">
            <p>No pending ride requests</p>
            <p className="hint">Waiting for ride assignments...</p>
            <p className="hint" style={{marginTop: '0.5rem', fontSize: '0.85rem'}}>
              Make sure Driver ID matches the assigned driver (check ride details)
            </p>
          </div>
        ) : (
          <div className="rides-list">
            {pendingRides.map((ride, idx) => {
              const rideId = ride.id || ride.rideId || idx;
              return (
                <div key={rideId} className="ride-request-card">
                  <div className="ride-header">
                    <span className="ride-id">Ride #{rideId}</span>
                    <span className="new-badge">NEW</span>
                  </div>
                  <div className="ride-details">
                    <p><strong>Pickup:</strong> {ride.pickupAddress || ride.pickup}</p>
                    <p><strong>Destination:</strong> {ride.destinationAddress || ride.destination}</p>
                    {ride.estimatedFare && (
                      <p><strong>Estimated Fare:</strong> ₹{ride.estimatedFare.toFixed(2)}</p>
                    )}
                    {ride.vehicleTier && (
                      <p><strong>Vehicle Tier:</strong> {ride.vehicleTier}</p>
                    )}
                  </div>
                  <button
                    className="accept-btn"
                    onClick={() => handleAcceptRide(rideId)}
                    disabled={loading}
                  >
                    ✅ Accept Ride
                  </button>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}

export default DriverPanel;
