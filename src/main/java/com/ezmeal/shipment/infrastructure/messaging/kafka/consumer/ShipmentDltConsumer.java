package com.ezmeal.shipment.infrastructure.messaging.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ShipmentDltConsumer {

    @KafkaListener(
            topics = {
                    "order.shipment.requested.DLT",
                    "order.cancelled.DLT"
            },
            groupId = "${spring.kafka.consumer.group-id}-dlt",
            containerFactory = "kafkaDltListenerContainerFactory"
    )
    public void handleDlt(ConsumerRecord<String, String> record) {
        log.error("[DLT] 메시지 소비 최종 실패 | topic={}, offset={}, payload={}",
                record.topic(), record.offset(), record.value());
    }
}
