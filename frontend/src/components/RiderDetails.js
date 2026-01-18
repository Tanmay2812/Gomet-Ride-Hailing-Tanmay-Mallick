import React, { useState, useEffect } from 'react';
import './RiderDetails.css';
import { getRiderById, getRidesByRiderId, createRider, updateRider } from '../services/api';
import Button from './common/Button';
import Input from './common/Input';

function RiderDetails() {
  const [mode, setMode] = useState('view'); // 'view', 'create', 'edit'
  const [riderIdInput, setRiderIdInput] = useState('');
  const [rider, setRider] = useState(null);
  const [rides, setRides] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  // Form state
  const [formData, setFormData] = useState({
    name: '',
    phoneNumber: '',
    email: '',
    region: '',
    rating: 5.0
  });

  useEffect(() => {
    if (mode === 'view' && riderIdInput) {
      const riderId = parseInt(riderIdInput);
      if (riderId && riderId > 0) {
        fetchRiderDetails(riderId);
        fetchRiderRides(riderId);
      }
    }
  }, [riderIdInput, mode]);

  useEffect(() => {
    if (mode === 'edit' && rider) {
      setFormData({
        name: rider.name || '',
        phoneNumber: rider.phoneNumber || '',
        email: rider.email || '',
        region: rider.region || '',
        rating: rider.rating || 5.0
      });
    } else if (mode === 'create') {
      setFormData({
        name: '',
        phoneNumber: '',
        email: '',
        region: '',
        rating: 5.0
      });
    }
  }, [mode, rider]);

  const fetchRiderDetails = async (id) => {
    if (!id || isNaN(id) || id <= 0) return;
    setLoading(true);
    setError(null);
    try {
      const response = await getRiderById(id);
      if (response && response.success) {
        setRider(response.data);
      } else if (response && response.data) {
        setRider(response.data);
      }
    } catch (err) {
      const errorMessage = err.response?.data?.message || err.message || 'Network Error';
      setError('Failed to fetch rider details: ' + errorMessage);
      setRider(null);
    } finally {
      setLoading(false);
    }
  };

  const fetchRiderRides = async (id) => {
    if (!id || isNaN(id) || id <= 0) return;
    try {
      const response = await getRidesByRiderId(id);
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
      // Ensure rating has a default value if not provided
      const riderData = {
        ...formData,
        rating: formData.rating || 5.0
      };
      const response = await createRider(riderData);
      if (response && response.success) {
        setSuccess('Rider created successfully! ID: ' + response.data.id);
        setRider(response.data);
        setRiderIdInput(response.data.id.toString());
        setMode('view');
        fetchRiderRides(response.data.id);
      }
    } catch (err) {
      const errorMessage = err.response?.data?.message || err.message || 'Failed to create rider';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdate = async () => {
    if (!rider || !rider.id) {
      setError('No rider selected for update');
      return;
    }
    setLoading(true);
    setError(null);
    setSuccess(null);
    try {
      const response = await updateRider(rider.id, formData);
      if (response && response.success) {
        setSuccess('Rider updated successfully!');
        setRider(response.data);
        setMode('view');
        fetchRiderRides(rider.id);
      }
    } catch (err) {
      const errorMessage = err.response?.data?.message || err.message || 'Failed to update rider';
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

  return (
    <div className="rider-details">
      <h2>Rider Details</h2>

      <div className="mode-tabs">
        <button
          className={mode === 'view' ? 'active' : ''}
          onClick={() => setMode('view')}
        >
          View Rider
        </button>
        <button
          className={mode === 'create' ? 'active' : ''}
          onClick={() => setMode('create')}
        >
          Create Rider
        </button>
        {rider && (
          <button
            className={mode === 'edit' ? 'active' : ''}
            onClick={() => setMode('edit')}
          >
            Edit Rider
          </button>
        )}
      </div>

      {mode === 'view' && (
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
            <Button
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
            >
              🔄 Refresh
            </Button>
          </div>

          {loading && <p>Loading rider details...</p>}
          {error && <div className="error-message">{error}</div>}
          {success && <div className="success-message">{success}</div>}

          {rider && (
            <div className="rider-info">
              <div className="info-row">
                <span className="label">ID:</span>
                <span className="value">{rider.id}</span>
              </div>
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
      )}

      {(mode === 'create' || mode === 'edit') && (
        <div className="rider-form-card">
          <h3>{mode === 'create' ? 'Create New Rider' : 'Edit Rider'}</h3>
          {error && <div className="error-message">{error}</div>}
          {success && <div className="success-message">{success}</div>}

          <div className="form-section">
            <Input
              label="Name *"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              placeholder="Enter rider name"
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
              placeholder="rider@example.com"
            />
            <Input
              label="Region *"
              value={formData.region}
              onChange={(e) => setFormData({ ...formData, region: e.target.value })}
              placeholder="Delhi-NCR"
            />
            <Input
              label="Rating (default: 5.0)"
              type="number"
              step="0.1"
              min="0"
              max="5"
              value={formData.rating || ''}
              onChange={(e) => {
                const value = e.target.value;
                setFormData({ 
                  ...formData, 
                  rating: value === '' ? undefined : parseFloat(value) || 5.0 
                });
              }}
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
              {loading ? 'Processing...' : mode === 'create' ? 'Create Rider' : 'Update Rider'}
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
                    {ride.driverId && <p><strong>Driver ID:</strong> {ride.driverId}</p>}
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

export default RiderDetails;
