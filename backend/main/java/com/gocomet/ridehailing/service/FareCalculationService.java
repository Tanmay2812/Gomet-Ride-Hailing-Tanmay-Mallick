package com.gocomet.ridehailing.service;

import com.gocomet.ridehailing.model.dto.Location;
import com.newrelic.api.agent.Trace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FareCalculationService {
    
    @Value("${app.fare.base-fare:50.0}")
    private Double baseFare;
    
    @Value("${app.fare.per-km-rate:12.0}")
    private Double perKmRate;
    
    @Value("${app.fare.per-minute-rate:2.0}")
    private Double perMinuteRate;
    
    @Value("${app.fare.minimum-fare:70.0}")
    private Double minimumFare;
    
    @Trace
    public Double calculateEstimatedFare(Double pickupLat, Double pickupLon, 
                                        Double destLat, Double destLon, 
                                        Double surgeMultiplier) {
        try {
            double distance = Location.calculateDistance(pickupLat, pickupLon, destLat, destLon);
            
            // Estimate time based on average speed of 30 km/h
            double estimatedMinutes = (distance / 30.0) * 60.0;
            
            double fare = baseFare 
                        + (distance * perKmRate) 
                        + (estimatedMinutes * perMinuteRate);
            
            fare = fare * surgeMultiplier;
            
            // Apply minimum fare
            fare = Math.max(fare, minimumFare);
            
            log.debug("Estimated fare: {} (distance: {}km, surge: {}x)", 
                fare, distance, surgeMultiplier);
            
            return Math.round(fare * 100.0) / 100.0; // Round to 2 decimal places
        } catch (Exception e) {
            log.error("Error calculating estimated fare", e);
            return minimumFare;
        }
    }
    
    @Trace
    public Double calculateFinalFare(Double distanceKm, Long durationMinutes, Double surgeMultiplier) {
        try {
            double fare = baseFare 
                        + (distanceKm * perKmRate) 
                        + (durationMinutes * perMinuteRate);
            
            fare = fare * surgeMultiplier;
            
            // Apply minimum fare
            fare = Math.max(fare, minimumFare);
            
            log.info("Final fare: {} (distance: {}km, duration: {}min, surge: {}x)", 
                fare, distanceKm, durationMinutes, surgeMultiplier);
            
            return Math.round(fare * 100.0) / 100.0;
        } catch (Exception e) {
            log.error("Error calculating final fare", e);
            return minimumFare;
        }
    }
}
