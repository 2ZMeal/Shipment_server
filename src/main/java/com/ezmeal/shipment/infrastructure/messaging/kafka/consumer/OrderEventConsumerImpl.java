package com.ezmeal.shipment.infrastructure.messaging.kafka.consumer;

import com.ezmeal.shipment.domain.entity.Shipment;
import com.ezmeal.shipment.domain.event.OrderEventConsumer;
import com.ezmeal.shipment.domain.repository.ShipmentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumerImpl implements OrderEventConsumer {

    private final ShipmentRepository shipmentRepository;
    private final ObjectMapper objectMapper;

    /**
     * order.delivering 수신 → Shipment 레코드 생성 (PREPARING 상태)
     * OrderDelivering payload: orderId, userId, companyId, productId, deliveryAddress, status
     */
    @Override
    @KafkaListener(topics = "order.delivering", groupId = "shipment-service")
    public void onOrderDelivering(String message) {
        try {
            JsonNode node     = objectMapper.readTree(message);
            UUID orderId   = UUID.fromString(node.get("orderId").asText());
            UUID userId    = UUID.fromString(node.get("userId").asText());
            UUID companyId = UUID.fromString(node.get("companyId").asText());

            // 멱등성: 이미 레코드가 있으면 무시
            Optional<Shipment> existing = shipmentRepository.findByOrderId(orderId);
            if (existing.isPresent()) {
                log.warn("[Kafka] order.delivering 중복 수신 무시: orderId={}", orderId);
                return;
            }

            Shipment shipment = Shipment.create(orderId, userId, companyId);
            shipmentRepository.save(shipment);
            log.info("[Kafka] Shipment 레코드 생성 완료: orderId={}", orderId);

        } catch (Exception e) {
            log.error("[Kafka] order.delivering 처리 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * order.cancelled 수신 → PREPARING 상태이면 CANCELLED 로 변경
     * OrderCancelled payload: orderId, userId, productId, status
     */
    @Override
    @KafkaListener(topics = "order.cancelled", groupId = "shipment-service")
    public void onOrderCancelled(String message) {
        try {
            JsonNode node  = objectMapper.readTree(message);
            UUID orderId = UUID.fromString(node.get("orderId").asText());

            shipmentRepository.findByOrderId(orderId).ifPresent(shipment -> {
                try {
                    shipment.cancel();
                    shipmentRepository.save(shipment);
                    log.info("[Kafka] Shipment 취소 처리 완료: orderId={}", orderId);
                } catch (IllegalStateException e) {
                    log.warn("[Kafka] Shipment 취소 불가 (이미 배송 시작): orderId={}", orderId);
                }
            });

        } catch (Exception e) {
            log.error("[Kafka] order.cancelled 처리 실패: {}", e.getMessage(), e);
        }
    }
}
