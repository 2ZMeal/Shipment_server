package com.ezmeal.shipment.infrastructure.messaging.kafka.producer;

import com.ezmeal.common.message.CommonKafkaEventPublisher;
import com.ezmeal.shipment.domain.entity.Shipment;
import com.ezmeal.shipment.domain.event.ShipmentEventProducer;
import com.ezmeal.shipment.domain.event.payload.ShipmentDeliveredPayload;
import com.ezmeal.shipment.domain.event.payload.ShipmentStartedPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShipmentEventProducerImpl implements ShipmentEventProducer {

    private static final String TOPIC_STARTED   = "shipment.started";
    private static final String TOPIC_DELIVERED = "shipment.delivered";

    private final CommonKafkaEventPublisher commonPublisher;

    @Override
    public void publishShipmentStarted(Shipment shipment) {
        commonPublisher.publish(
                TOPIC_STARTED,
                shipment.getOrderId().toString(),
                "SHIPMENT_STARTED",
                ShipmentStartedPayload.of(
                        shipment.getId(),
                        shipment.getOrderId(),
                        shipment.getUserId(),
                        shipment.getTrackingNumber(),
                        shipment.getStartedAt()
                )
        );
    }

    @Override
    public void publishShipmentDelivered(Shipment shipment) {
        commonPublisher.publish(
                TOPIC_DELIVERED,
                shipment.getOrderId().toString(),
                "SHIPMENT_DELIVERED",
                ShipmentDeliveredPayload.of(
                        shipment.getId(),
                        shipment.getOrderId(),
                        shipment.getUserId(),
                        shipment.getDeliveredAt()
                )
        );
    }
}
