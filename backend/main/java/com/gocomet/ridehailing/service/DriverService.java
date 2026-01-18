package com.gocomet.ridehailing.service;

import com.gocomet.ridehailing.exception.DriverException;
import com.gocomet.ridehailing.model.dto.CreateDriverRequest;
import com.gocomet.ridehailing.model.dto.DriverDTO;
import com.gocomet.ridehailing.model.dto.UpdateDriverRequest;
import com.gocomet.ridehailing.model.entity.Driver;
import com.gocomet.ridehailing.repository.DriverRepository;
import com.newrelic.api.agent.Trace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverService {

    private final DriverRepository driverRepository;

    @Trace
    public DriverDTO getDriverById(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverException("Driver not found with ID: " + driverId));
        return mapToDto(driver);
    }

    @Trace
    @Transactional
    public DriverDTO createDriver(CreateDriverRequest request) {
        // Check if phone number already exists
        if (driverRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new DriverException("Driver with phone number " + request.getPhoneNumber() + " already exists");
        }

        // Check if license number already exists
        if (driverRepository.findByLicenseNumber(request.getLicenseNumber()).isPresent()) {
            throw new DriverException("Driver with license number " + request.getLicenseNumber() + " already exists");
        }

        Driver driver = Driver.builder()
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .licenseNumber(request.getLicenseNumber())
                .vehicleNumber(request.getVehicleNumber())
                .vehicleTier(request.getVehicleTier())
                .status(request.getStatus())
                .region(request.getRegion())
                .tenantId(request.getTenantId())
                .rating(request.getRating() != null ? request.getRating() : 5.0)
                .totalRides(0)
                .build();

        driver = driverRepository.save(driver);
        log.info("Created driver: {} with ID: {}", driver.getName(), driver.getId());
        return mapToDto(driver);
    }

    @Trace
    @Transactional
    public DriverDTO updateDriver(Long driverId, UpdateDriverRequest request) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverException("Driver not found with ID: " + driverId));

        // Check phone number uniqueness if being updated
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(driver.getPhoneNumber())) {
            if (driverRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
                throw new DriverException("Phone number " + request.getPhoneNumber() + " is already in use");
            }
            driver.setPhoneNumber(request.getPhoneNumber());
        }

        // Check license number uniqueness if being updated
        if (request.getLicenseNumber() != null && !request.getLicenseNumber().equals(driver.getLicenseNumber())) {
            if (driverRepository.findByLicenseNumber(request.getLicenseNumber()).isPresent()) {
                throw new DriverException("License number " + request.getLicenseNumber() + " is already in use");
            }
            driver.setLicenseNumber(request.getLicenseNumber());
        }

        if (request.getName() != null) {
            driver.setName(request.getName());
        }
        if (request.getEmail() != null) {
            driver.setEmail(request.getEmail());
        }
        if (request.getVehicleNumber() != null) {
            driver.setVehicleNumber(request.getVehicleNumber());
        }
        if (request.getVehicleTier() != null) {
            driver.setVehicleTier(request.getVehicleTier());
        }
        if (request.getStatus() != null) {
            driver.setStatus(request.getStatus());
        }
        if (request.getRegion() != null) {
            driver.setRegion(request.getRegion());
        }
        if (request.getTenantId() != null) {
            driver.setTenantId(request.getTenantId());
        }
        if (request.getRating() != null) {
            driver.setRating(request.getRating());
        }

        driver = driverRepository.save(driver);
        log.info("Updated driver: {} with ID: {}", driver.getName(), driver.getId());
        return mapToDto(driver);
    }

    private DriverDTO mapToDto(Driver driver) {
        return DriverDTO.builder()
                .id(driver.getId())
                .name(driver.getName())
                .phoneNumber(driver.getPhoneNumber())
                .email(driver.getEmail())
                .licenseNumber(driver.getLicenseNumber())
                .vehicleNumber(driver.getVehicleNumber())
                .vehicleTier(driver.getVehicleTier())
                .status(driver.getStatus())
                .region(driver.getRegion())
                .tenantId(driver.getTenantId())
                .rating(driver.getRating())
                .totalRides(driver.getTotalRides())
                .lastLocationUpdate(driver.getLastLocationUpdate())
                .createdAt(driver.getCreatedAt())
                .updatedAt(driver.getUpdatedAt())
                .build();
    }
}
