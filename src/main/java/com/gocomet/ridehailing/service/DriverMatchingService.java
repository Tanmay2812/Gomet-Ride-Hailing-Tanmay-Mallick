package com.gocomet.ridehailing.service;

import com.gocomet.ridehailing.model.dto.Location;
import com.gocomet.ridehailing.model.entity.Driver;
import com.gocomet.ridehailing.model.enums.DriverStatus;
import com.gocomet.ridehailing.model.enums.VehicleTier;
import com.gocomet.ridehailing.repository.DriverRepository;
import com.newrelic.api.agent.Trace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverMatchingService {
    
    private final DriverRepository driverRepository;
    private final LocationCacheService locationCacheService;
    
    @Value("${app.matching.search-radius-km:5.0}")
    private Double searchRadiusKm;
    
    @Value("${app.matching.max-drivers-to-consider:20}")
    private Integer maxDriversToConsider;
    
    @Trace
    public Optional<Driver> findBestDriver(Double pickupLat, Double pickupLon, 
                                          VehicleTier vehicleTier, String region) {
        try {
            long startTime = System.currentTimeMillis();
            
            // Step 1: Get all available drivers from database (pre-filtered by tier and region)
            List<Driver> availableDrivers = driverRepository
                .findAvailableDriversByTierAndRegion(DriverStatus.AVAILABLE, vehicleTier, region);
            
            if (availableDrivers.isEmpty()) {
                log.warn("No available drivers found for tier {} in region {}", vehicleTier, region);
                return Optional.empty();
            }
            
            // Step 2: Get nearby drivers using Redis geospatial query
            List<Long> nearbyDriverIds = locationCacheService
                .findNearbyDrivers(pickupLat, pickupLon, searchRadiusKm);
            
            if (nearbyDriverIds.isEmpty()) {
                log.warn("No nearby drivers found within {}km of pickup location", searchRadiusKm);
                return Optional.empty();
            }
            
            // Step 3: Filter available drivers who are also nearby
            List<Long> candidateDriverIds = availableDrivers.stream()
                .map(Driver::getId)
                .filter(nearbyDriverIds::contains)
                .limit(maxDriversToConsider)
                .collect(Collectors.toList());
            
            if (candidateDriverIds.isEmpty()) {
                log.warn("No available drivers nearby for tier {} in region {}", vehicleTier, region);
                return Optional.empty();
            }
            
            // Step 4: Get locations for candidate drivers
            Map<Long, Location> driverLocations = locationCacheService.getDriverLocations(candidateDriverIds);
            
            // Step 5: Find best driver based on distance and rating
            Optional<Driver> bestDriver = candidateDriverIds.stream()
                .filter(driverLocations::containsKey)
                .map(driverId -> {
                    Driver driver = availableDrivers.stream()
                        .filter(d -> d.getId().equals(driverId))
                        .findFirst()
                        .orElse(null);
                    return driver;
                })
                .filter(driver -> driver != null)
                .min(Comparator
                    .comparingDouble((Driver d) -> {
                        Location loc = driverLocations.get(d.getId());
                        return Location.calculateDistance(pickupLat, pickupLon, 
                            loc.getLatitude(), loc.getLongitude());
                    })
                    .thenComparing(Comparator.comparingDouble(Driver::getRating).reversed())
                );
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Driver matching completed in {}ms for region {}, tier {}. " +
                    "Available: {}, Nearby: {}, Candidates: {}, Matched: {}", 
                duration, region, vehicleTier, availableDrivers.size(), 
                nearbyDriverIds.size(), candidateDriverIds.size(), bestDriver.isPresent());
            
            return bestDriver;
        } catch (Exception e) {
            log.error("Error finding best driver", e);
            return Optional.empty();
        }
    }
}
