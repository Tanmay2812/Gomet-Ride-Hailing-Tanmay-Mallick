package com.gocomet.ridehailing.repository;

import com.gocomet.ridehailing.model.entity.Rider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiderRepository extends JpaRepository<Rider, Long> {
    
    Optional<Rider> findByPhoneNumber(String phoneNumber);
    
    Optional<Rider> findByEmail(String email);
}
