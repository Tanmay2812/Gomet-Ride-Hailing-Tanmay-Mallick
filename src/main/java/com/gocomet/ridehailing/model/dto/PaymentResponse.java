package com.gocomet.ridehailing.model.dto;

import com.gocomet.ridehailing.model.enums.PaymentMethod;
import com.gocomet.ridehailing.model.enums.PaymentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Long id;
    private Long rideId;
    private Long tripId;
    private Double amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String transactionId;
    private String failureReason;
    private LocalDateTime createdAt;
}
