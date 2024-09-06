package com.mvo.paymentprovider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("card")
public class Card {
    @Id
    private UUID id;

    @Column("account_id")
    private UUID accountId;

    @Column("card_number")
    private Long curdNumber;

    @Column("exp_date")
    private String expDate;

    @Column("cvv")
    private String cvv;

    @Column("createdAt")
    private LocalDateTime createdAt;

    @Column("updatedAt")
    private LocalDateTime updatedAt;

    @Column("status")
    private Status status;

}
