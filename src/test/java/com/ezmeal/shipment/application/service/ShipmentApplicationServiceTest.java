package com.ezmeal.shipment.application.service;

import com.ezmeal.shipment.application.dto.request.ShipmentStartRequest;
import com.ezmeal.shipment.application.dto.response.ShipmentResponse;
import com.ezmeal.shipment.domain.entity.Shipment;
import com.ezmeal.shipment.domain.entity.ShipmentStatus;
import com.ezmeal.shipment.domain.event.ShipmentEventProducer;
import com.ezmeal.shipment.domain.exception.ShipmentErrorCode;
import com.ezmeal.shipment.domain.exception.ShipmentException;
import com.ezmeal.shipment.domain.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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

    @Test
    @DisplayName("MASTER 는 배송 상태를 조회할 수 있다")
    void getShipment_master_success() {
        given(shipmentRepository.findByOrderId(orderId)).willReturn(Optional.of(shipment));

        ShipmentResponse response = shipmentApplicationService.getShipment(
            orderId, "MASTER", userId.toString(), companyId.toString());

        assertThat(response.status()).isEqualTo(ShipmentStatus.PREPARING);
    }

    @Test
    @DisplayName("배송 정보가 없으면 SHIPMENT_001 예외가 발생한다")
    void getShipment_notFound_throwsException() {
        given(shipmentRepository.findByOrderId(orderId)).willReturn(Optional.empty());

        assertThatThrownBy(() ->
            shipmentApplicationService.getShipment(orderId, "MASTER", null, null))
            .isInstanceOf(ShipmentException.class)
            .extracting(e -> ((ShipmentException) e).getErrorCode())
            .isEqualTo(ShipmentErrorCode.SHIPMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("VENDOR 가 배송 시작 처리 시 IN_DELIVERY 상태로 변경된다")
    void startShipment_vendor_success() {
        given(shipmentRepository.findByOrderId(orderId)).willReturn(Optional.of(shipment));
        given(shipmentRepository.save(any())).willReturn(shipment);

        ShipmentStartRequest request = new ShipmentStartRequest("TRACK123", LocalDateTime.now());
        ShipmentResponse response = shipmentApplicationService.startShipment(
            orderId, request, "VENDOR", companyId.toString());

        assertThat(response.status()).isEqualTo(ShipmentStatus.IN_DELIVERY);
        then(shipmentEventProducer).should().publishShipmentStarted(shipment);
    }

    @Test
    @DisplayName("배송 시작 시 송장번호가 없으면 SHIPMENT_003 예외가 발생한다")
    void startShipment_noTrackingNumber_throwsException() {
        given(shipmentRepository.findByOrderId(orderId)).willReturn(Optional.of(shipment));

        ShipmentStartRequest request = new ShipmentStartRequest(null, null);

        assertThatThrownBy(() ->
            shipmentApplicationService.startShipment(orderId, request, "VENDOR", companyId.toString()))
            .isInstanceOf(ShipmentException.class)
            .extracting(e -> ((ShipmentException) e).getErrorCode())
            .isEqualTo(ShipmentErrorCode.TRACKING_NUMBER_REQUIRED);
    }

    @Test
    @DisplayName("VENDOR 가 배송 완료 처리 시 DELIVERED 상태로 변경된다")
    void deliverShipment_vendor_success() {
        shipment.start("TRACK123", LocalDateTime.now());
        given(shipmentRepository.findByOrderId(orderId)).willReturn(Optional.of(shipment));
        given(shipmentRepository.save(any())).willReturn(shipment);

        ShipmentResponse response = shipmentApplicationService.deliverShipment(
            orderId, "VENDOR", companyId.toString());

        assertThat(response.status()).isEqualTo(ShipmentStatus.DELIVERED);
        then(shipmentEventProducer).should().publishShipmentDelivered(shipment);
    }

    @Test
    @DisplayName("VENDOR 가 배송 취소 처리 시 CANCELLED 상태로 변경된다")
    void cancelShipment_vendor_success() {
        given(shipmentRepository.findByOrderId(orderId)).willReturn(Optional.of(shipment));
        given(shipmentRepository.save(any())).willReturn(shipment);

        ShipmentResponse response = shipmentApplicationService.cancelShipment(
            orderId, "VENDOR", companyId.toString());

        assertThat(response.status()).isEqualTo(ShipmentStatus.CANCELLED);
    }

    @Test
    @DisplayName("USER 가 타인의 배송을 조회하면 SHIPMENT_403 예외가 발생한다")
    void getShipment_user_otherOrder_throwsException() {
        given(shipmentRepository.findByOrderId(orderId)).willReturn(Optional.of(shipment));

        String otherUserId = UUID.randomUUID().toString();
        assertThatThrownBy(() ->
            shipmentApplicationService.getShipment(orderId, "USER", otherUserId, null))
            .isInstanceOf(ShipmentException.class)
            .extracting(e -> ((ShipmentException) e).getErrorCode())
            .isEqualTo(ShipmentErrorCode.ACCESS_DENIED);
    }
}
