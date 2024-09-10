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
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("transaction")
public class Transaction {
    @Id
    private UUID id;

    @Column("customer_account_id")
    private UUID customerAccountId;

    @Column("merchant_account_id")
    private UUID merchantAccountId;

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
    private OperationType operationType;

    @Column("createdAt")
    private LocalDateTime createdAt;

    @Column("updatedAt")
    private LocalDateTime updatedAt;

    @Column("status")
    private TransactionStatus transactionStatus;

    @Column("paymentMethod")
    private String paymentMethod;

}
