package com.gocomet.ridehailing.controller;

import com.gocomet.ridehailing.model.dto.ApiResponse;
import com.gocomet.ridehailing.model.entity.Rider;
import com.gocomet.ridehailing.service.RideService;
import com.newrelic.api.agent.Trace;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/riders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Riders", description = "Rider-related APIs")
public class RiderController {
    
    private final RideService rideService;
    
    @GetMapping("/{id}")
    @Trace(dispatcher = true)
    @Operation(summary = "Get rider details", description = "Retrieves rider information and statistics")
    public ResponseEntity<ApiResponse<Rider>> getRiderById(@PathVariable Long id) {
        log.debug("Getting rider: {}", id);
        
        Rider rider = rideService.getRiderById(id);
        
        return ResponseEntity.ok(ApiResponse.success(rider));
    }
}
