package com.gocomet.ridehailing.controller;

import com.gocomet.ridehailing.model.dto.ApiResponse;
import com.gocomet.ridehailing.model.dto.PaymentRequest;
import com.gocomet.ridehailing.model.dto.PaymentResponse;
import com.gocomet.ridehailing.service.PaymentService;
import com.newrelic.api.agent.Trace;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment processing APIs")
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @PostMapping
    @Trace(dispatcher = true)
    @Operation(summary = "Process payment", description = "Processes payment for a completed trip via external PSP")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("Processing payment for ride: {}", request.getRideId());
        long startTime = System.currentTimeMillis();
        
        PaymentResponse response = paymentService.processPayment(request);
        
        long duration = System.currentTimeMillis() - startTime;
        log.info("Payment {} processed in {}ms with status: {}", 
            response.getId(), duration, response.getStatus());
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Payment processed successfully", response));
    }
    
    @PostMapping("/{id}/retry")
    @Trace(dispatcher = true)
    @Operation(summary = "Retry failed payment", description = "Retries a failed payment transaction")
    public ResponseEntity<ApiResponse<PaymentResponse>> retryPayment(@PathVariable Long id) {
        log.info("Retrying payment: {}", id);
        
        PaymentResponse response = paymentService.retryPayment(id);
        
        return ResponseEntity.ok(ApiResponse.success("Payment retried successfully", response));
    }
    
    @GetMapping("/ride/{rideId}")
    @Trace(dispatcher = true)
    @Operation(summary = "Get payment by ride ID", description = "Retrieves payment details for a specific ride")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByRideId(@PathVariable Long rideId) {
        PaymentResponse response = paymentService.getPaymentByRideId(rideId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
