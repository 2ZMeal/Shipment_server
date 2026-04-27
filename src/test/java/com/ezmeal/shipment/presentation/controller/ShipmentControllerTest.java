package com.ezmeal.shipment.presentation.controller;

import com.ezmeal.common.exception.handler.GlobalExceptionHandler;
import com.ezmeal.common.exception.types.ForbiddenException;
import com.ezmeal.common.exception.types.NotFoundException;
import com.ezmeal.shipment.application.dto.request.ShipmentStartRequest;
import com.ezmeal.shipment.application.dto.response.ShipmentResponse;
import com.ezmeal.shipment.application.service.ShipmentApplicationService;
import com.ezmeal.shipment.domain.entity.ShipmentStatus;
import com.ezmeal.shipment.domain.exception.ShipmentErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShipmentController.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser
class ShipmentControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ShipmentApplicationService shipmentApplicationService;

    private final ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final UUID ORDER_ID   = UUID.randomUUID();
    private static final UUID USER_ID    = UUID.randomUUID();
    private static final UUID COMPANY_ID = UUID.randomUUID();

    private ShipmentResponse dummyResponse(ShipmentStatus status) {
        return new ShipmentResponse(
            UUID.randomUUID(), ORDER_ID, status,
            null, null, null, null, LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("GET /api/v1/shipments/{orderId} — 200 정상 조회")
    void getShipment_returns200() throws Exception {
        given(shipmentApplicationService.getShipment(any(), any()))
            .willReturn(dummyResponse(ShipmentStatus.PREPARING));

        mockMvc.perform(get("/api/v1/shipments/{orderId}", ORDER_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("PREPARING"));
    }

    @Test
    @DisplayName("GET /api/v1/shipments/{orderId} — 404 배송 정보 없음")
    void getShipment_returns404() throws Exception {
        given(shipmentApplicationService.getShipment(any(), any()))
            .willThrow(new NotFoundException(ShipmentErrorCode.SHIPMENT_NOT_FOUND));

        mockMvc.perform(get("/api/v1/shipments/{orderId}", ORDER_ID))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("SHIPMENT_001"));
    }

    @Test
    @DisplayName("PATCH /api/v1/shipments/{orderId}/start — 200 배송 시작")
    void startShipment_returns200() throws Exception {
        given(shipmentApplicationService.startShipment(any(), any(), any()))
            .willReturn(dummyResponse(ShipmentStatus.IN_DELIVERY));

        ShipmentStartRequest request = new ShipmentStartRequest("TRACK123", LocalDateTime.now());

        mockMvc.perform(patch("/api/v1/shipments/{orderId}/start", ORDER_ID)
                .with(csrf())
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("IN_DELIVERY"));
    }

    @Test
    @DisplayName("PATCH /api/v1/shipments/{orderId}/delivered — 200 배송 완료")
    void deliverShipment_returns200() throws Exception {
        given(shipmentApplicationService.deliverShipment(any(), any()))
            .willReturn(dummyResponse(ShipmentStatus.DELIVERED));

        mockMvc.perform(patch("/api/v1/shipments/{orderId}/delivered", ORDER_ID)
                .with(csrf())
                .header("X-Company-Id", COMPANY_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("DELIVERED"));
    }

    @Test
    @DisplayName("PATCH /api/v1/shipment/{orderId}/cancel — 200 배송 취소")
    void cancelShipment_returns200() throws Exception {
        given(shipmentApplicationService.cancelShipment(any(), any()))
            .willReturn(dummyResponse(ShipmentStatus.CANCELLED));

        mockMvc.perform(patch("/api/v1/shipment/{orderId}/cancel", ORDER_ID)
                .with(csrf())
                .header("X-Company-Id", COMPANY_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("PATCH start — 403 권한 없음")
    void startShipment_returns403() throws Exception {
        given(shipmentApplicationService.startShipment(any(), any(), any()))
            .willThrow(new ForbiddenException(ShipmentErrorCode.ACCESS_DENIED));

        ShipmentStartRequest request = new ShipmentStartRequest("TRACK123", null);

        mockMvc.perform(patch("/api/v1/shipments/{orderId}/start", ORDER_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("SHIPMENT_403"));
    }
}
