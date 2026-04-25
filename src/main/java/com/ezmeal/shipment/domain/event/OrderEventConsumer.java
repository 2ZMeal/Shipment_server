package com.ezmeal.shipment.domain.event;

public interface OrderEventConsumer {
    void onOrderDelivering(String message);
    void onOrderCancelled(String message);
}
