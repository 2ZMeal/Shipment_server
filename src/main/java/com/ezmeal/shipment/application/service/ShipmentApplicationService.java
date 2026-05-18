package com.ezmeal.shipment.application.service;

import com.ezmeal.common.enums.Role;
import com.ezmeal.common.exception.types.BadRequestException;
import com.ezmeal.common.exception.types.ForbiddenException;
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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShipmentApplicationService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentEventProducer shipmentEventProducer;

    // orderId 기준 전체 조회 - 업체별로 Shipment가 N개일 수 있음
    @Transactional(readOnly = true)
    public List<ShipmentResponse> getShipmentsByOrderId(UUID orderId, String requestCompanyId) {
        CustomUserPrincipal principal = getCurrentUser();
        List<Shipment> shipments = shipmentRepository.findAllByOrderId(orderId);
        if (shipments.isEmpty()) {
            throw new NotFoundException(ShipmentErrorCode.SHIPMENT_NOT_FOUND);
        }

        Role role = principal.getRole();
        if (role == Role.ADMIN) {
            return shipments.stream().map(ShipmentResponse::from).toList();
        }
        if (role == Role.USER) {
            UserRoleCheck.checkReadPermission(role, principal.getUserId(), requestCompanyId, shipments.get(0));
            return shipments.stream().map(ShipmentResponse::from).toList();
        }
        if (role == Role.COMPANY) {
            UUID companyId = UUID.fromString(requestCompanyId);
            return shipments.stream()
                    .filter(s -> s.getCompanyId().equals(companyId))
                    .map(ShipmentResponse::from)
                    .toList();
        }
        throw new ForbiddenException(ShipmentErrorCode.ACCESS_DENIED);
    }

    @Transactional
    public ShipmentResponse startShipment(UUID shipmentId, ShipmentStartRequest request,
                                          String requestCompanyId) {
        CustomUserPrincipal principal = getCurrentUser();
        Shipment shipment = findByShipmentIdOrThrow(shipmentId);
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
    public ShipmentResponse deliverShipment(UUID shipmentId, String requestCompanyId) {
        CustomUserPrincipal principal = getCurrentUser();
        Shipment shipment = findByShipmentIdOrThrow(shipmentId);
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
    public ShipmentResponse cancelShipment(UUID shipmentId, String requestCompanyId) {
        CustomUserPrincipal principal = getCurrentUser();
        Shipment shipment = findByShipmentIdOrThrow(shipmentId);
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
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserPrincipal principal)) {
            throw new ForbiddenException(ShipmentErrorCode.ACCESS_DENIED);
        }
        return principal;
    }

    private Shipment findByShipmentIdOrThrow(UUID shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new NotFoundException(ShipmentErrorCode.SHIPMENT_NOT_FOUND));
    }
}
