import React, { useState, useEffect } from 'react';
import './DriverDetails.css';
import { getDriverById, getRidesByDriverId, createDriver, updateDriver } from '../services/api';
import Button from './common/Button';
import Input from './common/Input';

function DriverDetails() {
  const [mode, setMode] = useState('view'); // 'view', 'create', 'edit'
  const [driverIdInput, setDriverIdInput] = useState('');
  const [driver, setDriver] = useState(null);
  const [rides, setRides] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  // Form state
  const [formData, setFormData] = useState({
    name: '',
    phoneNumber: '',
    email: '',
    licenseNumber: '',
    vehicleNumber: '',
    vehicleTier: 'ECONOMY',
    status: 'AVAILABLE',
    region: '',
    rating: 5.0
  });

  useEffect(() => {
    if (mode === 'view' && driverIdInput) {
      const driverId = parseInt(driverIdInput);
      if (driverId && driverId > 0) {
        fetchDriverDetails(driverId);
        fetchDriverRides(driverId);
      }
    }
  }, [driverIdInput, mode]);

  useEffect(() => {
    if (mode === 'edit' && driver) {
      setFormData({
        name: driver.name || '',
        phoneNumber: driver.phoneNumber || '',
        email: driver.email || '',
        licenseNumber: driver.licenseNumber || '',
        vehicleNumber: driver.vehicleNumber || '',
        vehicleTier: driver.vehicleTier || 'ECONOMY',
        status: driver.status || 'AVAILABLE',
        region: driver.region || '',
        rating: driver.rating || 5.0
      });
    } else if (mode === 'create') {
      setFormData({
        name: '',
        phoneNumber: '',
        email: '',
        licenseNumber: '',
        vehicleNumber: '',
        vehicleTier: 'ECONOMY',
        status: 'AVAILABLE',
        region: '',
        rating: 5.0
      });
    }
  }, [mode, driver]);

  const fetchDriverDetails = async (id) => {
    if (!id || isNaN(id) || id <= 0) return;
    setLoading(true);
    setError(null);
    try {
      const response = await getDriverById(id);
      if (response && response.success) {
        setDriver(response.data);
      } else if (response && response.data) {
        setDriver(response.data);
      }
    } catch (err) {
      const errorMessage = err.response?.data?.message || err.message || 'Network Error';
      setError('Failed to fetch driver details: ' + errorMessage);
      setDriver(null);
    } finally {
      setLoading(false);
    }
  };

  const fetchDriverRides = async (id) => {
    if (!id || isNaN(id) || id <= 0) return;
    try {
      const response = await getRidesByDriverId(id);
      if (response && response.success && Array.isArray(response.data)) {
        setRides(response.data);
      } else if (Array.isArray(response)) {
        setRides(response);
      } else if (response && response.data && Array.isArray(response.data)) {
        setRides(response.data);
      }
    } catch (err) {
      console.error('Error fetching rides:', err);
    }
  };

  const handleCreate = async () => {
    setLoading(true);
    setError(null);
    setSuccess(null);
    try {
      const response = await createDriver(formData);
      if (response && response.success) {
        setSuccess('Driver created successfully! ID: ' + response.data.id);
        setDriver(response.data);
        setDriverIdInput(response.data.id.toString());
        setMode('view');
        fetchDriverRides(response.data.id);
      }
    } catch (err) {
      const errorMessage = err.response?.data?.message || err.message || 'Failed to create driver';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdate = async () => {
    if (!driver || !driver.id) {
      setError('No driver selected for update');
      return;
    }
    setLoading(true);
    setError(null);
    setSuccess(null);
    try {
      const response = await updateDriver(driver.id, formData);
      if (response && response.success) {
        setSuccess('Driver updated successfully!');
        setDriver(response.data);
        setMode('view');
        fetchDriverRides(driver.id);
      }
    } catch (err) {
      const errorMessage = err.response?.data?.message || err.message || 'Failed to update driver';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    const colors = {
      REQUESTED: '#6c757d', SEARCHING: '#ffc107', MATCHED: '#17a2b8',
      ACCEPTED: '#28a745', IN_PROGRESS: '#007bff', COMPLETED: '#28a745',
      CANCELLED: '#dc3545', FAILED: '#dc3545',
    };
    return colors[status] || '#6c757d';
  };

  const getStatusIcon = (status) => {
    const icons = {
      REQUESTED: '📝', SEARCHING: '🔍', MATCHED: '🤝', ACCEPTED: '✅',
      IN_PROGRESS: '🚗', COMPLETED: '✔️', CANCELLED: '❌', FAILED: '⚠️',
    };
    return icons[status] || '📄';
  };

  const getStatusColorForDriver = (status) => {
    const colors = {
      AVAILABLE: '#10b981',
      BUSY: '#f59e0b',
      ON_RIDE: '#3b82f6',
      OFFLINE: '#6c757d',
    };
    return colors[status] || '#6c757d';
  };

  return (
    <div className="driver-details">
      <h2>Driver Details</h2>

      <div className="mode-tabs">
        <button
          className={mode === 'view' ? 'active' : ''}
          onClick={() => setMode('view')}
        >
          View Driver
        </button>
        <button
          className={mode === 'create' ? 'active' : ''}
          onClick={() => setMode('create')}
        >
          Create Driver
        </button>
        {driver && (
          <button
            className={mode === 'edit' ? 'active' : ''}
            onClick={() => setMode('edit')}
          >
            Edit Driver
          </button>
        )}
      </div>

      {mode === 'view' && (
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
            <Button
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
            >
              🔄 Refresh
            </Button>
          </div>

          {loading && <p>Loading driver details...</p>}
          {error && <div className="error-message">{error}</div>}
          {success && <div className="success-message">{success}</div>}

          {driver && (
            <div className="driver-info">
              <div className="info-row">
                <span className="label">ID:</span>
                <span className="value">{driver.id}</span>
              </div>
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
      )}

      {(mode === 'create' || mode === 'edit') && (
        <div className="driver-form-card">
          <h3>{mode === 'create' ? 'Create New Driver' : 'Edit Driver'}</h3>
          {error && <div className="error-message">{error}</div>}
          {success && <div className="success-message">{success}</div>}

          <div className="form-section">
            <Input
              label="Name *"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              placeholder="Enter driver name"
            />
            <Input
              label="Phone Number *"
              type="tel"
              value={formData.phoneNumber}
              onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
              placeholder="+919876543210"
            />
            <Input
              label="Email *"
              type="email"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              placeholder="driver@example.com"
            />
            <Input
              label="License Number *"
              value={formData.licenseNumber}
              onChange={(e) => setFormData({ ...formData, licenseNumber: e.target.value })}
              placeholder="DL1234567890"
            />
            <Input
              label="Vehicle Number *"
              value={formData.vehicleNumber}
              onChange={(e) => setFormData({ ...formData, vehicleNumber: e.target.value })}
              placeholder="DL-01-AB-1234"
            />
            <div className="form-group">
              <label>Vehicle Tier *</label>
              <select
                value={formData.vehicleTier}
                onChange={(e) => setFormData({ ...formData, vehicleTier: e.target.value })}
                className="input"
              >
                <option value="ECONOMY">ECONOMY</option>
                <option value="PREMIUM">PREMIUM</option>
                <option value="LUXURY">LUXURY</option>
              </select>
            </div>
            <div className="form-group">
              <label>Status *</label>
              <select
                value={formData.status}
                onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                className="input"
              >
                <option value="AVAILABLE">AVAILABLE</option>
                <option value="BUSY">BUSY</option>
                <option value="ON_RIDE">ON_RIDE</option>
                <option value="OFFLINE">OFFLINE</option>
              </select>
            </div>
            <Input
              label="Region *"
              value={formData.region}
              onChange={(e) => setFormData({ ...formData, region: e.target.value })}
              placeholder="Delhi-NCR"
            />
            <Input
              label="Rating"
              type="number"
              step="0.1"
              min="0"
              max="5"
              value={formData.rating}
              onChange={(e) => setFormData({ ...formData, rating: parseFloat(e.target.value) || 5.0 })}
              placeholder="5.0"
            />
          </div>

          <div className="form-actions">
            <Button
              variant="primary"
              onClick={mode === 'create' ? handleCreate : handleUpdate}
              disabled={loading}
              fullWidth
            >
              {loading ? 'Processing...' : mode === 'create' ? 'Create Driver' : 'Update Driver'}
            </Button>
            <Button
              variant="secondary"
              onClick={() => {
                setMode('view');
                setError(null);
                setSuccess(null);
              }}
              fullWidth
            >
              Cancel
            </Button>
          </div>
        </div>
      )}

      {mode === 'view' && (
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
      )}
    </div>
  );
}

export default DriverDetails;
