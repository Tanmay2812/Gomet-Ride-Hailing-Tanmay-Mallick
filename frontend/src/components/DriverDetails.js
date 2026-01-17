import React, { useState, useEffect } from 'react';
import './DriverDetails.css';
import { getDriverById, getRidesByDriverId } from '../services/api';

function DriverDetails() {
  const [driverIdInput, setDriverIdInput] = useState('1');
  const [driver, setDriver] = useState(null);
  const [rides, setRides] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    const driverId = parseInt(driverIdInput);
    if (driverId && driverId > 0) {
      fetchDriverDetails(driverId);
      fetchDriverRides(driverId);
    }
  }, [driverIdInput]);

  const fetchDriverDetails = async (id = null) => {
    const driverId = id !== null ? id : parseInt(driverIdInput);
    if (!driverId || isNaN(driverId) || driverId <= 0) {
      setError('Please enter a valid Driver ID');
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      console.log('[DriverDetails] Fetching driver details for ID:', driverId);
      const response = await getDriverById(driverId);
      console.log('[DriverDetails] Response received:', response);
      if (response && response.success) {
        setDriver(response.data);
      } else if (response && response.data) {
        setDriver(response.data);
      } else {
        setError('Unexpected response format');
      }
    } catch (err) {
      console.error('[DriverDetails] Error fetching driver:', err);
      const errorMessage = err.response?.data?.message || err.message || 'Network Error';
      setError('Failed to fetch driver details: ' + errorMessage);
      // Clear driver data on error
      setDriver(null);
    } finally {
      setLoading(false);
    }
  };

  const fetchDriverRides = async (id = null) => {
    const driverId = id !== null ? id : parseInt(driverIdInput);
    if (!driverId || isNaN(driverId) || driverId <= 0) return;
    try {
      console.log('[DriverDetails] Fetching rides for driver ID:', driverId);
      const response = await getRidesByDriverId(driverId);
      console.log('[DriverDetails] Rides response:', response);
      if (response && response.success && Array.isArray(response.data)) {
        setRides(response.data);
      } else if (Array.isArray(response)) {
        setRides(response);
      } else if (response && response.data && Array.isArray(response.data)) {
        setRides(response.data);
      }
    } catch (err) {
      console.error('[DriverDetails] Error fetching rides:', err);
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

  const getStatusColorForDriver = (status) => {
    const colors = {
      AVAILABLE: '#28a745',
      BUSY: '#ffc107',
      OFFLINE: '#6c757d',
    };
    return colors[status] || '#6c757d';
  };

  return (
    <div className="driver-details">
      <h2>Driver Details</h2>
      
      <div className="driver-info-card">
        <h3>Driver Information</h3>
        <div className="form-group">
          <label>Driver ID</label>
          <input
            type="number"
            value={driverIdInput}
            onChange={(e) => setDriverIdInput(e.target.value)}
            min="1"
            placeholder="Enter Driver ID"
          />
          <button 
            onClick={() => {
              const id = parseInt(driverIdInput);
              if (!id || id <= 0) {
                setError('Please enter a valid Driver ID');
                return;
              }
              fetchDriverDetails(id);
              fetchDriverRides(id);
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
        
        {loading && <p>Loading driver details...</p>}
        {error && <div className="error-message">{error}</div>}
        
        {driver && (
          <div className="driver-info">
            <div className="info-row">
              <span className="label">Name:</span>
              <span className="value">{driver.name}</span>
            </div>
            <div className="info-row">
              <span className="label">Email:</span>
              <span className="value">{driver.email}</span>
            </div>
            <div className="info-row">
              <span className="label">Phone:</span>
              <span className="value">{driver.phoneNumber}</span>
            </div>
            <div className="info-row">
              <span className="label">License:</span>
              <span className="value">{driver.licenseNumber}</span>
            </div>
            <div className="info-row">
              <span className="label">Vehicle Number:</span>
              <span className="value">{driver.vehicleNumber}</span>
            </div>
            <div className="info-row">
              <span className="label">Vehicle Tier:</span>
              <span className="value">{driver.vehicleTier}</span>
            </div>
            <div className="info-row">
              <span className="label">Region:</span>
              <span className="value">{driver.region}</span>
            </div>
            <div className="info-row">
              <span className="label">Status:</span>
              <span 
                className="value" 
                style={{ 
                  color: getStatusColorForDriver(driver.status),
                  fontWeight: 'bold'
                }}
              >
                {driver.status}
              </span>
            </div>
            <div className="info-row">
              <span className="label">Rating:</span>
              <span className="value">⭐ {driver.rating?.toFixed(1) || '5.0'}</span>
            </div>
            <div className="info-row">
              <span className="label">Total Rides:</span>
              <span className="value">{driver.totalRides || 0}</span>
            </div>
          </div>
        )}
      </div>

      <div className="rides-card">
        <h3>Ride History ({rides.length})</h3>
        {rides.length === 0 ? (
          <div className="empty-state">
            <p>No rides found for this driver</p>
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
                  <p><strong>Rider ID:</strong> {ride.riderId}</p>
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

export default DriverDetails;
