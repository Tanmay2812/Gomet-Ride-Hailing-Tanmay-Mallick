package com.gocomet.ridehailing.repository;

import com.gocomet.ridehailing.model.entity.Payment;
import com.gocomet.ridehailing.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    
    Optional<Payment> findByRideId(Long rideId);
    
    Optional<Payment> findByTripId(Long tripId);
    
    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.retryCount < :maxRetries")
    List<Payment> findFailedPaymentsForRetry(
        @Param("status") PaymentStatus status,
        @Param("maxRetries") Integer maxRetries
    );
}
