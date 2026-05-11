package com.ezmeal.shipment.domain.event.payload;

import java.util.UUID;

// shipment.requested 토픽 수신용 DTO (Order_server 발행 구조에 맞춤)
// DomainEvent 미구현 - 수신 측 역직렬화 전용
public record ShipmentRequestedPayload(
        UUID orderId,
        UUID userId,
        UUID companyId,
        String deliveryAddress,
        String requestNote
) {}
