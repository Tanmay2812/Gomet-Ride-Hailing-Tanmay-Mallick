package com.gocomet.ridehailing.repository;

import com.gocomet.ridehailing.model.entity.Trip;
import com.gocomet.ridehailing.model.enums.TripStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    
    Optional<Trip> findByRideId(Long rideId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Trip t WHERE t.id = :id")
    Optional<Trip> findByIdWithLock(@Param("id") Long id);
    
    @Query("SELECT t FROM Trip t WHERE t.driverId = :driverId AND t.status = :status")
    Optional<Trip> findActiveByDriverId(@Param("driverId") Long driverId, @Param("status") TripStatus status);
}
