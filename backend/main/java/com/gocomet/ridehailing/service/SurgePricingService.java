package com.gocomet.ridehailing.service;

import com.gocomet.ridehailing.model.enums.RideStatus;
import com.gocomet.ridehailing.repository.RideRepository;
import com.newrelic.api.agent.Trace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurgePricingService {
    
    private final RideRepository rideRepository;
    
    @Value("${app.surge.base-multiplier:1.0}")
    private Double baseMultiplier;
    
    @Value("${app.surge.max-multiplier:3.0}")
    private Double maxMultiplier;
    
    @Value("${app.surge.demand-threshold:10}")
    private Integer demandThreshold;
    
    @Trace
    @Cacheable(value = "surgeMultiplier", key = "#region", unless = "#result == 1.0")
    public Double calculateSurgeMultiplier(String region) {
        try {
            // Count active rides in the last 5 minutes
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
            
            List<RideStatus> activeStatuses = List.of(
                RideStatus.REQUESTED,
                RideStatus.SEARCHING,
                RideStatus.MATCHED,
                RideStatus.ACCEPTED,
                RideStatus.IN_PROGRESS
            );
            
            Long activeRidesCount = rideRepository.countActiveRidesByRegion(
                region, activeStatuses, fiveMinutesAgo
            );
            
            if (activeRidesCount <= demandThreshold) {
                return baseMultiplier;
            }
            
            // Calculate surge based on demand
            double surgeMultiplier = baseMultiplier + 
                ((activeRidesCount - demandThreshold) * 0.1);
            
            // Cap at max multiplier
            surgeMultiplier = Math.min(surgeMultiplier, maxMultiplier);
            
            log.info("Surge multiplier for region {}: {} (active rides: {})", 
                region, surgeMultiplier, activeRidesCount);
            
            return surgeMultiplier;
        } catch (Exception e) {
            log.error("Error calculating surge multiplier", e);
            return baseMultiplier;
        }
    }
}
