package com.ezmeal.shipment.domain.event.payload;

import com.ezmeal.common.message.DomainEvent;

import java.util.UUID;

// order.cancelled 토픽 수신용 DTO (Order_server 발행 구조에 맞춤)
public record OrderCancelledPayload(
        UUID orderId,
        UUID userId,
        UUID companyId
) implements DomainEvent {}
