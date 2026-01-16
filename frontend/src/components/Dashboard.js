import React, { useState, useEffect } from 'react';
import './Dashboard.css';
import wsService from '../services/websocket';

function Dashboard() {
  const [rides, setRides] = useState([]);
  const [stats, setStats] = useState({
    total: 0,
    active: 0,
    completed: 0,
  });

  useEffect(() => {
    // Connect to WebSocket
    wsService.connect(
      () => {
        console.log('Dashboard connected to WebSocket');
        
        // Subscribe to ride updates
        wsService.subscribe('/topic/rides/updates', (rideUpdate) => {
          console.log('Received ride update:', rideUpdate);
          updateRideInList(rideUpdate);
        });
      },
      (error) => {
        console.error('WebSocket connection error:', error);
      }
    );

    return () => {
      wsService.unsubscribe('/topic/rides/updates');
    };
  }, []);

  useEffect(() => {
    // Calculate stats whenever rides change
    const active = rides.filter(r => 
      ['SEARCHING', 'MATCHED', 'ACCEPTED', 'IN_PROGRESS'].includes(r.status)
    ).length;
    const completed = rides.filter(r => r.status === 'COMPLETED').length;
    
    setStats({
      total: rides.length,
      active,
      completed,
    });
  }, [rides]);

  const updateRideInList = (rideData) => {
    setRides(prevRides => {
      const existingIndex = prevRides.findIndex(r => r.id === rideData.id);
      if (existingIndex >= 0) {
        // Update existing ride
        const updated = [...prevRides];
        updated[existingIndex] = rideData;
        return updated;
      } else {
        // Add new ride
        return [rideData, ...prevRides];
      }
    });
  };

  const getStatusColor = (status) => {
    const colors = {
      REQUESTED: '#6c757d',
      SEARCHING: '#ffc107',
      MATCHED: '#17a2b8',
      ACCEPTED: '#28a745',
      IN_PROGRESS: '#007bff',
      COMPLETED: '#28a745',
      CANCELLED: '#dc3545',
      FAILED: '#dc3545',
    };
    return colors[status] || '#6c757d';
  };

  const getStatusIcon = (status) => {
    const icons = {
      REQUESTED: '📝',
      SEARCHING: '🔍',
      MATCHED: '🤝',
      ACCEPTED: '✅',
      IN_PROGRESS: '🚗',
      COMPLETED: '✔️',
      CANCELLED: '❌',
      FAILED: '⚠️',
    };
    return icons[status] || '📄';
  };

  return (
    <div className="dashboard">
      <h2>Real-Time Ride Dashboard</h2>
      
      <div className="stats-container">
        <div className="stat-card">
          <div className="stat-icon">📊</div>
          <div className="stat-value">{stats.total}</div>
          <div className="stat-label">Total Rides</div>
        </div>
        <div className="stat-card active">
          <div className="stat-icon">🚗</div>
          <div className="stat-value">{stats.active}</div>
          <div className="stat-label">Active Rides</div>
        </div>
        <div className="stat-card completed">
          <div className="stat-icon">✅</div>
          <div className="stat-value">{stats.completed}</div>
          <div className="stat-label">Completed</div>
        </div>
      </div>

      <div className="rides-container">
        <h3>Live Ride Updates</h3>
        {rides.length === 0 ? (
          <div className="empty-state">
            <p>No rides yet. Create a ride request to see live updates here!</p>
          </div>
        ) : (
          <div className="rides-list">
            {rides.map(ride => (
              <div key={ride.id} className="ride-card">
                <div className="ride-header">
                  <span className="ride-id">Ride #{ride.id}</span>
                  <span 
                    className="ride-status" 
                    style={{ backgroundColor: getStatusColor(ride.status) }}
                  >
                    {getStatusIcon(ride.status)} {ride.status}
                  </span>
                </div>
                <div className="ride-details">
                  <div className="detail-row">
                    <span className="label">Rider ID:</span>
                    <span className="value">{ride.riderId}</span>
                  </div>
                  {ride.driverId && (
                    <div className="detail-row">
                      <span className="label">Driver ID:</span>
                      <span className="value">{ride.driverId}</span>
                    </div>
                  )}
                  <div className="detail-row">
                    <span className="label">Vehicle Tier:</span>
                    <span className="value">{ride.vehicleTier}</span>
                  </div>
                  <div className="detail-row">
                    <span className="label">Pickup:</span>
                    <span className="value">{ride.pickupAddress}</span>
                  </div>
                  <div className="detail-row">
                    <span className="label">Destination:</span>
                    <span className="value">{ride.destinationAddress}</span>
                  </div>
                  {ride.estimatedFare && (
                    <div className="detail-row">
                      <span className="label">Estimated Fare:</span>
                      <span className="value fare">₹{ride.estimatedFare.toFixed(2)}</span>
                    </div>
                  )}
                  {ride.surgeMultiplier > 1 && (
                    <div className="detail-row">
                      <span className="label">Surge:</span>
                      <span className="value surge">{ride.surgeMultiplier}x</span>
                    </div>
                  )}
                </div>
                {ride.driverInfo && (
                  <div className="driver-info">
                    <h4>Driver Information</h4>
                    <p><strong>Name:</strong> {ride.driverInfo.name}</p>
                    <p><strong>Phone:</strong> {ride.driverInfo.phoneNumber}</p>
                    <p><strong>Vehicle:</strong> {ride.driverInfo.vehicleNumber}</p>
                    <p><strong>Rating:</strong> ⭐ {ride.driverInfo.rating}</p>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default Dashboard;
