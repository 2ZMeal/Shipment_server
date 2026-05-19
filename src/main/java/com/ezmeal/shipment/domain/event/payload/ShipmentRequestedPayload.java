package com.ezmeal.shipment.domain.event.payload;

import com.ezmeal.common.message.DomainEvent;

import java.util.UUID;

// shipment.requested 토픽 수신용 DTO (Order_server 발행 구조에 맞춤)
public record ShipmentRequestedPayload(
        UUID orderId,
        UUID userId,
        UUID companyId,
        String deliveryAddress,
        String requestNote
) implements DomainEvent {}
