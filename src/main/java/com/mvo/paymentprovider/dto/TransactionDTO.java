package com.mvo.paymentprovider.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.mvo.paymentprovider.entity.OperationType;
import com.mvo.paymentprovider.entity.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TransactionDTO {
    private UUID id;
    private UUID customerAccountId;
    private UUID merchantAccountId;
    private BigDecimal amount;
    private String currency;
    private String message;
    private String notificationUrl;
    private String language;
    private OperationType operationType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private TransactionStatus transactionStatus;
    private String paymentMethod;

}
