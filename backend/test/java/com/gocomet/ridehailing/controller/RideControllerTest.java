package com.gocomet.ridehailing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gocomet.ridehailing.model.dto.CreateRideRequest;
import com.gocomet.ridehailing.model.dto.RideResponse;
import com.gocomet.ridehailing.model.enums.PaymentMethod;
import com.gocomet.ridehailing.model.enums.RideStatus;
import com.gocomet.ridehailing.model.enums.VehicleTier;
import com.gocomet.ridehailing.service.RideService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RideController.class)
class RideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RideService rideService;

    @Test
    void testCreateRide_Success() throws Exception {
        // Arrange
        CreateRideRequest request = CreateRideRequest.builder()
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

        RideResponse response = RideResponse.builder()
                .id(1L)
                .riderId(1L)
                .status(RideStatus.REQUESTED)
                .estimatedFare(150.0)
                .build();

        when(rideService.createRide(any(CreateRideRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/v1/rides")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("REQUESTED"));
    }

    @Test
    void testCreateRide_ValidationError() throws Exception {
        // Arrange - invalid request (missing required fields)
        CreateRideRequest request = CreateRideRequest.builder()
                .riderId(1L)
                .build();

        // Act & Assert
        mockMvc.perform(post("/v1/rides")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetRideById_Success() throws Exception {
        // Arrange
        RideResponse response = RideResponse.builder()
                .id(1L)
                .riderId(1L)
                .status(RideStatus.REQUESTED)
                .estimatedFare(150.0)
                .build();

        when(rideService.getRideById(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/v1/rides/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }
}
