package com.mvo.paymentprovider.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.mvo.paymentprovider.entity.TransactionStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TransactionDTO {

    private Long id;
    private Long cardId;
    private Long accountId;
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
