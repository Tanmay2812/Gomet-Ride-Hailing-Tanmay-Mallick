package com.gocomet.ridehailing.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndTripRequest {
    
    @NotNull(message = "Trip ID is required")
    private Long tripId;
    
    @NotNull(message = "End latitude is required")
    private Double endLatitude;
    
    @NotNull(message = "End longitude is required")
    private Double endLongitude;
    
    @NotNull(message = "Distance is required")
    private Double distanceKm;
}
