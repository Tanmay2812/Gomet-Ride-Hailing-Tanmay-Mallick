package com.gocomet.ridehailing.controller;

import com.gocomet.ridehailing.model.dto.ApiResponse;
import com.gocomet.ridehailing.model.dto.CreateRideRequest;
import com.gocomet.ridehailing.model.dto.RideResponse;
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

@RestController
@RequestMapping("/v1/rides")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Rides", description = "Ride management APIs")
public class RideController {
    
    private final RideService rideService;
    
    @PostMapping
    @Trace(dispatcher = true)
    @Operation(summary = "Create a new ride request", description = "Creates a new ride request with automatic driver matching")
    public ResponseEntity<ApiResponse<RideResponse>> createRide(@Valid @RequestBody CreateRideRequest request) {
        log.info("Creating ride request for rider: {}", request.getRiderId());
        long startTime = System.currentTimeMillis();
        
        RideResponse response = rideService.createRide(request);
        
        long duration = System.currentTimeMillis() - startTime;
        log.info("Ride created: {} in {}ms", response.getId(), duration);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Ride created successfully", response));
    }
    
    @GetMapping("/{id}")
    @Trace(dispatcher = true)
    @Operation(summary = "Get ride by ID", description = "Retrieves ride details including driver information and status")
    public ResponseEntity<ApiResponse<RideResponse>> getRideById(@PathVariable Long id) {
        log.debug("Getting ride: {}", id);
        long startTime = System.currentTimeMillis();
        
        RideResponse response = rideService.getRideById(id);
        
        long duration = System.currentTimeMillis() - startTime;
        log.debug("Retrieved ride: {} in {}ms", id, duration);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping
    @Trace(dispatcher = true)
    @Operation(summary = "Get all rides", description = "Retrieves all rides, optionally filtered by status")
    public ResponseEntity<ApiResponse<java.util.List<RideResponse>>> getAllRides(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long riderId,
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false, defaultValue = "100") int limit) {
        log.debug("Getting rides, status: {}, riderId: {}, driverId: {}, limit: {}", status, riderId, driverId, limit);
        
        java.util.List<RideResponse> responses;
        if (riderId != null) {
            responses = rideService.getRidesByRiderId(riderId);
        } else if (driverId != null) {
            responses = rideService.getRidesByDriverId(driverId);
        } else {
            responses = rideService.getAllRides(status, limit);
        }
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    @PostMapping("/{id}/cancel")
    @Trace(dispatcher = true)
    @Operation(summary = "Cancel a ride", description = "Cancels an active ride request")
    public ResponseEntity<ApiResponse<RideResponse>> cancelRide(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "User cancelled") String reason) {
        log.info("Cancelling ride: {} for reason: {}", id, reason);
        
        RideResponse response = rideService.cancelRide(id, reason);
        
        return ResponseEntity.ok(ApiResponse.success("Ride cancelled successfully", response));
    }
}
