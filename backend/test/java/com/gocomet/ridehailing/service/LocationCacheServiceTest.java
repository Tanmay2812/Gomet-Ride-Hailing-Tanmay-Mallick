package com.gocomet.ridehailing.service;

import com.gocomet.ridehailing.model.dto.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationCacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private LocationCacheService locationCacheService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testUpdateDriverLocation() {
        // Act
        locationCacheService.updateDriverLocation(1L, 28.6139, 77.2090);

        // Assert
        verify(valueOperations, times(1)).set(
            anyString(),
            any(Location.class),
            anyLong(),
            any()
        );
    }

    @Test
    void testGetDriverLocation_Success() {
        // Arrange
        Location expectedLocation = Location.builder()
            .latitude(28.6139)
            .longitude(77.2090)
            .build();
        
        when(valueOperations.get(anyString())).thenReturn(expectedLocation);

        // Act
        Optional<Location> result = locationCacheService.getDriverLocation(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(28.6139, result.get().getLatitude());
        assertEquals(77.2090, result.get().getLongitude());
    }

    @Test
    void testGetDriverLocation_NotFound() {
        // Arrange
        when(valueOperations.get(anyString())).thenReturn(null);

        // Act
        Optional<Location> result = locationCacheService.getDriverLocation(1L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testRemoveDriverLocation() {
        // Act
        locationCacheService.removeDriverLocation(1L);

        // Assert
        verify(redisTemplate, times(1)).delete(anyString());
    }
}
