package com.ezmeal.shipment.application.dto.response;

import com.ezmeal.shipment.domain.entity.Shipment;
import com.ezmeal.shipment.domain.entity.ShipmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShipmentResponse(
        UUID id,
        UUID orderId,
        ShipmentStatus status,
        String trackingNumber,
        LocalDateTime startedAt,
        LocalDateTime deliveredAt,
        LocalDateTime canceledAt,
        LocalDateTime createdAt
) {
    public static ShipmentResponse from(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getId(),
                shipment.getOrderId(),
                shipment.getStatus(),
                shipment.getTrackingNumber(),
                shipment.getStartedAt(),
                shipment.getDeliveredAt(),
                shipment.getCanceledAt(),
                shipment.getCreatedAt()
        );
    }
}
