package com.ezmeal.shipment.application.service;

import com.ezmeal.common.exception.types.BadRequestException;
import com.ezmeal.common.exception.types.NotFoundException;
import com.ezmeal.common.security.principal.CustomUserPrincipal;
import com.ezmeal.shipment.application.dto.request.ShipmentStartRequest;
import com.ezmeal.shipment.application.dto.response.ShipmentResponse;
import com.ezmeal.shipment.domain.entity.Shipment;
import com.ezmeal.shipment.domain.event.ShipmentEventProducer;
import com.ezmeal.shipment.domain.exception.ShipmentErrorCode;
import com.ezmeal.shipment.domain.repository.ShipmentRepository;
import com.ezmeal.shipment.infrastructure.security.UserRoleCheck;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShipmentApplicationService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentEventProducer shipmentEventProducer;

    @Transactional(readOnly = true)
    public ShipmentResponse getShipment(UUID orderId, String requestCompanyId) {
        CustomUserPrincipal principal = getCurrentUser();
        Shipment shipment = findByOrderIdOrThrow(orderId);
        UserRoleCheck.checkReadPermission(principal.getRole(), principal.getUserId(), requestCompanyId, shipment);
        return ShipmentResponse.from(shipment);
    }

    @Transactional
    public ShipmentResponse startShipment(UUID orderId, ShipmentStartRequest request,
                                          String requestCompanyId) {
        CustomUserPrincipal principal = getCurrentUser();
        Shipment shipment = findByOrderIdOrThrow(orderId);
        UserRoleCheck.checkVendorOrMasterPermission(principal.getRole(), requestCompanyId, shipment);

        if (request.trackingNumber() == null || request.trackingNumber().isBlank()) {
            throw new BadRequestException(ShipmentErrorCode.TRACKING_NUMBER_REQUIRED);
        }

        try {
            shipment.start(request.trackingNumber(), request.startedAt());
        } catch (IllegalStateException e) {
            throw new BadRequestException(ShipmentErrorCode.NOT_PREPARING_STATUS);
        }

        shipmentRepository.save(shipment);
        shipmentEventProducer.publishShipmentStarted(shipment);
        return ShipmentResponse.from(shipment);
    }

    @Transactional
    public ShipmentResponse deliverShipment(UUID orderId, String requestCompanyId) {
        CustomUserPrincipal principal = getCurrentUser();
        Shipment shipment = findByOrderIdOrThrow(orderId);
        UserRoleCheck.checkVendorOrMasterPermission(principal.getRole(), requestCompanyId, shipment);

        try {
            shipment.deliver();
        } catch (IllegalStateException e) {
            throw new BadRequestException(ShipmentErrorCode.NOT_IN_DELIVERY_STATUS);
        }

        shipmentRepository.save(shipment);
        shipmentEventProducer.publishShipmentDelivered(shipment);
        return ShipmentResponse.from(shipment);
    }

    @Transactional
    public ShipmentResponse cancelShipment(UUID orderId, String requestCompanyId) {
        CustomUserPrincipal principal = getCurrentUser();
        Shipment shipment = findByOrderIdOrThrow(orderId);
        UserRoleCheck.checkVendorOrMasterPermission(principal.getRole(), requestCompanyId, shipment);

        try {
            shipment.cancel();
        } catch (IllegalStateException e) {
            throw new BadRequestException(ShipmentErrorCode.CANCEL_NOT_ALLOWED);
        }

        shipmentRepository.save(shipment);
        return ShipmentResponse.from(shipment);
    }

    private CustomUserPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (CustomUserPrincipal) auth.getPrincipal();
    }

    private Shipment findByOrderIdOrThrow(UUID orderId) {
        return shipmentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new NotFoundException(ShipmentErrorCode.SHIPMENT_NOT_FOUND));
    }
}
