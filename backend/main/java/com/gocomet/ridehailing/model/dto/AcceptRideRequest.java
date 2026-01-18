package com.gocomet.ridehailing.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcceptRideRequest {
    
    @NotNull(message = "Ride ID is required")
    private Long rideId;
    
    @NotNull(message = "Driver ID is required")
    private Long driverId;
    
    private Double currentLatitude;
    private Double currentLongitude;
}
