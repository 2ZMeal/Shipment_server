package com.ezmeal.shipment.domain.exception;

import lombok.Getter;

@Getter
public class ShipmentException extends RuntimeException {

    private final ShipmentErrorCode errorCode;

    public ShipmentException(ShipmentErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
