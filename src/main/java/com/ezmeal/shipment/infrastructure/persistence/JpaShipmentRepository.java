package com.ezmeal.shipment.infrastructure.persistence;

import com.ezmeal.shipment.domain.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaShipmentRepository extends JpaRepository<Shipment, UUID> {
    Optional<Shipment> findByOrderId(UUID orderId);
    List<Shipment> findAllByOrderId(UUID orderId);
}
