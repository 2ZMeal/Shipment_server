package com.ezmeal.shipment.domain.event;

import com.ezmeal.common.message.EventEnvelope;
import com.ezmeal.shipment.domain.event.payload.OrderCancelledPayload;
import com.ezmeal.shipment.domain.event.payload.ShipmentRequestedPayload;

public interface OrderEventConsumer {
    void onShipmentRequested(EventEnvelope<ShipmentRequestedPayload> envelope);
    void onOrderCancelled(EventEnvelope<OrderCancelledPayload> envelope);
}
