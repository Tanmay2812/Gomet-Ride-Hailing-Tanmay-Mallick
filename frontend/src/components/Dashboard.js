import React, { useState, useEffect } from 'react';
import './Dashboard.css';
import wsService from '../services/websocket';

// GLOBAL - runs when module loads
console.log('[Dashboard] 📦 MODULE LOADED - Dashboard.js imported!');

function Dashboard() {
  console.log('[Dashboard] 🎯🎯🎯 FUNCTION EXECUTING - Dashboard component rendering!');
  
  const [rides, setRides] = useState([]);
  const [stats, setStats] = useState({
    total: 0,
    active: 0,
    completed: 0,
  });
  const [loading, setLoading] = useState(true);
  
  console.log('[Dashboard] State initialized - rides:', rides.length, 'loading:', loading);

  // Calculate stats whenever rides change
  useEffect(() => {
    console.log('[Dashboard] 📊 Stats useEffect - rides changed:', rides.length);
    const active = rides.filter(r => 
      ['SEARCHING', 'MATCHED', 'ACCEPTED', 'IN_PROGRESS'].includes(r.status)
    ).length;
    const completed = rides.filter(r => r.status === 'COMPLETED').length;
    
    setStats({
      total: rides.length,
      active,
      completed,
    });
    console.log('[Dashboard] 📊 Stats updated - total:', rides.length, 'active:', active, 'completed:', completed);
  }, [rides]);

  // Fetch all rides on mount - SIMPLE DIRECT FETCH
  useEffect(() => {
    console.log('[Dashboard] ⚡⚡⚡ useEffect MOUNTED - Starting fetch!');
    
    const fetchRides = async () => {
      try {
        console.log('[Dashboard] 🚀 Starting fetch to http://localhost:8080/v1/rides?limit=100');
        setLoading(true);
        
        const response = await fetch('http://localhost:8080/v1/rides?limit=100');
        console.log('[Dashboard] 📡 Fetch response status:', response.status, response.ok);
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        console.log('[Dashboard] 📦 Parsed JSON:', data);
        console.log('[Dashboard] 📦 Data keys:', Object.keys(data));
        console.log('[Dashboard] 📦 Success?', data.success);
        console.log('[Dashboard] 📦 Data array?', Array.isArray(data.data));
        console.log('[Dashboard] 📦 Data length:', data.data?.length);
        
        if (data.success && Array.isArray(data.data)) {
          console.log(`[Dashboard] ✅✅✅ SUCCESS! Found ${data.data.length} rides!`);
          console.log('[Dashboard] Setting rides to state...');
          setRides(data.data);
          console.log('[Dashboard] Rides set! New state should have', data.data.length, 'rides');
          
          // Log each ride
          data.data.forEach((ride, idx) => {
            console.log(`[Dashboard]   Ride ${idx + 1}: #${ride.id} - ${ride.status}`);
          });
        } else {
          console.log('[Dashboard] ⚠️ Unexpected response format:', data);
        }
      } catch (error) {
        console.error('[Dashboard] ❌❌❌ FETCH ERROR:', error);
        console.error('[Dashboard] Error message:', error.message);
        console.error('[Dashboard] Error stack:', error.stack);
      } finally {
        console.log('[Dashboard] Setting loading to false');
        setLoading(false);
      }
    };

    // Call immediately
    fetchRides();
    
    // Also set up interval for refresh
    const interval = setInterval(() => {
      console.log('[Dashboard] 🔄 Auto-refresh triggered');
      fetchRides();
    }, 10000);

    return () => {
      console.log('[Dashboard] 🧹 Cleanup - clearing interval');
      clearInterval(interval);
    };
  }, []); // Empty deps - only on mount

  useEffect(() => {
    // Connect to WebSocket
    console.log('[Dashboard] 🔌 Setting up WebSocket...');
    wsService.connect(
      () => {
        console.log('[Dashboard] ✅ WebSocket connected');
        
        // Subscribe to ride updates
        wsService.subscribe('/topic/rides/updates', (rideUpdate) => {
          console.log('[Dashboard] 📨 Received ride update:', rideUpdate);
          updateRideInList(rideUpdate);
        });
      },
      (error) => {
        console.error('[Dashboard] ❌ WebSocket error:', error);
      }
    );

    return () => {
      console.log('[Dashboard] 🧹 WebSocket cleanup');
      wsService.unsubscribe('/topic/rides/updates');
    };
  }, []);

  const updateRideInList = (rideData) => {
    console.log('[Dashboard] 🔄 Updating ride in list:', rideData.id);
    setRides(prevRides => {
      const existingIndex = prevRides.findIndex(r => r.id === rideData.id);
      if (existingIndex >= 0) {
        // Update existing ride
        const updated = [...prevRides];
        updated[existingIndex] = rideData;
        console.log('[Dashboard] ✅ Updated existing ride');
        return updated;
      } else {
        // Add new ride
        console.log('[Dashboard] ✅ Added new ride');
        return [rideData, ...prevRides];
      }
    });
  };

  const handleManualRefresh = async () => {
    console.log('[Dashboard] 🔄 Manual refresh button clicked!');
    setLoading(true);
    try {
      const response = await fetch('http://localhost:8080/v1/rides?limit=100');
      const data = await response.json();
      if (data.success && Array.isArray(data.data)) {
        console.log(`[Dashboard] ✅ Manual refresh: ${data.data.length} rides`);
        setRides(data.data);
      }
    } catch (error) {
      console.error('[Dashboard] ❌ Manual refresh error:', error);
    } finally {
      setLoading(false);
    }
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

  console.log('[Dashboard] 🎨 RENDERING - rides:', rides.length, 'stats:', stats, 'loading:', loading);

  // TEST: Direct API call on render (for debugging)
  React.useEffect(() => {
    console.log('[Dashboard] 🧪 TEST: Running direct fetch in separate effect...');
    fetch('http://localhost:8080/v1/rides?limit=100')
      .then(res => res.json())
      .then(data => {
        console.log('[Dashboard] 🧪 TEST: Direct fetch result:', data);
        if (data.success && data.data) {
          console.log('[Dashboard] 🧪 TEST: Found', data.data.length, 'rides!');
          setRides(data.data);
        }
      })
      .catch(err => console.error('[Dashboard] 🧪 TEST: Fetch error:', err));
  }, []);

  return (
    <div className="dashboard">
      <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem'}}>
        <h2>Real-Time Ride Dashboard</h2>
        <div style={{display: 'flex', gap: '0.5rem'}}>
          <button 
            onClick={handleManualRefresh}
            style={{
              padding: '0.5rem 1rem',
              background: '#667eea',
              color: 'white',
              border: 'none',
              borderRadius: '5px',
              cursor: 'pointer',
              fontSize: '0.9rem',
              fontWeight: 'bold'
            }}
          >
            🔄 Refresh
          </button>
          <button 
            onClick={async () => {
              console.log('[Dashboard] 🧪 TEST BUTTON CLICKED!');
              try {
                const res = await fetch('http://localhost:8080/v1/rides?limit=100');
                const data = await res.json();
                console.log('[Dashboard] 🧪 TEST BUTTON RESULT:', data);
                alert(`Found ${data.data?.length || 0} rides! Check console for details.`);
                if (data.success && data.data) {
                  setRides(data.data);
                }
              } catch (err) {
                console.error('[Dashboard] 🧪 TEST BUTTON ERROR:', err);
                alert('Error: ' + err.message);
              }
            }}
            style={{
              padding: '0.5rem 1rem',
              background: '#10b981',
              color: 'white',
              border: 'none',
              borderRadius: '5px',
              cursor: 'pointer',
              fontSize: '0.9rem',
              fontWeight: 'bold'
            }}
          >
            🧪 Test API
          </button>
        </div>
      </div>
      
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
        {loading ? (
          <div className="empty-state">
            <p>Loading rides...</p>
          </div>
        ) : rides.length === 0 ? (
          <div className="empty-state">
            <p>No rides yet. Create a ride request to see live updates here!</p>
            <p style={{marginTop: '1rem', fontSize: '0.9rem', color: '#666'}}>
              Debug: rides.length = {rides.length}, loading = {loading.toString()}
            </p>
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
