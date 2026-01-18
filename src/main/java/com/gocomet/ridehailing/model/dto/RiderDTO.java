package com.gocomet.ridehailing.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiderDTO {
    private Long id;
    private String phoneNumber;
    private String name;
    private String email;
    private String region;
    private String tenantId;
    private Double rating;
    private Integer totalRides;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
