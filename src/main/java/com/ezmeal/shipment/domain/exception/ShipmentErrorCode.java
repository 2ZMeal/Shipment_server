package com.ezmeal.shipment.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ShipmentErrorCode {

    SHIPMENT_NOT_FOUND("SHIPMENT_001", "해당 주문의 배송 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NOT_PREPARING_STATUS("SHIPMENT_002", "배송 준비 상태(PREPARING)가 아닙니다.", HttpStatus.BAD_REQUEST),
    TRACKING_NUMBER_REQUIRED("SHIPMENT_003", "송장번호는 필수입니다.", HttpStatus.BAD_REQUEST),
    NOT_IN_DELIVERY_STATUS("SHIPMENT_004", "배송 중(IN_DELIVERY) 상태가 아닙니다.", HttpStatus.BAD_REQUEST),
    CANCEL_NOT_ALLOWED("SHIPMENT_005", "배송 시작 이후에는 취소할 수 없습니다.", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED("SHIPMENT_403", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
