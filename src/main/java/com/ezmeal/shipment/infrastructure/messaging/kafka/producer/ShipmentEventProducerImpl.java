package com.ezmeal.shipment.infrastructure.messaging.kafka.producer;

import com.ezmeal.shipment.domain.entity.Shipment;
import com.ezmeal.shipment.domain.event.ShipmentEventProducer;
import com.ezmeal.shipment.domain.event.payload.ShipmentDeliveredPayload;
import com.ezmeal.shipment.domain.event.payload.ShipmentStartedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentEventProducerImpl implements ShipmentEventProducer {

    private static final String TOPIC_STARTED   = "shipment.started";
    private static final String TOPIC_DELIVERED = "shipment.delivered";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishShipmentStarted(Shipment shipment) {
        ShipmentStartedPayload payload = ShipmentStartedPayload.of(
                shipment.getId(),
                shipment.getOrderId(),
                shipment.getUserId(),
                shipment.getTrackingNumber(),
                shipment.getStartedAt()
        );
        kafkaTemplate.send(TOPIC_STARTED, shipment.getOrderId().toString(), payload);
        log.info("[Kafka] shipment.started 발행 완료: orderId={}", shipment.getOrderId());
    }

    @Override
    public void publishShipmentDelivered(Shipment shipment) {
        ShipmentDeliveredPayload payload = ShipmentDeliveredPayload.of(
                shipment.getId(),
                shipment.getOrderId(),
                shipment.getUserId(),
                shipment.getDeliveredAt()
        );
        kafkaTemplate.send(TOPIC_DELIVERED, shipment.getOrderId().toString(), payload);
        log.info("[Kafka] shipment.delivered 발행 완료: orderId={}", shipment.getOrderId());
    }
}
