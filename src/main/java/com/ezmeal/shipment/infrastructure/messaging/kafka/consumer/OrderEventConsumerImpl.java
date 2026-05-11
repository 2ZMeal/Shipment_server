package com.ezmeal.shipment.infrastructure.messaging.kafka.consumer;

import com.ezmeal.common.message.inbox.InboxProcessor;
import com.ezmeal.shipment.domain.entity.Shipment;
import com.ezmeal.shipment.domain.event.OrderEventConsumer;
import com.ezmeal.shipment.domain.event.payload.OrderCancelledPayload;
import com.ezmeal.shipment.domain.event.payload.ShipmentRequestedPayload;
import com.ezmeal.shipment.domain.repository.ShipmentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumerImpl implements OrderEventConsumer {

    private final InboxProcessor inboxProcessor;
    private final ShipmentRepository shipmentRepository;
    private final ObjectMapper objectMapper;

    /**
     * shipment.requested 수신 → Shipment 레코드 생성 (PREPARING 상태)
     * EventEnvelope 구조: { eventId, eventType, aggregateId, occurredAt, payload: { orderId, userId, companyId, ... } }
     */
    @Override
    @KafkaListener(topics = "shipment.requested", groupId = "${spring.kafka.consumer.group-id}")
    public void onShipmentRequested(String message) {
        try {
            JsonNode envelope = objectMapper.readTree(message);
            String eventId   = envelope.get("eventId").asText();
            JsonNode payload = envelope.get("payload");

            ShipmentRequestedPayload dto = objectMapper.treeToValue(payload, ShipmentRequestedPayload.class);

            inboxProcessor.processOnce(eventId, () -> {
                Shipment shipment = Shipment.create(dto.orderId(), dto.userId(), dto.companyId());
                shipmentRepository.save(shipment);
                log.info("[Kafka] Shipment 레코드 생성 완료: orderId={}", dto.orderId());
            });

        } catch (Exception e) {
            log.error("[Kafka] shipment.requested 처리 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * order.cancelled 수신 → PREPARING 상태이면 CANCELLED 로 변경
     */
    @Override
    @KafkaListener(topics = "order.cancelled", groupId = "${spring.kafka.consumer.group-id}")
    public void onOrderCancelled(String message) {
        try {
            JsonNode envelope = objectMapper.readTree(message);
            String eventId   = envelope.get("eventId").asText();
            JsonNode payload = envelope.get("payload");

            OrderCancelledPayload dto = objectMapper.treeToValue(payload, OrderCancelledPayload.class);

            inboxProcessor.processOnce(eventId, () -> {
                shipmentRepository.findByOrderId(dto.orderId()).ifPresent(shipment -> {
                    try {
                        shipment.cancel();
                        shipmentRepository.save(shipment);
                        log.info("[Kafka] Shipment 취소 처리 완료: orderId={}", dto.orderId());
                    } catch (IllegalStateException e) {
                        log.warn("[Kafka] Shipment 취소 불가 (이미 배송 시작): orderId={}", dto.orderId());
                    }
                });
            });

        } catch (Exception e) {
            log.error("[Kafka] order.cancelled 처리 실패: {}", e.getMessage(), e);
        }
    }
}
