package com.ezmeal.shipment.domain.event;

public interface OrderEventConsumer {
    void onShipmentRequested(String message);
    void onOrderCancelled(String message);
}
