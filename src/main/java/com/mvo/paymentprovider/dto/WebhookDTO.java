package com.mvo.paymentprovider.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.mvo.paymentprovider.entity.Status;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WebhookDTO {

    private UUID id;
    private UUID transactionId;
    private String notificationUrl;
    private Integer attempts;
    private LocalDateTime lastAttemptTime;
    private String responseStatus;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private String status;
}
