package com.gocomet.ridehailing.controller;

import com.gocomet.ridehailing.model.dto.AcceptRideRequest;
import com.gocomet.ridehailing.model.dto.ApiResponse;
import com.gocomet.ridehailing.model.dto.DriverLocationUpdate;
import com.gocomet.ridehailing.model.dto.RideResponse;
import com.gocomet.ridehailing.service.LocationCacheService;
import com.gocomet.ridehailing.service.RideService;
import com.newrelic.api.agent.Trace;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/drivers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Drivers", description = "Driver-related APIs")
public class DriverController {
    
    private final RideService rideService;
    private final LocationCacheService locationCacheService;
    
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
}
