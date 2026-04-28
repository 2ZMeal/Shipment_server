package com.ezmeal.shipment.infrastructure.security;

import com.ezmeal.shipment.domain.entity.Shipment;
import com.ezmeal.shipment.domain.exception.ShipmentErrorCode;
import com.ezmeal.shipment.domain.exception.ShipmentException;

import java.util.UUID;

/**
 * API Gateway 가 전달하는 헤더를 기반으로 권한을 검증한다.
 * X-User-Id    : 요청자 UUID
 * X-User-Role  : USER | VENDOR | MASTER
 * X-Company-Id : (VENDOR 전용) 업체 UUID
 *
 * 추후 common module 의 SecurityContextHolder 방식으로 리팩토링 예정
 */
public class UserRoleCheck {

    private UserRoleCheck() {}

    /**
     * 조회 권한: USER는 자신의 주문만, VENDOR는 자신의 업체 주문만, MASTER는 전체 허용
     */
    public static void checkReadPermission(String role, String requestUserId,
                                           String requestCompanyId, Shipment shipment) {
        if ("MASTER".equals(role)) return;

        if ("USER".equals(role)) {
            UUID userId = parseUUID(requestUserId);
            if (!shipment.getUserId().equals(userId)) {
                throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
            }
            return;
        }

        if ("VENDOR".equals(role)) {
            UUID companyId = parseUUID(requestCompanyId);
            if (!shipment.getCompanyId().equals(companyId)) {
                throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
            }
            return;
        }

        throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
    }

    /**
     * 배송 변경 권한: VENDOR(본인 업체 주문만) 또는 MASTER 만 가능
     */
    public static void checkVendorOrMasterPermission(String role, String requestCompanyId,
                                                     Shipment shipment) {
        if ("MASTER".equals(role)) return;

        if ("VENDOR".equals(role)) {
            UUID companyId = parseUUID(requestCompanyId);
            if (!shipment.getCompanyId().equals(companyId)) {
                throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
            }
            return;
        }

        throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
    }

    private static UUID parseUUID(String value) {
        if (value == null || value.isBlank()) {
            throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new ShipmentException(ShipmentErrorCode.ACCESS_DENIED);
        }
    }
}
