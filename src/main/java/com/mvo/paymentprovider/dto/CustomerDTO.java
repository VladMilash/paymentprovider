package com.mvo.paymentprovider.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.mvo.paymentprovider.entity.Status;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CustomerDTO {
    private UUID id;
    private String firstname;
    private String lastname;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private Status status;
    private String country;
}
