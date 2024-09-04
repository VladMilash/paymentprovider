package com.mvo.paymentprovider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("webhook")
public class Webhook {
    @Id
    private Long id;

    @Column("transaction_id")
    private Long transactionId;

    @Column("notification_url")
    private String notificationUrl;

    @Column("attempts")
    private Integer attempts;

    @Column("last_attempt_time")
    private LocalDateTime lastAttemptTime;

    @Column("response_status")
    private String responseStatus;

    @Column("response_body")
    private String responseBody;

    @Column("createdAt")
    private LocalDate createdAt;

    @Column("updatedAt")
    private LocalDate updatedAt;

    @Column("status")
    private Status status;
}
