package com.mvo.paymentprovider.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.mvo.paymentprovider.entity.Status;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CardDTO {

    private UUID id;
    private UUID accountId;
    private Long cardNumber;
    private String expDate;
    private String cvv;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private Status status;
}
