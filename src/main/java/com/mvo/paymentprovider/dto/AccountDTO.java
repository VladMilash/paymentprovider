package com.mvo.paymentprovider.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.mvo.paymentprovider.entity.Card;
import com.mvo.paymentprovider.entity.Status;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AccountDTO {

    private UUID id;
    private String ownerType;
    private String currency;
    private BigDecimal balance;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private Status status;
    private UUID customerId;
    private UUID merchantId;
    private List<Card> cards;
}
