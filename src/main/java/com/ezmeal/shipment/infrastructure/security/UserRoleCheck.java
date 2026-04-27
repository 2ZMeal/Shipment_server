package com.ezmeal.shipment.infrastructure.security;

import com.ezmeal.common.enums.Role;
import com.ezmeal.common.exception.types.ForbiddenException;
import com.ezmeal.shipment.domain.entity.Shipment;
import com.ezmeal.shipment.domain.exception.ShipmentErrorCode;

import java.util.UUID;

/**
 * SecurityContext의 Role 기반으로 권한을 검증한다.
 * Role.ADMIN  : 전체 접근 허용 (구 MASTER)
 * Role.COMPANY: 소속 업체 주문만 접근 (구 VENDOR)
 * Role.USER   : 본인 주문만 접근
 */
public class UserRoleCheck {

    private UserRoleCheck() {}

    /**
     * 조회 권한: USER는 자신의 주문만, COMPANY는 자신의 업체 주문만, ADMIN은 전체 허용
     */
    public static void checkReadPermission(Role role, String requestUserId,
                                           String requestCompanyId, Shipment shipment) {
        if (role == Role.ADMIN) return;

        if (role == Role.USER) {
            UUID userId = parseUUID(requestUserId);
            if (!shipment.getUserId().equals(userId)) {
                throw new ForbiddenException(ShipmentErrorCode.ACCESS_DENIED);
            }
            return;
        }

        if (role == Role.COMPANY) {
            UUID companyId = parseUUID(requestCompanyId);
            if (!shipment.getCompanyId().equals(companyId)) {
                throw new ForbiddenException(ShipmentErrorCode.ACCESS_DENIED);
            }
            return;
        }

        throw new ForbiddenException(ShipmentErrorCode.ACCESS_DENIED);
    }

    /**
     * 배송 변경 권한: COMPANY(본인 업체 주문만) 또는 ADMIN 만 가능
     */
    public static void checkVendorOrMasterPermission(Role role, String requestCompanyId,
                                                     Shipment shipment) {
        if (role == Role.ADMIN) return;

        if (role == Role.COMPANY) {
            UUID companyId = parseUUID(requestCompanyId);
            if (!shipment.getCompanyId().equals(companyId)) {
                throw new ForbiddenException(ShipmentErrorCode.ACCESS_DENIED);
            }
            return;
        }

        throw new ForbiddenException(ShipmentErrorCode.ACCESS_DENIED);
    }

    private static UUID parseUUID(String value) {
        if (value == null || value.isBlank()) {
            throw new ForbiddenException(ShipmentErrorCode.ACCESS_DENIED);
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new ForbiddenException(ShipmentErrorCode.ACCESS_DENIED);
        }
    }
}
