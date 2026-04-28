package com.ezmeal.shipment.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ShipmentTest {

    private static final UUID ORDER_ID   = UUID.randomUUID();
    private static final UUID USER_ID    = UUID.randomUUID();
    private static final UUID COMPANY_ID = UUID.randomUUID();

    @Test
    @DisplayName("배송 레코드를 생성하면 PREPARING 상태이다")
    void create_returnsPreparingStatus() {
        Shipment shipment = Shipment.create(ORDER_ID, USER_ID, COMPANY_ID);
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.PREPARING);
        assertThat(shipment.getUserId()).isEqualTo(USER_ID);
        assertThat(shipment.getCompanyId()).isEqualTo(COMPANY_ID);
    }

    @Test
    @DisplayName("PREPARING 상태에서 start() 호출 시 IN_DELIVERY 로 전이된다")
    void start_fromPreparing_transitionsToInDelivery() {
        Shipment shipment = Shipment.create(ORDER_ID, USER_ID, COMPANY_ID);
        shipment.start("TRACK123", LocalDateTime.now());
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.IN_DELIVERY);
        assertThat(shipment.getTrackingNumber()).isEqualTo("TRACK123");
    }

    @Test
    @DisplayName("PREPARING 이 아닌 상태에서 start() 호출 시 예외가 발생한다")
    void start_notPreparing_throwsException() {
        Shipment shipment = Shipment.create(ORDER_ID, USER_ID, COMPANY_ID);
        shipment.start("TRACK123", LocalDateTime.now());
        assertThatThrownBy(() -> shipment.start("TRACK456", LocalDateTime.now()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("IN_DELIVERY 상태에서 deliver() 호출 시 DELIVERED 로 전이된다")
    void deliver_fromInDelivery_transitionsToDelivered() {
        Shipment shipment = Shipment.create(ORDER_ID, USER_ID, COMPANY_ID);
        shipment.start("TRACK123", LocalDateTime.now());
        shipment.deliver();
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
        assertThat(shipment.getDeliveredAt()).isNotNull();
    }

    @Test
    @DisplayName("IN_DELIVERY 가 아닌 상태에서 deliver() 호출 시 예외가 발생한다")
    void deliver_notInDelivery_throwsException() {
        Shipment shipment = Shipment.create(ORDER_ID, USER_ID, COMPANY_ID);
        assertThatThrownBy(shipment::deliver)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("PREPARING 상태에서 cancel() 호출 시 CANCELLED 로 전이된다")
    void cancel_fromPreparing_transitionsToCancelled() {
        Shipment shipment = Shipment.create(ORDER_ID, USER_ID, COMPANY_ID);
        shipment.cancel();
        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.CANCELLED);
        assertThat(shipment.getCanceledAt()).isNotNull();
    }

    @Test
    @DisplayName("IN_DELIVERY 이후 상태에서 cancel() 호출 시 예외가 발생한다")
    void cancel_afterInDelivery_throwsException() {
        Shipment shipment = Shipment.create(ORDER_ID, USER_ID, COMPANY_ID);
        shipment.start("TRACK123", LocalDateTime.now());
        assertThatThrownBy(shipment::cancel)
                .isInstanceOf(IllegalStateException.class);
    }
}
