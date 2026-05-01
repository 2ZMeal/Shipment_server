package com.ezmeal.shipment.application.service;

import com.ezmeal.common.enums.Role;
import com.ezmeal.common.exception.CustomException;
import com.ezmeal.common.security.principal.CustomUserPrincipal;
import com.ezmeal.shipment.application.dto.request.ShipmentStartRequest;
import com.ezmeal.shipment.application.dto.response.ShipmentResponse;
import com.ezmeal.shipment.domain.entity.Shipment;
import com.ezmeal.shipment.domain.entity.ShipmentStatus;
import com.ezmeal.shipment.domain.event.ShipmentEventProducer;
import com.ezmeal.shipment.domain.exception.ShipmentErrorCode;
import com.ezmeal.shipment.domain.repository.ShipmentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentApplicationServiceTest {

    @Mock ShipmentRepository shipmentRepository;
    @Mock ShipmentEventProducer shipmentEventProducer;

    @InjectMocks ShipmentApplicationService shipmentApplicationService;

    private UUID orderId;
    private UUID userId;
    private UUID companyId;
    private Shipment shipment;

    @BeforeEach
    void setUp() {
        orderId   = UUID.randomUUID();
        userId    = UUID.randomUUID();
        companyId = UUID.randomUUID();
        shipment  = Shipment.create(orderId, userId, companyId);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(Role role, String principalUserId) {
        CustomUserPrincipal principal = new CustomUserPrincipal(principalUserId, role);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("ADMIN 은 배송 상태를 조회할 수 있다")
    void getShipment_admin_success() {
        mockSecurityContext(Role.ADMIN, userId.toString());
        given(shipmentRepository.findByOrderId(orderId)).willReturn(Optional.of(shipment));

        ShipmentResponse response = shipmentApplicationService.getShipment(orderId, companyId.toString());

        assertThat(response.status()).isEqualTo(ShipmentStatus.PREPARING);
    }

    @Test
    @DisplayName("배송 정보가 없으면 SHIPMENT_001 예외가 발생한다")
    void getShipment_notFound_throwsException() {
        mockSecurityContext(Role.ADMIN, userId.toString());
        given(shipmentRepository.findByOrderId(orderId)).willReturn(Optional.empty());

        assertThatThrownBy(() ->
            shipmentApplicationService.getShipment(orderId, null))
            .isInstanceOf(CustomException.class)
            .extracting(e -> ((CustomException) e).getErrorCode())
            .isEqualTo(ShipmentErrorCode.SHIPMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("COMPANY 가 배송 시작 처리 시 IN_DELIVERY 상태로 변경된다")
    void startShipment_company_success() {
        mockSecurityContext(Role.COMPANY, userId.toString());
        given(shipmentRepository.findByOrderId(orderId)).willReturn(Optional.of(shipment));
        given(shipmentRepository.save(any())).willReturn(shipment);

        ShipmentStartRequest request = new ShipmentStartRequest("TRACK123", LocalDateTime.now());
        ShipmentResponse response = shipmentApplicationService.startShipment(
            orderId, request, companyId.toString());

        assertThat(response.status()).isEqualTo(ShipmentStatus.IN_DELIVERY);
        then(shipmentEventProducer).should().publishShipmentStarted(shipment);
    }

    @Test
    @DisplayName("배송 시작 시 송장번호가 없으면 SHIPMENT_003 예외가 발생한다")
    void startShipment_noTrackingNumber_throwsException() {
        mockSecurityContext(Role.COMPANY, userId.toString());
        given(shipmentRepository.findByOrderId(orderId)).willReturn(Optional.of(shipment));

        ShipmentStartRequest request = new ShipmentStartRequest(null, null);

        assertThatThrownBy(() ->
            shipmentApplicationService.startShipment(orderId, request, companyId.toString()))
            .isInstanceOf(CustomException.class)
            .extracting(e -> ((CustomException) e).getErrorCode())
            .isEqualTo(ShipmentErrorCode.TRACKING_NUMBER_REQUIRED);
    }

    @Test
    @DisplayName("COMPANY 가 배송 완료 처리 시 DELIVERED 상태로 변경된다")
    void deliverShipment_company_success() {
        mockSecurityContext(Role.COMPANY, userId.toString());
        shipment.start("TRACK123", LocalDateTime.now());
        given(shipmentRepository.findByOrderId(orderId)).willReturn(Optional.of(shipment));
        given(shipmentRepository.save(any())).willReturn(shipment);

        ShipmentResponse response = shipmentApplicationService.deliverShipment(
            orderId, companyId.toString());

        assertThat(response.status()).isEqualTo(ShipmentStatus.DELIVERED);
        then(shipmentEventProducer).should().publishShipmentDelivered(shipment);
    }

    @Test
    @DisplayName("COMPANY 가 배송 취소 처리 시 CANCELLED 상태로 변경된다")
    void cancelShipment_company_success() {
        mockSecurityContext(Role.COMPANY, userId.toString());
        given(shipmentRepository.findByOrderId(orderId)).willReturn(Optional.of(shipment));
        given(shipmentRepository.save(any())).willReturn(shipment);

        ShipmentResponse response = shipmentApplicationService.cancelShipment(
            orderId, companyId.toString());

        assertThat(response.status()).isEqualTo(ShipmentStatus.CANCELLED);
    }

    @Test
    @DisplayName("USER 가 타인의 배송을 조회하면 SHIPMENT_403 예외가 발생한다")
    void getShipment_user_otherOrder_throwsException() {
        String otherUserId = UUID.randomUUID().toString();
        mockSecurityContext(Role.USER, otherUserId);
        given(shipmentRepository.findByOrderId(orderId)).willReturn(Optional.of(shipment));

        assertThatThrownBy(() ->
            shipmentApplicationService.getShipment(orderId, null))
            .isInstanceOf(CustomException.class)
            .extracting(e -> ((CustomException) e).getErrorCode())
            .isEqualTo(ShipmentErrorCode.ACCESS_DENIED);
    }
}
