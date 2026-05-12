package com.ezmeal.shipment.domain.event.payload;

import com.ezmeal.common.message.DomainEvent;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ShipmentDeliveredPayload(
        UUID shipmentId,
        UUID orderId,
        UUID userId,
        LocalDateTime deliveredAt
) implements DomainEvent {

    public static ShipmentDeliveredPayload of(UUID shipmentId, UUID orderId, UUID userId,
                                              LocalDateTime deliveredAt) {
        return ShipmentDeliveredPayload.builder()
                .shipmentId(shipmentId)
                .orderId(orderId)
                .userId(userId)
                .deliveredAt(deliveredAt)
                .build();
    }
}
