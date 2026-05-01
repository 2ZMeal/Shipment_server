package com.ezmeal.shipment.infrastructure.messaging.kafka.producer;

import com.ezmeal.common.security.principal.CustomUserPrincipal;
import com.ezmeal.shipment.domain.entity.Shipment;
import com.ezmeal.shipment.domain.event.ShipmentEventProducer;
import com.ezmeal.shipment.domain.event.payload.ShipmentDeliveredPayload;
import com.ezmeal.shipment.domain.event.payload.ShipmentStartedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

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
        ProducerRecord<String, Object> record = new ProducerRecord<>(
                TOPIC_STARTED, shipment.getOrderId().toString(), payload
        );
        addSecurityHeaders(record);
        kafkaTemplate.send(record);
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
        ProducerRecord<String, Object> record = new ProducerRecord<>(
                TOPIC_DELIVERED, shipment.getOrderId().toString(), payload
        );
        addSecurityHeaders(record);
        kafkaTemplate.send(record);
        log.info("[Kafka] shipment.delivered 발행 완료: orderId={}", shipment.getOrderId());
    }

    private void addSecurityHeaders(ProducerRecord<String, Object> record) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserPrincipal principal) {
            record.headers().add("X-User-Id",   principal.getUserId().getBytes(StandardCharsets.UTF_8));
            record.headers().add("X-User-Role", principal.getRole().name().getBytes(StandardCharsets.UTF_8));
        }
    }
}
