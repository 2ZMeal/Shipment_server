package com.ezmeal.shipment.domain.event.payload;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ShipmentStartedPayload(
        UUID eventId,
        String eventType,
        LocalDateTime occurredAt,
        UUID shipmentId,
        UUID orderId,
        UUID userId,
        String trackingNumber,
        LocalDateTime startedAt
) {
    public static ShipmentStartedPayload of(UUID shipmentId, UUID orderId, UUID userId,
                                            String trackingNumber, LocalDateTime startedAt) {
        return ShipmentStartedPayload.builder()
                .eventId(UUID.randomUUID())
                .eventType("ShipmentStarted")
                .occurredAt(LocalDateTime.now())
                .shipmentId(shipmentId)
                .orderId(orderId)
                .userId(userId)
                .trackingNumber(trackingNumber)
                .startedAt(startedAt)
                .build();
    }
}
