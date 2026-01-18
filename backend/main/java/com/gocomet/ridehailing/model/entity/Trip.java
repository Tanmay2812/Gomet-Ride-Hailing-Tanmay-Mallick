package com.gocomet.ridehailing.model.entity;

import com.gocomet.ridehailing.model.enums.TripStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "trips", indexes = {
    @Index(name = "idx_trip_ride", columnList = "rideId"),
    @Index(name = "idx_trip_driver", columnList = "driverId"),
    @Index(name = "idx_trip_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long rideId;

    @Column(nullable = false)
    private Long driverId;

    @Column(nullable = false)
    private Long riderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripStatus status;

    @Column(nullable = false)
    private Double startLatitude;

    @Column(nullable = false)
    private Double startLongitude;

    private Double endLatitude;
    
    private Double endLongitude;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;
    
    private LocalDateTime pausedAt;
    
    private Long pausedDurationSeconds = 0L;

    private Double distanceKm = 0.0;
    
    private Long durationMinutes = 0L;

    private Double baseFare;
    
    private Double surgeMultiplier;
    
    private Double totalFare;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
