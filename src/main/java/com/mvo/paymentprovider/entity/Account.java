package com.mvo.paymentprovider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("account")
public class Account {
    @Id
    private UUID id;

    @Column("ownerType")
    private String ownerType;

    @Column("currency")
    private String currency;

    @Column("balance")
    private BigDecimal balance;

    @Column("createdAt")
    private LocalDateTime createdAt;

    @Column("updatedAt")
    private LocalDateTime updatedAt;

    @Column("status")
    private Status status;

    @Column("customer_id")
    private UUID customerId;

    @Column("merchant_id")
    private UUID merchantId;

}
