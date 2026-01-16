package com.gocomet.ridehailing.model.dto;

import com.gocomet.ridehailing.model.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {
    
    @NotNull(message = "Ride ID is required")
    private Long rideId;
    
    @NotNull(message = "Trip ID is required")
    private Long tripId;
    
    @NotNull(message = "Amount is required")
    private Double amount;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    private String idempotencyKey;
}
