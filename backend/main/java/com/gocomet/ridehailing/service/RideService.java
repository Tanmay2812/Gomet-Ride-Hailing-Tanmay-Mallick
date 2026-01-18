package com.gocomet.ridehailing.service;

import com.gocomet.ridehailing.exception.RideException;
import com.gocomet.ridehailing.model.dto.CreateRideRequest;
import com.gocomet.ridehailing.model.dto.Location;
import com.gocomet.ridehailing.model.dto.RideResponse;
import com.gocomet.ridehailing.model.entity.Driver;
import com.gocomet.ridehailing.model.entity.Ride;
import com.gocomet.ridehailing.model.entity.Rider;
import com.gocomet.ridehailing.model.enums.DriverStatus;
import com.gocomet.ridehailing.model.enums.RideStatus;
import com.gocomet.ridehailing.repository.DriverRepository;
import com.gocomet.ridehailing.repository.RideRepository;
import com.gocomet.ridehailing.repository.RiderRepository;
import com.newrelic.api.agent.Trace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.gocomet.ridehailing.model.enums.VehicleTier;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideService {
    
    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;
    private final RiderRepository riderRepository;
    private final DriverMatchingService driverMatchingService;
    private final SurgePricingService surgePricingService;
    private final FareCalculationService fareCalculationService;
    private final NotificationService notificationService;
    private final LocationCacheService locationCacheService;
    
    @org.springframework.beans.factory.annotation.Value("${app.matching.search-radius-km:5.0}")
    private Double searchRadiusKm;
    
    @Trace
    @Transactional
    public RideResponse createRide(CreateRideRequest request) {
        try {
            // Generate or use provided idempotency key
            String idempotencyKey = request.getIdempotencyKey();
            if (idempotencyKey == null || idempotencyKey.isEmpty()) {
                idempotencyKey = generateIdempotencyKey(request);
            }
            
            // Check for duplicate request
            Optional<Ride> existingRide = rideRepository.findByIdempotencyKey(idempotencyKey);
            if (existingRide.isPresent()) {
                log.info("Duplicate ride request detected: {}", idempotencyKey);
                return mapToResponse(existingRide.get());
            }
            
            // Calculate surge multiplier
            Double surgeMultiplier = surgePricingService.calculateSurgeMultiplier(request.getRegion());
            
            // Calculate estimated fare
            Double estimatedFare = fareCalculationService.calculateEstimatedFare(
                request.getPickupLatitude(),
                request.getPickupLongitude(),
                request.getDestinationLatitude(),
                request.getDestinationLongitude(),
                surgeMultiplier
            );
            
            // Create ride
            Ride ride = Ride.builder()
                .idempotencyKey(idempotencyKey)
                .riderId(request.getRiderId())
                .status(RideStatus.REQUESTED)
                .vehicleTier(request.getVehicleTier())
                .paymentMethod(request.getPaymentMethod())
                .pickupLatitude(request.getPickupLatitude())
                .pickupLongitude(request.getPickupLongitude())
                .pickupAddress(request.getPickupAddress())
                .destinationLatitude(request.getDestinationLatitude())
                .destinationLongitude(request.getDestinationLongitude())
                .destinationAddress(request.getDestinationAddress())
                .region(request.getRegion())
                .tenantId(request.getTenantId())
                .estimatedFare(estimatedFare)
                .surgeMultiplier(surgeMultiplier)
                .build();
            
            ride = rideRepository.save(ride);
            log.info("Ride created: {} for rider: {}", ride.getId(), ride.getRiderId());
            
            // Start async driver matching
            Long rideId = ride.getId();
            matchDriverAsync(rideId, request);
            
            RideResponse response = mapToResponse(ride);
            notificationService.broadcastRideUpdate(response);
            
            return response;
        } catch (Exception e) {
            log.error("Error creating ride", e);
            throw new RideException("Failed to create ride: " + e.getMessage());
        }
    }
    
    @Async("taskExecutor")
    @Trace
    public CompletableFuture<Void> matchDriverAsync(Long rideId, CreateRideRequest request) {
        try {
            log.info("Starting driver matching for ride {}", rideId);
            
            Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideException("Ride not found"));
            
            // Update status to searching
            ride.setStatus(RideStatus.SEARCHING);
            rideRepository.save(ride);
            notificationService.broadcastRideUpdate(mapToResponse(ride));
            
            // Find best driver
            Optional<Driver> bestDriverOpt = driverMatchingService.findBestDriver(
                request.getPickupLatitude(),
                request.getPickupLongitude(),
                request.getVehicleTier(),
                request.getRegion()
            );
            
            if (bestDriverOpt.isEmpty()) {
                log.warn("No driver found for ride {}", rideId);
                
                // Determine specific failure reason
                String failureReason = determineFailureReason(
                    request.getPickupLatitude(),
                    request.getPickupLongitude(),
                    request.getVehicleTier(),
                    request.getRegion()
                );
                
                ride.setStatus(RideStatus.FAILED);
                ride.setFailureReason(failureReason);
                rideRepository.save(ride);
                notificationService.notifyRider(ride.getRiderId(), "RIDE_FAILED", 
                    failureReason);
                notificationService.broadcastRideUpdate(mapToResponse(ride));
                return CompletableFuture.completedFuture(null);
            }
            
            Driver driver = bestDriverOpt.get();
            
            // Assign driver
            assignDriver(rideId, driver.getId());
            
            log.info("Driver {} matched with ride {}", driver.getId(), rideId);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Error in async driver matching", e);
            try {
                Ride ride = rideRepository.findById(rideId).orElse(null);
                if (ride != null) {
                    ride.setStatus(RideStatus.FAILED);
                    ride.setFailureReason("Driver matching error: " + e.getMessage());
                    rideRepository.save(ride);
                    notificationService.broadcastRideUpdate(mapToResponse(ride));
                }
            } catch (Exception ex) {
                log.error("Error updating ride status after matching failure", ex);
            }
            return CompletableFuture.failedFuture(e);
        }
    }
    
    private String determineFailureReason(Double pickupLat, Double pickupLon, 
                                         VehicleTier vehicleTier, String region) {
        // Check if any drivers exist in database for this tier and region
        List<Driver> availableDrivers = driverRepository
            .findAvailableDriversByTierAndRegion(DriverStatus.AVAILABLE, vehicleTier, region);
        
        if (availableDrivers.isEmpty()) {
            // Check if any drivers exist at all for this tier/region
            List<Driver> allDrivers = driverRepository.findAll().stream()
                .filter(d -> d.getVehicleTier() == vehicleTier && d.getRegion().equals(region))
                .collect(java.util.stream.Collectors.toList());
            
            if (allDrivers.isEmpty()) {
                return String.format("No %s drivers found in %s region. Please try a different vehicle tier or region.", 
                    vehicleTier, region);
            } else {
                // Drivers exist but none are available
                long availableCount = allDrivers.stream()
                    .filter(d -> d.getStatus() == DriverStatus.AVAILABLE)
                    .count();
                if (availableCount == 0) {
                    return String.format("No available %s drivers in %s. All drivers are currently busy or offline.", 
                        vehicleTier, region);
                }
            }
        }
        
        // Check if drivers have locations in Redis
        List<Long> nearbyDriverIds = locationCacheService
            .findNearbyDrivers(pickupLat, pickupLon, searchRadiusKm);
        
        if (nearbyDriverIds.isEmpty()) {
            return String.format("No drivers found within %.1f km of pickup location. Drivers may need to update their location.", 
                searchRadiusKm);
        }
        
        // Check if available drivers are nearby
        List<Long> candidateIds = availableDrivers.stream()
            .map(Driver::getId)
            .filter(nearbyDriverIds::contains)
            .collect(java.util.stream.Collectors.toList());
        
        if (candidateIds.isEmpty()) {
            return String.format("Available %s drivers in %s are not within %.1f km of pickup location.", 
                vehicleTier, region, searchRadiusKm);
        }
        
        return "No suitable driver found. Please try again later.";
    }
    
    @Trace
    @Transactional
    public void assignDriver(Long rideId, Long driverId) {
        // Get ride with lock
        Ride ride = rideRepository.findByIdWithLock(rideId)
            .orElseThrow(() -> new RideException("Ride not found"));
        
        if (ride.getStatus() != RideStatus.SEARCHING) {
            throw new RideException("Ride is not in searching state");
        }
        
        // Update driver status
        driverRepository.updateDriverStatus(driverId, DriverStatus.BUSY);
        
        // Update ride
        ride.setDriverId(driverId);
        ride.setStatus(RideStatus.MATCHED);
        ride.setMatchedAt(LocalDateTime.now());
        rideRepository.save(ride);
        
        // Send notifications
        notificationService.sendRideMatchedNotification(ride, driverId);
        notificationService.broadcastRideUpdate(mapToResponse(ride));
    }
    
    @Trace
    @Transactional
    public RideResponse acceptRide(Long rideId, Long driverId) {
        try {
            // Get ride with lock
            Ride ride = rideRepository.findByIdWithLock(rideId)
                .orElseThrow(() -> new RideException("Ride not found"));
            
            // Validate
            if (ride.getStatus() != RideStatus.MATCHED) {
                throw new RideException("Ride is not in matched state");
            }
            
            if (!ride.getDriverId().equals(driverId)) {
                throw new RideException("This ride is not assigned to you");
            }
            
            // Update ride status
            ride.setStatus(RideStatus.ACCEPTED);
            ride.setAcceptedAt(LocalDateTime.now());
            ride = rideRepository.save(ride);
            
            // Update driver status
            driverRepository.updateDriverStatus(driverId, DriverStatus.ON_RIDE);
            
            // Send notifications
            notificationService.sendRideAcceptedNotification(ride);
            
            RideResponse response = mapToResponse(ride);
            notificationService.broadcastRideUpdate(response);
            
            log.info("Ride {} accepted by driver {}", rideId, driverId);
            return response;
        } catch (Exception e) {
            log.error("Error accepting ride", e);
            throw new RideException("Failed to accept ride: " + e.getMessage());
        }
    }
    
    @Trace
    public List<RideResponse> getAllRides(String status, int limit) {
        try {
            List<Ride> rides;
            if (status != null && !status.isEmpty()) {
                try {
                    RideStatus rideStatus = RideStatus.valueOf(status.toUpperCase());
                    // Filter by status - get all rides and filter by status
                    rides = rideRepository.findAll().stream()
                        .filter(r -> r.getStatus() == rideStatus)
                        .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid ride status: {}", status);
                    rides = rideRepository.findAll();
                }
            } else {
                rides = rideRepository.findAll();
            }
            
            // Sort by created date descending and limit
            rides = rides.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(limit)
                .collect(Collectors.toList());
            
            return rides.stream()
                .map(this::mapRideToResponse)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting all rides", e);
            throw new RideException("Failed to retrieve rides: " + e.getMessage());
        }
    }
    
    @Trace
    public List<RideResponse> getRidesByRiderId(Long riderId) {
        try {
            List<Ride> rides = rideRepository.findByRiderIdOrderByCreatedAtDesc(riderId);
            return rides.stream()
                .map(this::mapRideToResponse)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting rides for rider: {}", riderId, e);
            throw new RideException("Failed to retrieve rides: " + e.getMessage());
        }
    }
    
    @Trace
    public List<RideResponse> getRidesByDriverId(Long driverId) {
        try {
            List<Ride> rides = rideRepository.findByDriverIdOrderByCreatedAtDesc(driverId);
            return rides.stream()
                .map(this::mapRideToResponse)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting rides for driver: {}", driverId, e);
            throw new RideException("Failed to retrieve rides: " + e.getMessage());
        }
    }
    
    @Trace
    public Driver getDriverById(Long driverId) {
        return driverRepository.findById(driverId)
            .orElseThrow(() -> new RideException("Driver not found"));
    }
    
    @Trace
    public Rider getRiderById(Long riderId) {
        return riderRepository.findById(riderId)
            .orElseThrow(() -> new RideException("Rider not found"));
    }
    
    @Trace
    public RideResponse getRideById(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
            .orElseThrow(() -> new RideException("Ride not found"));
        return mapToResponse(ride);
    }
    
    @Trace
    @Transactional
    public RideResponse cancelRide(Long rideId, String reason) {
        Ride ride = rideRepository.findByIdWithLock(rideId)
            .orElseThrow(() -> new RideException("Ride not found"));
        
        if (ride.getStatus() == RideStatus.COMPLETED || ride.getStatus() == RideStatus.CANCELLED) {
            throw new RideException("Cannot cancel ride in " + ride.getStatus() + " status");
        }
        
        // If driver was assigned, free them up
        if (ride.getDriverId() != null) {
            driverRepository.updateDriverStatus(ride.getDriverId(), DriverStatus.AVAILABLE);
        }
        
        ride.setStatus(RideStatus.CANCELLED);
        ride.setCancelledAt(LocalDateTime.now());
        ride = rideRepository.save(ride);
        
        log.info("Ride {} cancelled. Reason: {}", rideId, reason);
        
        return mapToResponse(ride);
    }
    
    public RideResponse mapRideToResponse(Ride ride) {
        return mapToResponse(ride);
    }
    
    private RideResponse mapToResponse(Ride ride) {
        RideResponse.RideResponseBuilder builder = RideResponse.builder()
            .id(ride.getId())
            .riderId(ride.getRiderId())
            .driverId(ride.getDriverId())
            .status(ride.getStatus())
            .vehicleTier(ride.getVehicleTier())
            .paymentMethod(ride.getPaymentMethod())
            .pickupLatitude(ride.getPickupLatitude())
            .pickupLongitude(ride.getPickupLongitude())
            .pickupAddress(ride.getPickupAddress())
            .destinationLatitude(ride.getDestinationLatitude())
            .destinationLongitude(ride.getDestinationLongitude())
            .destinationAddress(ride.getDestinationAddress())
            .estimatedFare(ride.getEstimatedFare())
            .surgeMultiplier(ride.getSurgeMultiplier())
            .region(ride.getRegion())
            .createdAt(ride.getCreatedAt())
            .matchedAt(ride.getMatchedAt())
            .acceptedAt(ride.getAcceptedAt())
            .startedAt(ride.getStartedAt())
            .endedAt(ride.getEndedAt())
            .failureReason(ride.getFailureReason());
        
        // Add driver info if available
        if (ride.getDriverId() != null) {
            driverRepository.findById(ride.getDriverId()).ifPresent(driver -> {
                RideResponse.DriverInfo driverInfo = RideResponse.DriverInfo.builder()
                    .name(driver.getName())
                    .phoneNumber(driver.getPhoneNumber())
                    .vehicleNumber(driver.getVehicleNumber())
                    .rating(driver.getRating())
                    .currentLocation(locationCacheService.getDriverLocation(driver.getId()).orElse(null))
                    .build();
                builder.driverInfo(driverInfo);
            });
        }
        
        return builder.build();
    }
    
    private String generateIdempotencyKey(CreateRideRequest request) {
        return "ride-" + request.getRiderId() + "-" + UUID.randomUUID().toString();
    }
}
