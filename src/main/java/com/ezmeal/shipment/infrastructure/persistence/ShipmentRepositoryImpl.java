package com.ezmeal.shipment.infrastructure.persistence;

import com.ezmeal.shipment.domain.entity.Shipment;
import com.ezmeal.shipment.domain.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ShipmentRepositoryImpl implements ShipmentRepository {

    private final JpaShipmentRepository jpaShipmentRepository;

    @Override
    public Shipment save(Shipment shipment) {
        return jpaShipmentRepository.save(shipment);
    }

    @Override
    public Optional<Shipment> findById(UUID shipmentId) {
        return jpaShipmentRepository.findById(shipmentId);
    }

    @Override
    public Optional<Shipment> findByOrderId(UUID orderId) {
        return jpaShipmentRepository.findByOrderId(orderId);
    }

    @Override
    public List<Shipment> findAllByOrderId(UUID orderId) {
        return jpaShipmentRepository.findAllByOrderId(orderId);
    }
}
