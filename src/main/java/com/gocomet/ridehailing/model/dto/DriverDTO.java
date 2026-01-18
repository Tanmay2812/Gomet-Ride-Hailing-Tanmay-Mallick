package com.gocomet.ridehailing.model.dto;

import com.gocomet.ridehailing.model.enums.DriverStatus;
import com.gocomet.ridehailing.model.enums.VehicleTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverDTO {
    private Long id;
    private String phoneNumber;
    private String name;
    private String email;
    private String licenseNumber;
    private String vehicleNumber;
    private VehicleTier vehicleTier;
    private DriverStatus status;
    private String region;
    private String tenantId;
    private Double rating;
    private Integer totalRides;
    private LocalDateTime lastLocationUpdate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
