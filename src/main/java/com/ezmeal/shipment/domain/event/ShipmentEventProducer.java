package com.ezmeal.shipment.domain.event;

import com.ezmeal.shipment.domain.entity.Shipment;

public interface ShipmentEventProducer {
    void publishShipmentStarted(Shipment shipment);
    void publishShipmentDelivered(Shipment shipment);
}
