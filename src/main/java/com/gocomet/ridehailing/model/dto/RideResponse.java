package com.gocomet.ridehailing.model.dto;

import com.gocomet.ridehailing.model.enums.PaymentMethod;
import com.gocomet.ridehailing.model.enums.RideStatus;
import com.gocomet.ridehailing.model.enums.VehicleTier;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideResponse {
    private Long id;
    private Long riderId;
    private Long driverId;
    private RideStatus status;
    private VehicleTier vehicleTier;
    private PaymentMethod paymentMethod;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private String pickupAddress;
    private Double destinationLatitude;
    private Double destinationLongitude;
    private String destinationAddress;
    private Double estimatedFare;
    private Double surgeMultiplier;
    private String region;
    private LocalDateTime createdAt;
    private LocalDateTime matchedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private DriverInfo driverInfo;
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverInfo {
        private String name;
        private String phoneNumber;
        private String vehicleNumber;
        private Double rating;
        private Location currentLocation;
    }
}
