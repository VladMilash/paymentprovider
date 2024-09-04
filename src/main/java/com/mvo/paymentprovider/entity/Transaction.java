package com.mvo.paymentprovider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("transaction")
public class Transaction {
    @Id
    private Long id;

    @Column("card_id")
    private Long cardId;

    @Column("account_id")
    private Long accountId;

    @Column("amount")
    private BigDecimal amount;

    @Column("currency")
    private String currency;

    @Column("message")
    private String message;

    @Column("notification_url")
    private String notificationUrl;

    @Column("language")
    private String language;

    @Column("operation_type")
    private String operationType;

    @Column("createdAt")
    private LocalDate createdAt;

    @Column("updatedAt")
    private LocalDate updatedAt;

    @Column("status")
    private Status status;

}
