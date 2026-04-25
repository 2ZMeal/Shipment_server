package com.ezmeal.shipment.domain.exception;

import lombok.Builder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ShipmentException.class)
    public ResponseEntity<ErrorResponse> handleShipmentException(ShipmentException e) {
        ShipmentErrorCode code = e.getErrorCode();
        return ResponseEntity
                .status(code.getHttpStatus())
                .body(ErrorResponse.of(code.getCode(), code.getMessage()));
    }

    @Builder
    public record ErrorResponse(String code, String message, LocalDateTime timestamp) {
        public static ErrorResponse of(String code, String message) {
            return ErrorResponse.builder()
                    .code(code)
                    .message(message)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }
}
