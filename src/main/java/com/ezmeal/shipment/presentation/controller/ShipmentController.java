package com.ezmeal.shipment.presentation.controller;

import com.ezmeal.shipment.application.dto.request.ShipmentStartRequest;
import com.ezmeal.shipment.application.dto.response.ShipmentResponse;
import com.ezmeal.shipment.application.service.ShipmentApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentApplicationService shipmentApplicationService;

    @GetMapping("/api/v1/shipments/{orderId}")
    public ResponseEntity<Map<String, Object>> getShipment(
        @PathVariable UUID orderId,
        @RequestHeader("X-User-Id")   String userId,
        @RequestHeader("X-User-Role") String role,
        @RequestHeader(value = "X-Company-Id", required = false) String companyId
    ) {
        ShipmentResponse data = shipmentApplicationService.getShipment(orderId, role, userId, companyId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "배송 상태 조회 성공",
            "data",    data
        ));
    }

    @PatchMapping("/api/v1/shipments/{orderId}/start")
    public ResponseEntity<Map<String, Object>> startShipment(
        @PathVariable UUID orderId,
        @RequestBody ShipmentStartRequest request,
        @RequestHeader("X-User-Id")   String userId,
        @RequestHeader("X-User-Role") String role,
        @RequestHeader(value = "X-Company-Id", required = false) String companyId
    ) {
        ShipmentResponse data = shipmentApplicationService.startShipment(orderId, request, role, companyId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "배송이 시작되었습니다.",
            "data",    data
        ));
    }

    @PatchMapping("/api/v1/shipments/{orderId}/delivered")
    public ResponseEntity<Map<String, Object>> deliverShipment(
        @PathVariable UUID orderId,
        @RequestHeader("X-User-Id")   String userId,
        @RequestHeader("X-User-Role") String role,
        @RequestHeader(value = "X-Company-Id", required = false) String companyId
    ) {
        ShipmentResponse data = shipmentApplicationService.deliverShipment(orderId, role, companyId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "배송이 완료되었습니다.",
            "data",    data
        ));
    }

    // API 스펙 원문: /api/v1/shipment/{orderId}/cancel (단수)
    @PatchMapping("/api/v1/shipment/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelShipment(
        @PathVariable UUID orderId,
        @RequestHeader("X-User-Id")   String userId,
        @RequestHeader("X-User-Role") String role,
        @RequestHeader(value = "X-Company-Id", required = false) String companyId
    ) {
        ShipmentResponse data = shipmentApplicationService.cancelShipment(orderId, role, companyId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "배송이 취소되었습니다.",
            "data",    data
        ));
    }
}
