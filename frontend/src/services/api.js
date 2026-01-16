import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor
api.interceptors.request.use(
  (config) => {
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor
api.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error) => {
    const message = error.response?.data?.message || error.message || 'An error occurred';
    console.error('API Error:', message);
    return Promise.reject(error);
  }
);

// Rides API
export const createRide = (rideData) => api.post('/v1/rides', rideData);
export const getRideById = (rideId) => api.get(`/v1/rides/${rideId}`);
export const getAllRides = (status, limit = 100) => {
  const params = new URLSearchParams();
  if (status) params.append('status', status);
  params.append('limit', limit.toString());
  return api.get(`/v1/rides?${params.toString()}`);
};
export const cancelRide = (rideId, reason) => api.post(`/v1/rides/${rideId}/cancel?reason=${reason}`);

// Driver API
export const updateDriverLocation = (driverId, location) => 
  api.post(`/v1/drivers/${driverId}/location`, location);
export const acceptRide = (driverId, acceptData) => 
  api.post(`/v1/drivers/${driverId}/accept`, acceptData);
export const getPendingRides = (driverId) =>
  api.get(`/v1/drivers/${driverId}/pending-rides`);

// Trip API
export const startTrip = (rideId) => api.post(`/v1/trips/start?rideId=${rideId}`);
export const pauseTrip = (tripId) => api.post(`/v1/trips/${tripId}/pause`);
export const resumeTrip = (tripId) => api.post(`/v1/trips/${tripId}/resume`);
export const endTrip = (tripId, endData) => api.post(`/v1/trips/${tripId}/end`, endData);
export const getTripById = (tripId) => api.get(`/v1/trips/${tripId}`);
export const getTripByRideId = (rideId) => api.get(`/v1/trips?rideId=${rideId}`);

// Payment API
export const processPayment = (paymentData) => api.post('/v1/payments', paymentData);
export const retryPayment = (paymentId) => api.post(`/v1/payments/${paymentId}/retry`);
export const getPaymentByRideId = (rideId) => api.get(`/v1/payments/ride/${rideId}`);

export default api;
