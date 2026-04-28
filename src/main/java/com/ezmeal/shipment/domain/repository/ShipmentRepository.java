package com.ezmeal.shipment.domain.repository;

import com.ezmeal.shipment.domain.entity.Shipment;

import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository {
    Shipment save(Shipment shipment);
    Optional<Shipment> findByOrderId(UUID orderId);
}
