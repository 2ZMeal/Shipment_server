package com.ezmeal.shipment.domain.event.payload;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ShipmentDeliveredPayload(
        UUID eventId,
        String eventType,
        LocalDateTime occurredAt,
        UUID shipmentId,
        UUID orderId,
        UUID userId,
        LocalDateTime deliveredAt
) {
    public static ShipmentDeliveredPayload of(UUID shipmentId, UUID orderId, UUID userId,
                                              LocalDateTime deliveredAt) {
        return ShipmentDeliveredPayload.builder()
                .eventId(UUID.randomUUID())
                .eventType("ShipmentDelivered")
                .occurredAt(LocalDateTime.now())
                .shipmentId(shipmentId)
                .orderId(orderId)
                .userId(userId)
                .deliveredAt(deliveredAt)
                .build();
    }
}
