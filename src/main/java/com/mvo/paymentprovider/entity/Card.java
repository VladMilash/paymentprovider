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
@Table("card")
public class Card {
    @Id
    private Long id;

    @Column("account_id")
    private Long accountId;

    @Column("card_number")
    private Long curdNumber;

    @Column("exp_date")
    private String expDate;

    @Column("cvv")
    private String cvv;

    @Column("createdAt")
    private LocalDate createdAt;

    @Column("updatedAt")
    private LocalDate updatedAt;

    @Column("status")
    private Status status;

}
