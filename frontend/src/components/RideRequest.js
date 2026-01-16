import React, { useState } from 'react';
import './RideRequest.css';
import { createRide } from '../services/api';

function RideRequest() {
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  const [formData, setFormData] = useState({
    riderId: 1,
    pickupLatitude: 28.6139,
    pickupLongitude: 77.2090,
    pickupAddress: 'Connaught Place, New Delhi',
    destinationLatitude: 28.5355,
    destinationLongitude: 77.3910,
    destinationAddress: 'Noida Sector 18',
    vehicleTier: 'ECONOMY',
    paymentMethod: 'CREDIT_CARD',
    region: 'Delhi-NCR',
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const response = await createRide(formData);
      setResult(response.data);
      console.log('Ride created:', response);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create ride');
      console.error('Error creating ride:', err);
    } finally {
      setLoading(false);
    }
  };

  const presetLocations = [
    {
      name: 'Delhi to Noida',
      pickup: { lat: 28.6139, lng: 77.2090, address: 'Connaught Place, New Delhi' },
      destination: { lat: 28.5355, lng: 77.3910, address: 'Noida Sector 18' },
    },
    {
      name: 'Mumbai Central to BKC',
      pickup: { lat: 19.0176, lng: 72.8562, address: 'Mumbai Central Station' },
      destination: { lat: 19.0653, lng: 72.8683, address: 'Bandra Kurla Complex' },
    },
    {
      name: 'Bangalore Airport to MG Road',
      pickup: { lat: 13.1986, lng: 77.7066, address: 'Kempegowda International Airport' },
      destination: { lat: 12.9716, lng: 77.5946, address: 'MG Road, Bangalore' },
    },
  ];

  const loadPreset = (preset) => {
    setFormData(prev => ({
      ...prev,
      pickupLatitude: preset.pickup.lat,
      pickupLongitude: preset.pickup.lng,
      pickupAddress: preset.pickup.address,
      destinationLatitude: preset.destination.lat,
      destinationLongitude: preset.destination.lng,
      destinationAddress: preset.destination.address,
    }));
  };

  return (
    <div className="ride-request">
      <h2>Request a Ride</h2>

      <div className="presets">
        <h3>Quick Locations</h3>
        <div className="preset-buttons">
          {presetLocations.map((preset, idx) => (
            <button
              key={idx}
              type="button"
              className="preset-btn"
              onClick={() => loadPreset(preset)}
            >
              📍 {preset.name}
            </button>
          ))}
        </div>
      </div>

      <form onSubmit={handleSubmit} className="ride-form">
        <div className="form-section">
          <h3>Rider Information</h3>
          <div className="form-group">
            <label>Rider ID</label>
            <input
              type="number"
              name="riderId"
              value={formData.riderId}
              onChange={handleChange}
              required
            />
          </div>
        </div>

        <div className="form-section">
          <h3>Pickup Location</h3>
          <div className="form-row">
            <div className="form-group">
              <label>Latitude</label>
              <input
                type="number"
                name="pickupLatitude"
                step="any"
                value={formData.pickupLatitude}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-group">
              <label>Longitude</label>
              <input
                type="number"
                name="pickupLongitude"
                step="any"
                value={formData.pickupLongitude}
                onChange={handleChange}
                required
              />
            </div>
          </div>
          <div className="form-group">
            <label>Address</label>
            <input
              type="text"
              name="pickupAddress"
              value={formData.pickupAddress}
              onChange={handleChange}
              required
            />
          </div>
        </div>

        <div className="form-section">
          <h3>Destination</h3>
          <div className="form-row">
            <div className="form-group">
              <label>Latitude</label>
              <input
                type="number"
                name="destinationLatitude"
                step="any"
                value={formData.destinationLatitude}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-group">
              <label>Longitude</label>
              <input
                type="number"
                name="destinationLongitude"
                step="any"
                value={formData.destinationLongitude}
                onChange={handleChange}
                required
              />
            </div>
          </div>
          <div className="form-group">
            <label>Address</label>
            <input
              type="text"
              name="destinationAddress"
              value={formData.destinationAddress}
              onChange={handleChange}
              required
            />
          </div>
        </div>

        <div className="form-section">
          <h3>Ride Details</h3>
          <div className="form-row">
            <div className="form-group">
              <label>Vehicle Tier</label>
              <select
                name="vehicleTier"
                value={formData.vehicleTier}
                onChange={handleChange}
                required
              >
                <option value="ECONOMY">Economy</option>
                <option value="PREMIUM">Premium</option>
                <option value="LUXURY">Luxury</option>
                <option value="SUV">SUV</option>
              </select>
            </div>
            <div className="form-group">
              <label>Payment Method</label>
              <select
                name="paymentMethod"
                value={formData.paymentMethod}
                onChange={handleChange}
                required
              >
                <option value="CASH">Cash</option>
                <option value="CREDIT_CARD">Credit Card</option>
                <option value="DEBIT_CARD">Debit Card</option>
                <option value="WALLET">Wallet</option>
                <option value="UPI">UPI</option>
              </select>
            </div>
          </div>
          <div className="form-group">
            <label>Region</label>
            <input
              type="text"
              name="region"
              value={formData.region}
              onChange={handleChange}
              required
            />
          </div>
        </div>

        <button type="submit" className="submit-btn" disabled={loading}>
          {loading ? '🔄 Creating Ride...' : '🚗 Request Ride'}
        </button>
      </form>

      {error && (
        <div className="alert alert-error">
          ❌ {error}
        </div>
      )}

      {result && (
        <div className="alert alert-success">
          <h3>✅ Ride Created Successfully!</h3>
          <div className="result-details">
            <p><strong>Ride ID:</strong> {result.id}</p>
            <p><strong>Status:</strong> {result.status}</p>
            <p><strong>Estimated Fare:</strong> ₹{result.estimatedFare?.toFixed(2)}</p>
            {result.surgeMultiplier > 1 && (
              <p><strong>Surge Multiplier:</strong> {result.surgeMultiplier}x</p>
            )}
            <p className="tip">💡 Check the Dashboard tab for real-time updates!</p>
          </div>
        </div>
      )}
    </div>
  );
}

export default RideRequest;
