import React, { useState, useEffect } from 'react';
import './RiderDetails.css';
import { getRiderById, getRidesByRiderId } from '../services/api';

function RiderDetails() {
  const [riderIdInput, setRiderIdInput] = useState('1');
  const [rider, setRider] = useState(null);
  const [rides, setRides] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    const riderId = parseInt(riderIdInput);
    if (riderId && riderId > 0) {
      fetchRiderDetails(riderId);
      fetchRiderRides(riderId);
    }
  }, [riderIdInput]);

  const fetchRiderDetails = async (id = null) => {
    const riderId = id !== null ? id : parseInt(riderIdInput);
    if (!riderId || isNaN(riderId) || riderId <= 0) {
      setError('Please enter a valid Rider ID');
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      console.log('[RiderDetails] Fetching rider details for ID:', riderId);
      const response = await getRiderById(riderId);
      console.log('[RiderDetails] Response received:', response);
      if (response && response.success) {
        setRider(response.data);
      } else if (response && response.data) {
        setRider(response.data);
      } else {
        setError('Unexpected response format');
      }
    } catch (err) {
      console.error('[RiderDetails] Error fetching rider:', err);
      const errorMessage = err.response?.data?.message || err.message || 'Network Error';
      setError('Failed to fetch rider details: ' + errorMessage);
      // Clear rider data on error
      setRider(null);
    } finally {
      setLoading(false);
    }
  };

  const fetchRiderRides = async (id = null) => {
    const riderId = id !== null ? id : parseInt(riderIdInput);
    if (!riderId || isNaN(riderId) || riderId <= 0) return;
    try {
      console.log('[RiderDetails] Fetching rides for rider ID:', riderId);
      const response = await getRidesByRiderId(riderId);
      console.log('[RiderDetails] Rides response:', response);
      if (response && response.success && Array.isArray(response.data)) {
        setRides(response.data);
      } else if (Array.isArray(response)) {
        setRides(response);
      } else if (response && response.data && Array.isArray(response.data)) {
        setRides(response.data);
      }
    } catch (err) {
      console.error('[RiderDetails] Error fetching rides:', err);
      // Don't set error here, just log - rides are secondary
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

  return (
    <div className="rider-details">
      <h2>Rider Details</h2>
      
      <div className="rider-info-card">
        <h3>Rider Information</h3>
        <div className="form-group">
          <label>Rider ID</label>
          <input
            type="number"
            value={riderIdInput}
            onChange={(e) => setRiderIdInput(e.target.value)}
            min="1"
            placeholder="Enter Rider ID"
          />
          <button 
            onClick={() => {
              const id = parseInt(riderIdInput);
              if (!id || id <= 0) {
                setError('Please enter a valid Rider ID');
                return;
              }
              fetchRiderDetails(id);
              fetchRiderRides(id);
            }}
            className="refresh-btn"
            style={{
              marginTop: '0.5rem',
              padding: '0.5rem 1rem',
              background: '#667eea',
              color: 'white',
              border: 'none',
              borderRadius: '5px',
              cursor: 'pointer'
            }}
          >
            🔄 Refresh
          </button>
        </div>
        
        {loading && <p>Loading rider details...</p>}
        {error && <div className="error-message">{error}</div>}
        
        {rider && (
          <div className="rider-info">
            <div className="info-row">
              <span className="label">Name:</span>
              <span className="value">{rider.name}</span>
            </div>
            <div className="info-row">
              <span className="label">Email:</span>
              <span className="value">{rider.email}</span>
            </div>
            <div className="info-row">
              <span className="label">Phone:</span>
              <span className="value">{rider.phoneNumber}</span>
            </div>
            <div className="info-row">
              <span className="label">Region:</span>
              <span className="value">{rider.region}</span>
            </div>
            <div className="info-row">
              <span className="label">Rating:</span>
              <span className="value">⭐ {rider.rating?.toFixed(1) || '5.0'}</span>
            </div>
            <div className="info-row">
              <span className="label">Total Rides:</span>
              <span className="value">{rider.totalRides || 0}</span>
            </div>
          </div>
        )}
      </div>

      <div className="rides-card">
        <h3>Ride History ({rides.length})</h3>
        {rides.length === 0 ? (
          <div className="empty-state">
            <p>No rides found for this rider</p>
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
                  {ride.driverId && (
                    <p><strong>Driver ID:</strong> {ride.driverId}</p>
                  )}
                  <p><strong>Pickup:</strong> {ride.pickupAddress}</p>
                  <p><strong>Destination:</strong> {ride.destinationAddress}</p>
                  <p><strong>Vehicle Tier:</strong> {ride.vehicleTier}</p>
                  {ride.estimatedFare && (
                    <p><strong>Estimated Fare:</strong> ₹{ride.estimatedFare.toFixed(2)}</p>
                  )}
                  {ride.surgeMultiplier > 1 && (
                    <p><strong>Surge:</strong> {ride.surgeMultiplier}x</p>
                  )}
                  <p><strong>Created:</strong> {new Date(ride.createdAt).toLocaleString()}</p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default RiderDetails;
