package com.gocomet.ridehailing.service;

import com.gocomet.ridehailing.exception.TripException;
import com.gocomet.ridehailing.model.dto.EndTripRequest;
import com.gocomet.ridehailing.model.entity.Ride;
import com.gocomet.ridehailing.model.entity.Trip;
import com.gocomet.ridehailing.model.enums.RideStatus;
import com.gocomet.ridehailing.model.enums.TripStatus;
import com.gocomet.ridehailing.repository.RideRepository;
import com.gocomet.ridehailing.repository.TripRepository;
import com.newrelic.api.agent.Trace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripService {
    
    private final TripRepository tripRepository;
    private final RideRepository rideRepository;
    private final FareCalculationService fareCalculationService;
    private final NotificationService notificationService;
    
    @Trace
    @Transactional
    public Trip startTrip(Long rideId) {
        try {
            // Get ride with lock
            Ride ride = rideRepository.findByIdWithLock(rideId)
                .orElseThrow(() -> new TripException("Ride not found"));
            
            // Validate ride status
            if (ride.getStatus() != RideStatus.ACCEPTED && ride.getStatus() != RideStatus.DRIVER_ARRIVED) {
                throw new TripException("Cannot start trip. Ride must be in ACCEPTED or DRIVER_ARRIVED status");
            }
            
            // Check if trip already exists
            if (tripRepository.findByRideId(rideId).isPresent()) {
                throw new TripException("Trip already started for this ride");
            }
            
            // Create trip
            Trip trip = Trip.builder()
                .rideId(rideId)
                .driverId(ride.getDriverId())
                .riderId(ride.getRiderId())
                .status(TripStatus.STARTED)
                .startLatitude(ride.getPickupLatitude())
                .startLongitude(ride.getPickupLongitude())
                .startTime(LocalDateTime.now())
                .surgeMultiplier(ride.getSurgeMultiplier())
                .build();
            
            trip = tripRepository.save(trip);
            
            // Update ride status
            ride.setStatus(RideStatus.IN_PROGRESS);
            ride.setStartedAt(LocalDateTime.now());
            rideRepository.save(ride);
            
            // Send notifications
            notificationService.sendTripStartedNotification(ride);
            
            log.info("Trip started for ride {}", rideId);
            return trip;
        } catch (Exception e) {
            log.error("Error starting trip", e);
            throw new TripException("Failed to start trip: " + e.getMessage());
        }
    }
    
    @Trace
    @Transactional
    public Trip pauseTrip(Long tripId) {
        Trip trip = tripRepository.findByIdWithLock(tripId)
            .orElseThrow(() -> new TripException("Trip not found"));
        
        if (trip.getStatus() != TripStatus.STARTED) {
            throw new TripException("Trip is not in progress");
        }
        
        trip.setStatus(TripStatus.PAUSED);
        trip.setPausedAt(LocalDateTime.now());
        trip = tripRepository.save(trip);
        
        log.info("Trip {} paused", tripId);
        return trip;
    }
    
    @Trace
    @Transactional
    public Trip resumeTrip(Long tripId) {
        Trip trip = tripRepository.findByIdWithLock(tripId)
            .orElseThrow(() -> new TripException("Trip not found"));
        
        if (trip.getStatus() != TripStatus.PAUSED) {
            throw new TripException("Trip is not paused");
        }
        
        // Calculate paused duration
        if (trip.getPausedAt() != null) {
            long pausedSeconds = ChronoUnit.SECONDS.between(trip.getPausedAt(), LocalDateTime.now());
            long currentPaused = trip.getPausedDurationSeconds() != null ? trip.getPausedDurationSeconds() : 0L;
            trip.setPausedDurationSeconds(currentPaused + pausedSeconds);
        }
        
        trip.setStatus(TripStatus.RESUMED);
        trip.setPausedAt(null);
        trip = tripRepository.save(trip);
        
        log.info("Trip {} resumed", tripId);
        return trip;
    }
    
    @Trace
    @Transactional
    public Trip endTrip(EndTripRequest request) {
        try {
            // Get trip with lock
            Trip trip = tripRepository.findByIdWithLock(request.getTripId())
                .orElseThrow(() -> new TripException("Trip not found"));
            
            // Validate trip status
            if (trip.getStatus() != TripStatus.STARTED && trip.getStatus() != TripStatus.RESUMED) {
                throw new TripException("Trip is not in progress");
            }
            
            // Update trip details
            trip.setStatus(TripStatus.ENDED);
            trip.setEndTime(LocalDateTime.now());
            trip.setEndLatitude(request.getEndLatitude());
            trip.setEndLongitude(request.getEndLongitude());
            trip.setDistanceKm(request.getDistanceKm());
            
            // Calculate trip duration (excluding paused time)
            long totalSeconds = ChronoUnit.SECONDS.between(trip.getStartTime(), trip.getEndTime());
            long pausedSeconds = trip.getPausedDurationSeconds() != null ? trip.getPausedDurationSeconds() : 0L;
            long activeSeconds = totalSeconds - pausedSeconds;
            trip.setDurationMinutes(activeSeconds / 60);
            
            // Calculate fare
            Double finalFare = fareCalculationService.calculateFinalFare(
                trip.getDistanceKm(),
                trip.getDurationMinutes(),
                trip.getSurgeMultiplier()
            );
            
            trip.setTotalFare(finalFare);
            trip = tripRepository.save(trip);
            
            // Update ride status
            Ride ride = rideRepository.findById(trip.getRideId())
                .orElseThrow(() -> new TripException("Ride not found"));
            ride.setStatus(RideStatus.COMPLETED);
            ride.setEndedAt(LocalDateTime.now());
            rideRepository.save(ride);
            
            // Send notifications
            notificationService.sendTripEndedNotification(ride, finalFare);
            
            log.info("Trip {} ended. Distance: {}km, Duration: {}min, Fare: ₹{}", 
                trip.getId(), trip.getDistanceKm(), trip.getDurationMinutes(), finalFare);
            
            return trip;
        } catch (Exception e) {
            log.error("Error ending trip", e);
            throw new TripException("Failed to end trip: " + e.getMessage());
        }
    }
    
    @Trace
    public Trip getTripById(Long tripId) {
        return tripRepository.findById(tripId)
            .orElseThrow(() -> new TripException("Trip not found"));
    }
    
    @Trace
    public Trip getTripByRideId(Long rideId) {
        return tripRepository.findByRideId(rideId)
            .orElseThrow(() -> new TripException("Trip not found for ride"));
    }
}
