package com.mvo.paymentprovider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("merchant")
public class Merchant {
    @Id
    private Long id;

    @Column("name")
    private String name;

    @Column("apikey")
    private String apikey;

    @Column("secretKey")
    private String secretKey;

    @Column("createdAt")
    private LocalDate createdAt;

    @Column("updatedAt")
    private LocalDate updatedAt;

    @Column("status")
    private Status status;
}
