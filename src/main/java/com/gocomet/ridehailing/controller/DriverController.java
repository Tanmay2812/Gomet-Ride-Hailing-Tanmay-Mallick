package com.gocomet.ridehailing.controller;

import com.gocomet.ridehailing.model.dto.AcceptRideRequest;
import com.gocomet.ridehailing.model.dto.ApiResponse;
import com.gocomet.ridehailing.model.dto.DriverLocationUpdate;
import com.gocomet.ridehailing.model.dto.RideResponse;
import com.gocomet.ridehailing.model.entity.Ride;
import com.gocomet.ridehailing.model.enums.RideStatus;
import com.gocomet.ridehailing.repository.RideRepository;
import com.gocomet.ridehailing.service.LocationCacheService;
import com.gocomet.ridehailing.service.RideService;
import com.newrelic.api.agent.Trace;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/drivers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Drivers", description = "Driver-related APIs")
public class DriverController {
    
    private final RideService rideService;
    private final com.gocomet.ridehailing.service.DriverService driverService;
    private final LocationCacheService locationCacheService;
    private final RideRepository rideRepository;
    
    @PostMapping("/{id}/location")
    @Trace(dispatcher = true)
    @Operation(summary = "Update driver location", description = "Updates driver's real-time location (should be called every 1-2 seconds)")
    public ResponseEntity<ApiResponse<String>> updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody DriverLocationUpdate locationUpdate) {
        
        long startTime = System.currentTimeMillis();
        
        locationCacheService.updateDriverLocation(
            id,
            locationUpdate.getLatitude(),
            locationUpdate.getLongitude()
        );
        
        long duration = System.currentTimeMillis() - startTime;
        
        if (duration > 100) {
            log.warn("Location update took {}ms for driver {}", duration, id);
        }
        
        return ResponseEntity.ok(ApiResponse.success("Location updated successfully"));
    }
    
    @PostMapping("/{id}/accept")
    @Trace(dispatcher = true)
    @Operation(summary = "Accept ride assignment", description = "Driver accepts a ride that was assigned to them")
    public ResponseEntity<ApiResponse<RideResponse>> acceptRide(
            @PathVariable Long id,
            @Valid @RequestBody AcceptRideRequest request) {
        
        log.info("Driver {} accepting ride {}", id, request.getRideId());
        long startTime = System.currentTimeMillis();
        
        RideResponse response = rideService.acceptRide(request.getRideId(), id);
        
        long duration = System.currentTimeMillis() - startTime;
        log.info("Ride accepted by driver {} in {}ms", id, duration);
        
        return ResponseEntity.ok(ApiResponse.success("Ride accepted successfully", response));
    }
    
    @GetMapping("/{id}")
    @Trace(dispatcher = true)
    @Operation(summary = "Get driver details", description = "Retrieves driver information and statistics")
    public ResponseEntity<ApiResponse<com.gocomet.ridehailing.model.dto.DriverDTO>> getDriverById(@PathVariable Long id) {
        log.debug("Getting driver: {}", id);
        
        com.gocomet.ridehailing.model.dto.DriverDTO driver = driverService.getDriverById(id);
        
        return ResponseEntity.ok(ApiResponse.success(driver));
    }
    
    @PostMapping
    @Trace(dispatcher = true)
    @Operation(summary = "Create driver", description = "Creates a new driver")
    public ResponseEntity<ApiResponse<com.gocomet.ridehailing.model.dto.DriverDTO>> createDriver(
            @Valid @RequestBody com.gocomet.ridehailing.model.dto.CreateDriverRequest request) {
        log.info("Creating driver: {}", request.getName());
        com.gocomet.ridehailing.model.dto.DriverDTO driver = driverService.createDriver(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Driver created successfully", driver));
    }
    
    @PutMapping("/{id}")
    @Trace(dispatcher = true)
    @Operation(summary = "Update driver", description = "Updates driver information")
    public ResponseEntity<ApiResponse<com.gocomet.ridehailing.model.dto.DriverDTO>> updateDriver(
            @PathVariable Long id,
            @Valid @RequestBody com.gocomet.ridehailing.model.dto.UpdateDriverRequest request) {
        log.info("Updating driver: {}", id);
        com.gocomet.ridehailing.model.dto.DriverDTO driver = driverService.updateDriver(id, request);
        return ResponseEntity.ok(ApiResponse.success("Driver updated successfully", driver));
    }
    
    @GetMapping("/{id}/pending-rides")
    @Trace(dispatcher = true)
    @Operation(summary = "Get pending rides for driver", description = "Gets all rides matched to this driver that are pending acceptance")
    public ResponseEntity<ApiResponse<List<RideResponse>>> getPendingRides(@PathVariable Long id) {
        log.debug("Getting pending rides for driver {}", id);
        
        // Find all rides matched to this driver that haven't been accepted yet
        List<Ride> pendingRides = rideRepository.findByDriverIdOrderByCreatedAtDesc(id)
            .stream()
            .filter(ride -> ride.getStatus() == RideStatus.MATCHED)
            .collect(Collectors.toList());
        
        List<RideResponse> responses = pendingRides.stream()
            .map(rideService::mapRideToResponse)
            .collect(Collectors.toList());
        
        log.info("Found {} pending rides for driver {}", responses.size(), id);
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
