package com.mvo.paymentprovider.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.mvo.paymentprovider.entity.Status;
import lombok.Data;


import java.time.LocalDate;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MerchantDTO {
    private Long id;
    private String name;
    private String apikey;
    private String secretKey;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private Status status;

}
