package com.gocomet.ridehailing.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class FareCalculationServiceTest {

    private FareCalculationService fareCalculationService;

    @BeforeEach
    void setUp() {
        fareCalculationService = new FareCalculationService();
        
        // Set test values
        ReflectionTestUtils.setField(fareCalculationService, "baseFare", 50.0);
        ReflectionTestUtils.setField(fareCalculationService, "perKmRate", 12.0);
        ReflectionTestUtils.setField(fareCalculationService, "perMinuteRate", 2.0);
        ReflectionTestUtils.setField(fareCalculationService, "minimumFare", 70.0);
    }

    @Test
    void testCalculateEstimatedFare_NoSurge() {
        // Act
        Double fare = fareCalculationService.calculateEstimatedFare(
            28.6139, 77.2090,  // Pickup
            28.5355, 77.3910,  // Destination
            1.0                // No surge
        );

        // Assert
        assertNotNull(fare);
        assertTrue(fare >= 70.0); // Should be at least minimum fare
    }

    @Test
    void testCalculateEstimatedFare_WithSurge() {
        // Act
        Double fare = fareCalculationService.calculateEstimatedFare(
            28.6139, 77.2090,
            28.5355, 77.3910,
            2.0  // 2x surge
        );

        // Assert
        assertNotNull(fare);
        Double normalFare = fareCalculationService.calculateEstimatedFare(
            28.6139, 77.2090,
            28.5355, 77.3910,
            1.0
        );
        
        assertTrue(fare > normalFare);
    }

    @Test
    void testCalculateFinalFare() {
        // Act
        Double fare = fareCalculationService.calculateFinalFare(
            15.5,  // distance in km
            45L,   // duration in minutes
            1.5    // surge multiplier
        );

        // Assert
        assertNotNull(fare);
        
        // Expected: (50 + (15.5 * 12) + (45 * 2)) * 1.5 = (50 + 186 + 90) * 1.5 = 489
        double expectedFare = (50 + (15.5 * 12) + (45 * 2)) * 1.5;
        assertEquals(Math.round(expectedFare * 100.0) / 100.0, fare);
    }

    @Test
    void testMinimumFare() {
        // Act - very short trip
        Double fare = fareCalculationService.calculateFinalFare(
            0.5,   // 500 meters
            2L,    // 2 minutes
            1.0
        );

        // Assert
        assertNotNull(fare);
        assertEquals(70.0, fare); // Should be minimum fare
    }
}
