package com.ezmeal.shipment.infrastructure.messaging.kafka.consumer;

import com.ezmeal.common.message.EventEnvelope;
import com.ezmeal.common.message.inbox.InboxProcessor;
import com.ezmeal.shipment.domain.entity.Shipment;
import com.ezmeal.shipment.domain.event.OrderEventConsumer;
import com.ezmeal.shipment.domain.event.payload.OrderCancelledPayload;
import com.ezmeal.shipment.domain.event.payload.ShipmentRequestedPayload;
import com.ezmeal.shipment.domain.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumerImpl implements OrderEventConsumer {

    private final InboxProcessor inboxProcessor;
    private final ShipmentRepository shipmentRepository;

    /**
     * shipment.requested 수신 → Shipment 레코드 생성 (PREPARING 상태)
     */
    @KafkaListener(topics = "order.shipment.requested", groupId = "${spring.kafka.consumer.group-id}")
    public void onShipmentRequested(EventEnvelope<ShipmentRequestedPayload> envelope) {
        inboxProcessor.processOnce(envelope.eventId(), () -> {
            ShipmentRequestedPayload payload = envelope.payload();
            Shipment shipment = Shipment.create(payload.orderId(), payload.userId(), payload.companyId());
            shipmentRepository.save(shipment);
            log.info("[Kafka] Shipment 레코드 생성 완료: orderId={}", payload.orderId());
        });
    }

    /**
     * order.cancelled 수신 → PREPARING 상태이면 CANCELLED 로 변경
     */
    @KafkaListener(topics = "order.cancelled", groupId = "${spring.kafka.consumer.group-id}")
    public void onOrderCancelled(EventEnvelope<OrderCancelledPayload> envelope) {
        inboxProcessor.processOnce(envelope.eventId(), () -> {
            OrderCancelledPayload payload = envelope.payload();
            shipmentRepository.findByOrderId(payload.orderId()).ifPresent(shipment -> {
                try {
                    shipment.cancel();
                    shipmentRepository.save(shipment);
                    log.info("[Kafka] Shipment 취소 처리 완료: orderId={}", payload.orderId());
                } catch (IllegalStateException e) {
                    log.warn("[Kafka] Shipment 취소 불가 (이미 배송 시작): orderId={}", payload.orderId());
                }
            });
        });
    }
}
