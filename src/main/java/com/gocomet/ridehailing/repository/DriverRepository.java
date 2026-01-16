package com.gocomet.ridehailing.repository;

import com.gocomet.ridehailing.model.entity.Driver;
import com.gocomet.ridehailing.model.enums.DriverStatus;
import com.gocomet.ridehailing.model.enums.VehicleTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    
    Optional<Driver> findByPhoneNumber(String phoneNumber);
    
    Optional<Driver> findByLicenseNumber(String licenseNumber);
    
    @Query("SELECT d FROM Driver d WHERE d.status = :status AND d.vehicleTier = :vehicleTier AND d.region = :region")
    List<Driver> findAvailableDriversByTierAndRegion(
        @Param("status") DriverStatus status,
        @Param("vehicleTier") VehicleTier vehicleTier,
        @Param("region") String region
    );
    
    @Modifying
    @Query("UPDATE Driver d SET d.status = :status WHERE d.id = :driverId")
    int updateDriverStatus(@Param("driverId") Long driverId, @Param("status") DriverStatus status);
    
    @Query("SELECT d FROM Driver d WHERE d.id IN :driverIds")
    List<Driver> findAllByIds(@Param("driverIds") List<Long> driverIds);
}
