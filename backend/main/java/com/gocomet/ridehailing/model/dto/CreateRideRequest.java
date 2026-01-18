package com.gocomet.ridehailing.model.dto;

import com.gocomet.ridehailing.model.enums.PaymentMethod;
import com.gocomet.ridehailing.model.enums.VehicleTier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRideRequest {
    
    @NotNull(message = "Rider ID is required")
    private Long riderId;
    
    @NotNull(message = "Pickup latitude is required")
    private Double pickupLatitude;
    
    @NotNull(message = "Pickup longitude is required")
    private Double pickupLongitude;
    
    @NotBlank(message = "Pickup address is required")
    private String pickupAddress;
    
    @NotNull(message = "Destination latitude is required")
    private Double destinationLatitude;
    
    @NotNull(message = "Destination longitude is required")
    private Double destinationLongitude;
    
    @NotBlank(message = "Destination address is required")
    private String destinationAddress;
    
    @NotNull(message = "Vehicle tier is required")
    private VehicleTier vehicleTier;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    @NotBlank(message = "Region is required")
    private String region;
    
    private String tenantId;
    
    private String idempotencyKey;
}
