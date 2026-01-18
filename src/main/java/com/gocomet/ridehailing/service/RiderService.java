package com.gocomet.ridehailing.service;

import com.gocomet.ridehailing.exception.RiderException;
import com.gocomet.ridehailing.model.dto.CreateRiderRequest;
import com.gocomet.ridehailing.model.dto.RiderDTO;
import com.gocomet.ridehailing.model.dto.UpdateRiderRequest;
import com.gocomet.ridehailing.model.entity.Rider;
import com.gocomet.ridehailing.repository.RiderRepository;
import com.newrelic.api.agent.Trace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiderService {

    private final RiderRepository riderRepository;

    @Trace
    public RiderDTO getRiderById(Long riderId) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new RiderException("Rider not found with ID: " + riderId));
        return mapToDto(rider);
    }

    @Trace
    @Transactional
    public RiderDTO createRider(CreateRiderRequest request) {
        // Check if phone number already exists
        if (riderRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new RiderException("Rider with phone number " + request.getPhoneNumber() + " already exists");
        }

        Rider rider = Rider.builder()
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .region(request.getRegion())
                .tenantId(request.getTenantId())
                .rating(request.getRating() != null ? request.getRating() : 5.0)
                .totalRides(0)
                .build();

        rider = riderRepository.save(rider);
        log.info("Created rider: {} with ID: {}", rider.getName(), rider.getId());
        return mapToDto(rider);
    }

    @Trace
    @Transactional
    public RiderDTO updateRider(Long riderId, UpdateRiderRequest request) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new RiderException("Rider not found with ID: " + riderId));

        // Check phone number uniqueness if being updated
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(rider.getPhoneNumber())) {
            if (riderRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
                throw new RiderException("Phone number " + request.getPhoneNumber() + " is already in use");
            }
            rider.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getName() != null) {
            rider.setName(request.getName());
        }
        if (request.getEmail() != null) {
            rider.setEmail(request.getEmail());
        }
        if (request.getRegion() != null) {
            rider.setRegion(request.getRegion());
        }
        if (request.getTenantId() != null) {
            rider.setTenantId(request.getTenantId());
        }
        if (request.getRating() != null) {
            rider.setRating(request.getRating());
        }

        rider = riderRepository.save(rider);
        log.info("Updated rider: {} with ID: {}", rider.getName(), rider.getId());
        return mapToDto(rider);
    }

    private RiderDTO mapToDto(Rider rider) {
        return RiderDTO.builder()
                .id(rider.getId())
                .name(rider.getName())
                .phoneNumber(rider.getPhoneNumber())
                .email(rider.getEmail())
                .region(rider.getRegion())
                .tenantId(rider.getTenantId())
                .rating(rider.getRating())
                .totalRides(rider.getTotalRides())
                .createdAt(rider.getCreatedAt())
                .updatedAt(rider.getUpdatedAt())
                .build();
    }
}
