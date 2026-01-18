package com.gocomet.ridehailing.model.dto;

import com.gocomet.ridehailing.model.enums.DriverStatus;
import com.gocomet.ridehailing.model.enums.VehicleTier;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDriverRequest {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "License number is required")
    private String licenseNumber;
    
    @NotBlank(message = "Vehicle number is required")
    private String vehicleNumber;
    
    @NotNull(message = "Vehicle tier is required")
    private VehicleTier vehicleTier;
    
    @NotNull(message = "Status is required")
    private DriverStatus status;
    
    @NotBlank(message = "Region is required")
    private String region;
    
    private String tenantId;
    
    private Double rating = 5.0;
}
