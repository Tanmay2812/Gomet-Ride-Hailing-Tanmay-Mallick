package com.gocomet.ridehailing.model.dto;

import com.gocomet.ridehailing.model.enums.DriverStatus;
import com.gocomet.ridehailing.model.enums.VehicleTier;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDriverRequest {
    
    private String name;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String licenseNumber;
    
    private String vehicleNumber;
    
    private VehicleTier vehicleTier;
    
    private DriverStatus status;
    
    private String region;
    
    private String tenantId;
    
    private Double rating;
}
