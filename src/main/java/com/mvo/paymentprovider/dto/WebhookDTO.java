package com.mvo.paymentprovider.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;
}
