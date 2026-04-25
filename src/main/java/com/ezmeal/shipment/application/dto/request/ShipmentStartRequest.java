package com.ezmeal.shipment.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record ShipmentStartRequest(

        @NotBlank(message = "송장번호는 필수입니다.")
        String trackingNumber,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime startedAt  // null 이면 서비스에서 현재 시각 사용
) {}
