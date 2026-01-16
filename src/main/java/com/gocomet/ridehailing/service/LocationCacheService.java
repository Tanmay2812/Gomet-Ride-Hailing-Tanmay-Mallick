package com.gocomet.ridehailing.service;

import com.gocomet.ridehailing.model.dto.Location;
import com.newrelic.api.agent.Trace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String DRIVER_LOCATION_PREFIX = "driver:location:";
    private static final String DRIVER_LOCATION_INDEX_PREFIX = "location:index:";
    private static final long LOCATION_TTL_SECONDS = 30;
    
    @Trace
    public void updateDriverLocation(Long driverId, Double latitude, Double longitude) {
        try {
            Location location = Location.builder()
                    .latitude(latitude)
                    .longitude(longitude)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            String key = DRIVER_LOCATION_PREFIX + driverId;
            redisTemplate.opsForValue().set(key, location, LOCATION_TTL_SECONDS, TimeUnit.SECONDS);
            
            // Update geospatial index for fast radius queries
            String geoKey = DRIVER_LOCATION_INDEX_PREFIX + "all";
            redisTemplate.opsForGeo().add(geoKey, new org.springframework.data.geo.Point(longitude, latitude), driverId.toString());
            redisTemplate.expire(geoKey, LOCATION_TTL_SECONDS * 2, TimeUnit.SECONDS);
            
            log.debug("Updated location for driver {}: {}, {}", driverId, latitude, longitude);
        } catch (Exception e) {
            log.error("Error updating driver location", e);
        }
    }
    
    @Trace
    public Optional<Location> getDriverLocation(Long driverId) {
        try {
            String key = DRIVER_LOCATION_PREFIX + driverId;
            Object value = redisTemplate.opsForValue().get(key);
            
            if (value instanceof Location) {
                return Optional.of((Location) value);
            } else if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) value;
                return Optional.of(Location.builder()
                        .latitude(((Number) map.get("latitude")).doubleValue())
                        .longitude(((Number) map.get("longitude")).doubleValue())
                        .build());
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error getting driver location", e);
            return Optional.empty();
        }
    }
    
    @Trace
    public Map<Long, Location> getDriverLocations(List<Long> driverIds) {
        Map<Long, Location> locations = new HashMap<>();
        
        for (Long driverId : driverIds) {
            getDriverLocation(driverId).ifPresent(location -> 
                locations.put(driverId, location)
            );
        }
        
        return locations;
    }
    
    @Trace
    public List<Long> findNearbyDrivers(Double latitude, Double longitude, Double radiusKm) {
        try {
            String geoKey = DRIVER_LOCATION_INDEX_PREFIX + "all";
            
            // Redis GEO commands use meters
            org.springframework.data.geo.Distance distance = 
                new org.springframework.data.geo.Distance(radiusKm, org.springframework.data.geo.Metrics.KILOMETERS);
            org.springframework.data.geo.Point point = new org.springframework.data.geo.Point(longitude, latitude);
            
            var results = redisTemplate.opsForGeo()
                .radius(geoKey, point, distance);
            
            if (results == null || results.getContent().isEmpty()) {
                return Collections.emptyList();
            }
            
            return results.getContent().stream()
                .map(result -> Long.parseLong(result.getContent().getName()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error finding nearby drivers", e);
            return Collections.emptyList();
        }
    }
    
    public void removeDriverLocation(Long driverId) {
        try {
            String key = DRIVER_LOCATION_PREFIX + driverId;
            redisTemplate.delete(key);
            
            String geoKey = DRIVER_LOCATION_INDEX_PREFIX + "all";
            redisTemplate.opsForGeo().remove(geoKey, driverId.toString());
        } catch (Exception e) {
            log.error("Error removing driver location", e);
        }
    }
}
