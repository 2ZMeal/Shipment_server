package com.ezmeal.shipment.presentation.controller;

import com.ezmeal.common.response.CommonApiResponse;
import com.ezmeal.shipment.application.dto.request.ShipmentStartRequest;
import com.ezmeal.shipment.application.dto.response.ShipmentResponse;
import com.ezmeal.shipment.application.service.ShipmentApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentApplicationService shipmentApplicationService;

    // orderId 기준 조회 → 업체별 Shipment 목록 반환
    @GetMapping("/api/v1/shipments/{orderId}")
    public ResponseEntity<CommonApiResponse<List<ShipmentResponse>>> getShipment(
        @PathVariable UUID orderId,
        @RequestHeader(value = "X-Company-Id", required = false) String companyId
    ) {
        List<ShipmentResponse> data = shipmentApplicationService.getShipmentsByOrderId(orderId, companyId);
        return ResponseEntity.ok(CommonApiResponse.success("배송 상태 조회 성공", data));
    }

    // 특정 Shipment 배송 시작 (shipmentId 기준)
    @PatchMapping("/api/v1/shipments/{shipmentId}/start")
    public ResponseEntity<CommonApiResponse<ShipmentResponse>> startShipment(
        @PathVariable UUID shipmentId,
        @RequestBody ShipmentStartRequest request,
        @RequestHeader(value = "X-Company-Id", required = false) String companyId
    ) {
        ShipmentResponse data = shipmentApplicationService.startShipment(shipmentId, request, companyId);
        return ResponseEntity.ok(CommonApiResponse.success("배송이 시작되었습니다.", data));
    }

    // 특정 Shipment 배송 완료 (shipmentId 기준)
    @PatchMapping("/api/v1/shipments/{shipmentId}/delivered")
    public ResponseEntity<CommonApiResponse<ShipmentResponse>> deliverShipment(
        @PathVariable UUID shipmentId,
        @RequestHeader(value = "X-Company-Id", required = false) String companyId
    ) {
        ShipmentResponse data = shipmentApplicationService.deliverShipment(shipmentId, companyId);
        return ResponseEntity.ok(CommonApiResponse.success("배송이 완료되었습니다.", data));
    }

    // 특정 Shipment 배송 취소 (shipmentId 기준)
    @PatchMapping("/api/v1/shipments/{shipmentId}/cancel")
    public ResponseEntity<CommonApiResponse<ShipmentResponse>> cancelShipment(
        @PathVariable UUID shipmentId,
        @RequestHeader(value = "X-Company-Id", required = false) String companyId
    ) {
        ShipmentResponse data = shipmentApplicationService.cancelShipment(shipmentId, companyId);
        return ResponseEntity.ok(CommonApiResponse.success("배송이 취소되었습니다.", data));
    }
}
