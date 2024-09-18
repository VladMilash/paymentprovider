package com.mvo.paymentprovider.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RequestDTO {
    private String paymentMethod;
    private BigDecimal amount;
    private String currency;
    private Long cardNumber;
    private String expDate;
    private String cvv;
    private String language;
    private String notificationUrl;
    private String firstName;
    private String lastName;
    private String country;
    private UUID merchantId;
}
