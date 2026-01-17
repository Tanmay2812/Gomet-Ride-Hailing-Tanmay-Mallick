package com.gocomet.ridehailing.service;

import com.gocomet.ridehailing.exception.PaymentException;
import com.gocomet.ridehailing.model.dto.PaymentRequest;
import com.gocomet.ridehailing.model.dto.PaymentResponse;
import com.gocomet.ridehailing.model.entity.Payment;
import com.gocomet.ridehailing.model.entity.Ride;
import com.gocomet.ridehailing.model.entity.Trip;
import com.gocomet.ridehailing.model.enums.PaymentStatus;
import com.gocomet.ridehailing.repository.PaymentRepository;
import com.gocomet.ridehailing.repository.RideRepository;
import com.gocomet.ridehailing.repository.TripRepository;
import com.newrelic.api.agent.Trace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final RideRepository rideRepository;
    private final TripRepository tripRepository;
    private final NotificationService notificationService;
    
    @Value("${app.payment.timeout-ms:5000}")
    private Long paymentTimeoutMs;
    
    @Value("${app.payment.retry-attempts:3}")
    private Integer maxRetryAttempts;
    
    @Trace
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        try {
            // Check for idempotency
            final String idempotencyKey = (request.getIdempotencyKey() == null || request.getIdempotencyKey().isEmpty()) 
                ? generateIdempotencyKey(request) 
                : request.getIdempotencyKey();
            
            // Check if payment already exists
            return paymentRepository.findByIdempotencyKey(idempotencyKey)
                .map(this::mapToResponse)
                .orElseGet(() -> createAndProcessPayment(request, idempotencyKey));
            
        } catch (Exception e) {
            log.error("Error processing payment", e);
            throw new PaymentException("Failed to process payment: " + e.getMessage());
        }
    }
    
    private PaymentResponse createAndProcessPayment(PaymentRequest request, String idempotencyKey) {
        // Validate ride and trip
        Ride ride = rideRepository.findById(request.getRideId())
            .orElseThrow(() -> new PaymentException("Ride not found"));
        
        // Validate trip exists
        tripRepository.findById(request.getTripId())
            .orElseThrow(() -> new PaymentException("Trip not found"));
        
        // Create payment record
        Payment payment = Payment.builder()
            .idempotencyKey(idempotencyKey)
            .rideId(request.getRideId())
            .tripId(request.getTripId())
            .riderId(ride.getRiderId())
            .driverId(ride.getDriverId())
            .amount(request.getAmount())
            .paymentMethod(request.getPaymentMethod())
            .status(PaymentStatus.PENDING)
            .retryCount(0)
            .build();
        
        payment = paymentRepository.save(payment);
        
        // Process payment with external PSP
        boolean success = processWithPaymentGateway(payment);
        
        if (success) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(UUID.randomUUID().toString());
            log.info("Payment successful for ride {}: ₹{}", request.getRideId(), request.getAmount());
            
            // Send payment success notification
            notificationService.notifyRider(ride.getRiderId(), "PAYMENT_SUCCESS", Map.of(
                "rideId", ride.getId(),
                "paymentId", payment.getId(),
                "amount", payment.getAmount(),
                "transactionId", payment.getTransactionId(),
                "message", "Payment successful! Amount: ₹" + payment.getAmount()
            ));
            
            notificationService.notifyDriver(ride.getDriverId(), "PAYMENT_RECEIVED", Map.of(
                "rideId", ride.getId(),
                "paymentId", payment.getId(),
                "amount", payment.getAmount(),
                "message", "Payment received for ride #" + ride.getId()
            ));
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Payment gateway declined");
            log.error("Payment failed for ride {}", request.getRideId());
            
            // Send payment failure notification
            notificationService.notifyRider(ride.getRiderId(), "PAYMENT_FAILED", Map.of(
                "rideId", ride.getId(),
                "paymentId", payment.getId(),
                "amount", payment.getAmount(),
                "reason", payment.getFailureReason(),
                "message", "Payment failed. Please try again."
            ));
        }
        
        payment = paymentRepository.save(payment);
        return mapToResponse(payment);
    }
    
    @Trace
    private boolean processWithPaymentGateway(Payment payment) {
        try {
            // Simulate payment gateway call
            // In production, integrate with actual PSP (Stripe, Razorpay, etc.)
            Thread.sleep(500); // Simulate network latency
            
            // Simulate 95% success rate
            boolean success = Math.random() < 0.95;
            
            if (success) {
                log.debug("Payment gateway approved transaction for payment {}", payment.getId());
            } else {
                log.warn("Payment gateway declined transaction for payment {}", payment.getId());
            }
            
            return success;
        } catch (Exception e) {
            log.error("Error calling payment gateway", e);
            return false;
        }
    }
    
    @Trace
    @Transactional
    public PaymentResponse retryPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentException("Payment not found"));
        
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.warn("Payment already successful, skipping retry");
            return mapToResponse(payment);
        }
        
        if (payment.getRetryCount() >= maxRetryAttempts) {
            throw new PaymentException("Max retry attempts exceeded");
        }
        
        payment.setRetryCount(payment.getRetryCount() + 1);
        payment.setStatus(PaymentStatus.PROCESSING);
        payment = paymentRepository.save(payment);
        
        boolean success = processWithPaymentGateway(payment);
        
        Ride ride = rideRepository.findById(payment.getRideId())
            .orElseThrow(() -> new PaymentException("Ride not found"));
        
        if (success) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(UUID.randomUUID().toString());
            
            // Send payment success notification
            notificationService.notifyRider(ride.getRiderId(), "PAYMENT_SUCCESS", Map.of(
                "rideId", ride.getId(),
                "paymentId", payment.getId(),
                "amount", payment.getAmount(),
                "transactionId", payment.getTransactionId(),
                "message", "Payment successful! Amount: ₹" + payment.getAmount()
            ));
            
            notificationService.notifyDriver(ride.getDriverId(), "PAYMENT_RECEIVED", Map.of(
                "rideId", ride.getId(),
                "paymentId", payment.getId(),
                "amount", payment.getAmount(),
                "message", "Payment received for ride #" + ride.getId()
            ));
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            
            // Send payment failure notification
            notificationService.notifyRider(ride.getRiderId(), "PAYMENT_FAILED", Map.of(
                "rideId", ride.getId(),
                "paymentId", payment.getId(),
                "amount", payment.getAmount(),
                "reason", payment.getFailureReason(),
                "message", "Payment failed. Please try again."
            ));
        }
        
        payment = paymentRepository.save(payment);
        return mapToResponse(payment);
    }
    
    @Trace
    public PaymentResponse getPaymentByRideId(Long rideId) {
        return paymentRepository.findByRideId(rideId)
            .map(this::mapToResponse)
            .orElseThrow(() -> new PaymentException("Payment not found for ride"));
    }
    
    private String generateIdempotencyKey(PaymentRequest request) {
        return "payment-" + request.getRideId() + "-" + request.getTripId();
    }
    
    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
            .id(payment.getId())
            .rideId(payment.getRideId())
            .tripId(payment.getTripId())
            .amount(payment.getAmount())
            .paymentMethod(payment.getPaymentMethod())
            .status(payment.getStatus())
            .transactionId(payment.getTransactionId())
            .failureReason(payment.getFailureReason())
            .createdAt(payment.getCreatedAt())
            .build();
    }
}
