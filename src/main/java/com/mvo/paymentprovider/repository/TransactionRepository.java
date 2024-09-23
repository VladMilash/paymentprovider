package com.mvo.paymentprovider.repository;

import com.mvo.paymentprovider.entity.OperationType;
import com.mvo.paymentprovider.entity.Transaction;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public interface TransactionRepository extends R2dbcRepository<Transaction, UUID> {
    @Query("SELECT * FROM transaction " +
            "WHERE createdat >= :startDate " +
            "AND createdat <= :endDate " +
            "AND operation_type = :operationType " +
            "AND merchant_account_id = :accountMerchantId")
    Flux<Transaction> getTransactionsByCreatedAtBetweenAndOperationTypeAndMerchantAccountId(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("operationType") OperationType operationType,
            @Param("accountMerchantId") UUID accountMerchantId
    );

}
