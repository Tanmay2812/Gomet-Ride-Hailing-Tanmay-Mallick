package com.gocomet.ridehailing.controller;

import com.gocomet.ridehailing.model.dto.ApiResponse;
import com.gocomet.ridehailing.model.dto.CreateRiderRequest;
import com.gocomet.ridehailing.model.dto.RiderDTO;
import com.gocomet.ridehailing.model.dto.UpdateRiderRequest;
import com.gocomet.ridehailing.service.RiderService;
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
@RequestMapping("/v1/riders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Riders", description = "Rider-related APIs")
public class RiderController {
    
    private final RiderService riderService;
    
    @GetMapping("/{id}")
    @Trace(dispatcher = true)
    @Operation(summary = "Get rider details", description = "Retrieves rider information and statistics")
    public ResponseEntity<ApiResponse<RiderDTO>> getRiderById(@PathVariable Long id) {
        log.debug("Getting rider: {}", id);
        RiderDTO rider = riderService.getRiderById(id);
        return ResponseEntity.ok(ApiResponse.success(rider));
    }
    
    @PostMapping
    @Trace(dispatcher = true)
    @Operation(summary = "Create rider", description = "Creates a new rider")
    public ResponseEntity<ApiResponse<RiderDTO>> createRider(@Valid @RequestBody CreateRiderRequest request) {
        log.info("Creating rider: {}", request.getName());
        RiderDTO rider = riderService.createRider(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Rider created successfully", rider));
    }
    
    @PutMapping("/{id}")
    @Trace(dispatcher = true)
    @Operation(summary = "Update rider", description = "Updates rider information")
    public ResponseEntity<ApiResponse<RiderDTO>> updateRider(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRiderRequest request) {
        log.info("Updating rider: {}", id);
        RiderDTO rider = riderService.updateRider(id, request);
        return ResponseEntity.ok(ApiResponse.success("Rider updated successfully", rider));
    }
}
