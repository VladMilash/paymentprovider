package com.mvo.paymentprovider.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.mvo.paymentprovider.entity.OperationType;
import com.mvo.paymentprovider.entity.TransactionStatus;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Column;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

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
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private TransactionStatus transactionStatus;
    private String paymentMethod;

}
