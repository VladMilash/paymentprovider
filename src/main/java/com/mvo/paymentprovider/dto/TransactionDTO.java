package com.mvo.paymentprovider.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.mvo.paymentprovider.entity.TransactionStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TransactionDTO {

    private UUID id;
    private UUID cardId;
    private UUID accountId;
    private BigDecimal amount;
    private String currency;
    private String message;
    private String notificationUrl;
    private String language;
    private String operationType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private TransactionStatus transactionStatus;

}
