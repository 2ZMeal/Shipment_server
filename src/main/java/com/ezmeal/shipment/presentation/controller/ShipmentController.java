package com.ezmeal.shipment.presentation.controller;

import com.ezmeal.common.response.CommonApiResponse;
import com.ezmeal.shipment.application.dto.request.ShipmentStartRequest;
import com.ezmeal.shipment.application.dto.response.ShipmentResponse;
import com.ezmeal.shipment.application.service.ShipmentApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentApplicationService shipmentApplicationService;

    @GetMapping("/api/v1/shipments/{orderId}")
    public ResponseEntity<CommonApiResponse<ShipmentResponse>> getShipment(
        @PathVariable UUID orderId,
        @RequestHeader(value = "X-Company-Id", required = false) String companyId
    ) {
        ShipmentResponse data = shipmentApplicationService.getShipment(orderId, companyId);
        return ResponseEntity.ok(CommonApiResponse.success("배송 상태 조회 성공", data));
    }

    @PatchMapping("/api/v1/shipments/{orderId}/start")
    public ResponseEntity<CommonApiResponse<ShipmentResponse>> startShipment(
        @PathVariable UUID orderId,
        @RequestBody ShipmentStartRequest request,
        @RequestHeader(value = "X-Company-Id", required = false) String companyId
    ) {
        ShipmentResponse data = shipmentApplicationService.startShipment(orderId, request, companyId);
        return ResponseEntity.ok(CommonApiResponse.success("배송이 시작되었습니다.", data));
    }

    @PatchMapping("/api/v1/shipments/{orderId}/delivered")
    public ResponseEntity<CommonApiResponse<ShipmentResponse>> deliverShipment(
        @PathVariable UUID orderId,
        @RequestHeader(value = "X-Company-Id", required = false) String companyId
    ) {
        ShipmentResponse data = shipmentApplicationService.deliverShipment(orderId, companyId);
        return ResponseEntity.ok(CommonApiResponse.success("배송이 완료되었습니다.", data));
    }

    // API 스펙 원문: /api/v1/shipment/{orderId}/cancel (단수)
    @PatchMapping("/api/v1/shipment/{orderId}/cancel")
    public ResponseEntity<CommonApiResponse<ShipmentResponse>> cancelShipment(
        @PathVariable UUID orderId,
        @RequestHeader(value = "X-Company-Id", required = false) String companyId
    ) {
        ShipmentResponse data = shipmentApplicationService.cancelShipment(orderId, companyId);
        return ResponseEntity.ok(CommonApiResponse.success("배송이 취소되었습니다.", data));
    }
}
