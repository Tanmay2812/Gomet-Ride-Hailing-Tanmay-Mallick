package com.gocomet.ridehailing.controller;

import com.gocomet.ridehailing.model.dto.ApiResponse;
import com.gocomet.ridehailing.model.dto.EndTripRequest;
import com.gocomet.ridehailing.model.entity.Trip;
import com.gocomet.ridehailing.service.TripService;
import com.newrelic.api.agent.Trace;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/trips")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trips", description = "Trip lifecycle management APIs")
public class TripController {
    
    private final TripService tripService;
    
    @PostMapping("/start")
    @Trace(dispatcher = true)
    @Operation(summary = "Start a trip", description = "Starts a trip after driver arrives at pickup location")
    public ResponseEntity<ApiResponse<Trip>> startTrip(@RequestParam Long rideId) {
        log.info("Starting trip for ride: {}", rideId);
        long startTime = System.currentTimeMillis();
        
        Trip trip = tripService.startTrip(rideId);
        
        long duration = System.currentTimeMillis() - startTime;
        log.info("Trip {} started in {}ms", trip.getId(), duration);
        
        return ResponseEntity.ok(ApiResponse.success("Trip started successfully", trip));
    }
    
    @PostMapping("/{id}/pause")
    @Trace(dispatcher = true)
    @Operation(summary = "Pause a trip", description = "Pauses an ongoing trip")
    public ResponseEntity<ApiResponse<Trip>> pauseTrip(@PathVariable Long id) {
        log.info("Pausing trip: {}", id);
        
        Trip trip = tripService.pauseTrip(id);
        
        return ResponseEntity.ok(ApiResponse.success("Trip paused successfully", trip));
    }
    
    @PostMapping("/{id}/resume")
    @Trace(dispatcher = true)
    @Operation(summary = "Resume a trip", description = "Resumes a paused trip")
    public ResponseEntity<ApiResponse<Trip>> resumeTrip(@PathVariable Long id) {
        log.info("Resuming trip: {}", id);
        
        Trip trip = tripService.resumeTrip(id);
        
        return ResponseEntity.ok(ApiResponse.success("Trip resumed successfully", trip));
    }
    
    @PostMapping("/{id}/end")
    @Trace(dispatcher = true)
    @Operation(summary = "End a trip", description = "Ends a trip and calculates final fare")
    public ResponseEntity<ApiResponse<Trip>> endTrip(
            @PathVariable Long id,
            @Valid @RequestBody EndTripRequest request) {
        
        log.info("Ending trip: {}", id);
        long startTime = System.currentTimeMillis();
        
        request.setTripId(id);
        Trip trip = tripService.endTrip(request);
        
        long duration = System.currentTimeMillis() - startTime;
        log.info("Trip {} ended in {}ms. Fare: ₹{}", id, duration, trip.getTotalFare());
        
        return ResponseEntity.ok(ApiResponse.success("Trip ended successfully", trip));
    }
    
    @GetMapping("/{id}")
    @Trace(dispatcher = true)
    @Operation(summary = "Get trip by ID", description = "Retrieves trip details including fare information")
    public ResponseEntity<ApiResponse<Trip>> getTripById(@PathVariable Long id) {
        Trip trip = tripService.getTripById(id);
        return ResponseEntity.ok(ApiResponse.success(trip));
    }
}
