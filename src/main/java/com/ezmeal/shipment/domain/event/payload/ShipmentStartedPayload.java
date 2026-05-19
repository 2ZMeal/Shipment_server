package com.ezmeal.shipment.domain.event.payload;

import com.ezmeal.common.message.DomainEvent;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ShipmentStartedPayload(
        UUID shipmentId,
        UUID orderId,
        UUID userId,
        String trackingNumber,
        LocalDateTime startedAt
) implements DomainEvent {

    public static ShipmentStartedPayload of(UUID shipmentId, UUID orderId, UUID userId,
                                            String trackingNumber, LocalDateTime startedAt) {
        return ShipmentStartedPayload.builder()
                .shipmentId(shipmentId)
                .orderId(orderId)
                .userId(userId)
                .trackingNumber(trackingNumber)
                .startedAt(startedAt)
                .build();
    }
}
