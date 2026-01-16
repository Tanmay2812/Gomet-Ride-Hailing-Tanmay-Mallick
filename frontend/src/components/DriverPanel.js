import React, { useState, useEffect } from 'react';
import './DriverPanel.css';
import { acceptRide, updateDriverLocation, startTrip, endTrip } from '../services/api';
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

  useEffect(() => {
    // Connect to WebSocket for driver notifications
    wsService.connect(
      () => {
        console.log('Driver panel connected to WebSocket');
        
        // Subscribe to driver-specific notifications
        wsService.subscribe(`/topic/driver/${driverId}`, (notification) => {
          console.log('Driver notification:', notification);
          
          if (notification.eventType === 'NEW_RIDE_REQUEST') {
            const rideData = notification.data;
            setPendingRides(prev => {
              const exists = prev.find(r => r.rideId === rideData.rideId);
              if (!exists) {
                return [...prev, rideData];
              }
              return prev;
            });
            showMessage('New ride request received!', 'success');
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
      
      setActiveRide(response.data);
      setPendingRides(prev => prev.filter(r => r.rideId !== rideId));
      showMessage('Ride accepted successfully!', 'success');
    } catch (error) {
      showMessage('Failed to accept ride', 'error');
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
      await endTrip(activeRide.id, {
        endLatitude: location.latitude,
        endLongitude: location.longitude,
        distanceKm: 15.5 // In real app, this would be calculated
      });
      
      showMessage('Trip completed!', 'success');
      setActiveRide(null);
    } catch (error) {
      showMessage('Failed to end trip', 'error');
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
        <h3>Pending Ride Requests ({pendingRides.length})</h3>
        {pendingRides.length === 0 ? (
          <div className="empty-state">
            <p>No pending ride requests</p>
            <p className="hint">Waiting for ride assignments...</p>
          </div>
        ) : (
          <div className="rides-list">
            {pendingRides.map((ride, idx) => (
              <div key={idx} className="ride-request-card">
                <div className="ride-header">
                  <span className="ride-id">Ride #{ride.rideId}</span>
                  <span className="new-badge">NEW</span>
                </div>
                <div className="ride-details">
                  <p><strong>Pickup:</strong> {ride.pickupAddress}</p>
                  <p><strong>Pickup Location:</strong> ({ride.pickupLatitude}, {ride.pickupLongitude})</p>
                </div>
                <button
                  className="accept-btn"
                  onClick={() => handleAcceptRide(ride.rideId)}
                  disabled={loading}
                >
                  ✅ Accept Ride
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default DriverPanel;
