package com.mvo.paymentprovider.repository;

import com.mvo.paymentprovider.entity.OperationType;
import com.mvo.paymentprovider.entity.Transaction;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public interface TransactionRepository extends R2dbcRepository<Transaction, UUID> {
    Flux<Transaction> getTransactionsByCreatedAtBetweenAndOperationTypeAndMerchantAccountId (LocalDateTime startDate, LocalDateTime endDate,
                                                                                                    OperationType operationType,UUID accountMerchantId);

}
