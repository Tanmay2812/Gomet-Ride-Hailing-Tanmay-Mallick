package com.gocomet.ridehailing.repository;

import com.gocomet.ridehailing.model.entity.Ride;
import com.gocomet.ridehailing.model.enums.RideStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {
    
    Optional<Ride> findByIdempotencyKey(String idempotencyKey);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Ride r WHERE r.id = :id")
    Optional<Ride> findByIdWithLock(@Param("id") Long id);
    
    @Query("SELECT r FROM Ride r WHERE r.riderId = :riderId ORDER BY r.createdAt DESC")
    List<Ride> findByRiderIdOrderByCreatedAtDesc(@Param("riderId") Long riderId);
    
    @Query("SELECT r FROM Ride r WHERE r.driverId = :driverId ORDER BY r.createdAt DESC")
    List<Ride> findByDriverIdOrderByCreatedAtDesc(@Param("driverId") Long driverId);
    
    @Query("SELECT r FROM Ride r WHERE r.status = :status AND r.region = :region")
    List<Ride> findByStatusAndRegion(@Param("status") RideStatus status, @Param("region") String region);
    
    @Query("SELECT COUNT(r) FROM Ride r WHERE r.region = :region AND r.status IN :statuses AND r.createdAt > :since")
    Long countActiveRidesByRegion(
        @Param("region") String region,
        @Param("statuses") List<RideStatus> statuses,
        @Param("since") LocalDateTime since
    );
    
    @Modifying
    @Query("UPDATE Ride r SET r.status = :status, r.driverId = :driverId, r.matchedAt = :matchedAt WHERE r.id = :rideId")
    int assignDriver(
        @Param("rideId") Long rideId,
        @Param("driverId") Long driverId,
        @Param("status") RideStatus status,
        @Param("matchedAt") LocalDateTime matchedAt
    );
}
