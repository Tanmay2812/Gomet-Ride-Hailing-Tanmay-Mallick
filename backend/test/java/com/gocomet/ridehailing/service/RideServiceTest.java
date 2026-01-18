package com.gocomet.ridehailing.service;

import com.gocomet.ridehailing.model.dto.CreateRideRequest;
import com.gocomet.ridehailing.model.dto.RideResponse;
import com.gocomet.ridehailing.model.entity.Driver;
import com.gocomet.ridehailing.model.entity.Ride;
import com.gocomet.ridehailing.model.enums.*;
import com.gocomet.ridehailing.repository.DriverRepository;
import com.gocomet.ridehailing.repository.RideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private DriverMatchingService driverMatchingService;

    @Mock
    private SurgePricingService surgePricingService;

    @Mock
    private FareCalculationService fareCalculationService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private LocationCacheService locationCacheService;

    @InjectMocks
    private RideService rideService;

    private CreateRideRequest validRequest;
    private Ride mockRide;

    @BeforeEach
    void setUp() {
        validRequest = CreateRideRequest.builder()
                .riderId(1L)
                .pickupLatitude(28.6139)
                .pickupLongitude(77.2090)
                .pickupAddress("Connaught Place, New Delhi")
                .destinationLatitude(28.5355)
                .destinationLongitude(77.3910)
                .destinationAddress("Noida Sector 18")
                .vehicleTier(VehicleTier.ECONOMY)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .region("Delhi-NCR")
                .build();

        mockRide = Ride.builder()
                .id(1L)
                .idempotencyKey("test-key")
                .riderId(1L)
                .status(RideStatus.REQUESTED)
                .vehicleTier(VehicleTier.ECONOMY)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .pickupLatitude(28.6139)
                .pickupLongitude(77.2090)
                .pickupAddress("Connaught Place, New Delhi")
                .destinationLatitude(28.5355)
                .destinationLongitude(77.3910)
                .destinationAddress("Noida Sector 18")
                .region("Delhi-NCR")
                .estimatedFare(150.0)
                .surgeMultiplier(1.0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateRide_Success() {
        // Arrange
        when(surgePricingService.calculateSurgeMultiplier(anyString())).thenReturn(1.0);
        when(fareCalculationService.calculateEstimatedFare(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(150.0);
        when(rideRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(rideRepository.save(any(Ride.class))).thenReturn(mockRide);

        // Act
        RideResponse response = rideService.createRide(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(RideStatus.REQUESTED, response.getStatus());
        assertEquals(150.0, response.getEstimatedFare());
        
        verify(rideRepository, times(1)).save(any(Ride.class));
        verify(surgePricingService, times(1)).calculateSurgeMultiplier(anyString());
        verify(fareCalculationService, times(1)).calculateEstimatedFare(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    void testCreateRide_IdempotencyCheck() {
        // Arrange
        when(rideRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.of(mockRide));

        // Act
        validRequest.setIdempotencyKey("existing-key");
        RideResponse response = rideService.createRide(validRequest);

        // Assert
        assertNotNull(response);
        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    void testGetRideById_Success() {
        // Arrange
        when(rideRepository.findById(1L)).thenReturn(Optional.of(mockRide));

        // Act
        RideResponse response = rideService.getRideById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(RideStatus.REQUESTED, response.getStatus());
    }

    @Test
    void testAcceptRide_Success() {
        // Arrange
        mockRide.setStatus(RideStatus.MATCHED);
        mockRide.setDriverId(1L);
        
        when(rideRepository.findByIdWithLock(1L)).thenReturn(Optional.of(mockRide));
        when(rideRepository.save(any(Ride.class))).thenReturn(mockRide);
        when(driverRepository.updateDriverStatus(1L, DriverStatus.ON_RIDE)).thenReturn(1);

        // Act
        RideResponse response = rideService.acceptRide(1L, 1L);

        // Assert
        assertNotNull(response);
        verify(rideRepository, times(1)).save(any(Ride.class));
        verify(driverRepository, times(1)).updateDriverStatus(1L, DriverStatus.ON_RIDE);
        verify(notificationService, times(1)).sendRideAcceptedNotification(any(Ride.class));
    }

    @Test
    void testCancelRide_Success() {
        // Arrange
        mockRide.setDriverId(1L);
        when(rideRepository.findByIdWithLock(1L)).thenReturn(Optional.of(mockRide));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride ride = invocation.getArgument(0);
            ride.setStatus(RideStatus.CANCELLED);
            return ride;
        });

        // Act
        RideResponse response = rideService.cancelRide(1L, "User cancelled");

        // Assert
        assertNotNull(response);
        assertEquals(RideStatus.CANCELLED, response.getStatus());
        verify(driverRepository, times(1)).updateDriverStatus(1L, DriverStatus.AVAILABLE);
    }
}
