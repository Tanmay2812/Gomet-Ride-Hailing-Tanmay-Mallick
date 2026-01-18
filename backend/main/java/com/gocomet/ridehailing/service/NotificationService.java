package com.gocomet.ridehailing.service;

import com.gocomet.ridehailing.model.dto.RideResponse;
import com.gocomet.ridehailing.model.entity.Ride;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    @Async
    public void notifyRider(Long riderId, String eventType, Object data) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("eventType", eventType);
            message.put("data", data);
            message.put("timestamp", System.currentTimeMillis());
            
            messagingTemplate.convertAndSend("/topic/rider/" + riderId, message);
            log.debug("Notified rider {} about event: {}", riderId, eventType);
        } catch (Exception e) {
            log.error("Error notifying rider", e);
        }
    }
    
    @Async
    public void notifyDriver(Long driverId, String eventType, Object data) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("eventType", eventType);
            message.put("data", data);
            message.put("timestamp", System.currentTimeMillis());
            
            messagingTemplate.convertAndSend("/topic/driver/" + driverId, message);
            log.debug("Notified driver {} about event: {}", driverId, eventType);
        } catch (Exception e) {
            log.error("Error notifying driver", e);
        }
    }
    
    @Async
    public void broadcastRideUpdate(RideResponse rideResponse) {
        try {
            messagingTemplate.convertAndSend("/topic/rides/updates", rideResponse);
            log.debug("Broadcasted ride update for ride {}", rideResponse.getId());
        } catch (Exception e) {
            log.error("Error broadcasting ride update", e);
        }
    }
    
    public void sendRideMatchedNotification(Ride ride, Long driverId) {
        notifyRider(ride.getRiderId(), "RIDE_MATCHED", Map.of(
            "rideId", ride.getId(),
            "driverId", driverId,
            "message", "Driver found! They are on their way."
        ));
        
        notifyDriver(driverId, "NEW_RIDE_REQUEST", Map.of(
            "rideId", ride.getId(),
            "pickupAddress", ride.getPickupAddress(),
            "pickupLatitude", ride.getPickupLatitude(),
            "pickupLongitude", ride.getPickupLongitude()
        ));
    }
    
    public void sendRideAcceptedNotification(Ride ride) {
        notifyRider(ride.getRiderId(), "RIDE_ACCEPTED", Map.of(
            "rideId", ride.getId(),
            "message", "Driver accepted your ride!"
        ));
    }
    
    public void sendTripStartedNotification(Ride ride) {
        notifyRider(ride.getRiderId(), "TRIP_STARTED", Map.of(
            "rideId", ride.getId(),
            "message", "Your trip has started!"
        ));
        
        notifyDriver(ride.getDriverId(), "TRIP_STARTED", Map.of(
            "rideId", ride.getId(),
            "message", "Trip started successfully!"
        ));
    }
    
    public void sendTripEndedNotification(Ride ride, Double fare) {
        notifyRider(ride.getRiderId(), "TRIP_ENDED", Map.of(
            "rideId", ride.getId(),
            "fare", fare,
            "message", "Trip completed! Total fare: ₹" + fare
        ));
        
        notifyDriver(ride.getDriverId(), "TRIP_ENDED", Map.of(
            "rideId", ride.getId(),
            "fare", fare,
            "message", "Trip completed successfully!"
        ));
    }
}
