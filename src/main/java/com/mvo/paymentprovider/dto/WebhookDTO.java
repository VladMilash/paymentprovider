package com.mvo.paymentprovider.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.mvo.paymentprovider.entity.Status;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WebhookDTO {

    private Long id;
    private Long transactionId;
    private String notificationUrl;
    private Integer attempts;
    private LocalDateTime lastAttemptTime;
    private String responseStatus;
    private String responseBody;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Status status;
}
