package com.ezmeal.shipment.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_shipment")
@Getter
@NoArgsConstructor
public class Shipment {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "order_id", nullable = false, unique = true, columnDefinition = "uuid")
    private UUID orderId;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "company_id", nullable = false, columnDefinition = "uuid")
    private UUID companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    // Audit
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    // 팩토리 메서드 — Kafka order.delivering 이벤트로 배송 레코드 생성 시 사용
    public static Shipment create(UUID orderId, UUID userId, UUID companyId) {
        Shipment s = new Shipment();
        s.orderId   = orderId;
        s.userId    = userId;
        s.companyId = companyId;
        s.status    = ShipmentStatus.PREPARING;
        s.createdAt = LocalDateTime.now();
        s.createdBy = "SYSTEM";
        return s;
    }

    // 배송 시작: PREPARING → IN_DELIVERY
    public void start(String trackingNumber, LocalDateTime startedAt) {
        if (this.status != ShipmentStatus.PREPARING) {
            throw new IllegalStateException("PREPARING 상태가 아닙니다. 현재 상태: " + this.status);
        }
        this.trackingNumber = trackingNumber;
        this.startedAt      = (startedAt != null) ? startedAt : LocalDateTime.now();
        this.status         = ShipmentStatus.IN_DELIVERY;
        this.updatedAt      = LocalDateTime.now();
    }

    // 배송 완료: IN_DELIVERY → DELIVERED
    public void deliver() {
        if (this.status != ShipmentStatus.IN_DELIVERY) {
            throw new IllegalStateException("IN_DELIVERY 상태가 아닙니다. 현재 상태: " + this.status);
        }
        this.deliveredAt = LocalDateTime.now();
        this.status      = ShipmentStatus.DELIVERED;
        this.updatedAt   = LocalDateTime.now();
    }

    // 배송 취소: PREPARING → CANCELLED (IN_DELIVERY 이후는 불가)
    public void cancel() {
        if (this.status == ShipmentStatus.IN_DELIVERY
                || this.status == ShipmentStatus.DELIVERED) {
            throw new IllegalStateException("배송 시작 이후에는 취소할 수 없습니다. 현재 상태: " + this.status);
        }
        this.canceledAt = LocalDateTime.now();
        this.status     = ShipmentStatus.CANCELLED;
        this.updatedAt  = LocalDateTime.now();
    }
}
