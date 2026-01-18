package com.gocomet.ridehailing.model.entity;

import com.gocomet.ridehailing.model.enums.PaymentMethod;
import com.gocomet.ridehailing.model.enums.RideStatus;
import com.gocomet.ridehailing.model.enums.VehicleTier;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "rides", indexes = {
    @Index(name = "idx_ride_rider", columnList = "riderId"),
    @Index(name = "idx_ride_driver", columnList = "driverId"),
    @Index(name = "idx_ride_status", columnList = "status"),
    @Index(name = "idx_ride_created", columnList = "createdAt"),
    @Index(name = "idx_ride_region", columnList = "region"),
    @Index(name = "idx_ride_idempotency", columnList = "idempotencyKey")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private Long riderId;

    private Long driverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleTier vehicleTier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private Double pickupLatitude;

    @Column(nullable = false)
    private Double pickupLongitude;

    @Column(nullable = false)
    private String pickupAddress;

    @Column(nullable = false)
    private Double destinationLatitude;

    @Column(nullable = false)
    private Double destinationLongitude;

    @Column(nullable = false)
    private String destinationAddress;

    @Column(nullable = false)
    private String region;

    private String tenantId;

    private Double estimatedFare;
    
    private Double surgeMultiplier = 1.0;

    private LocalDateTime matchedAt;
    
    private LocalDateTime acceptedAt;
    
    private LocalDateTime startedAt;
    
    private LocalDateTime endedAt;
    
    private LocalDateTime cancelledAt;
    
    @Column(length = 500)
    private String failureReason;

    @Version
    private Long version; // For optimistic locking

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
