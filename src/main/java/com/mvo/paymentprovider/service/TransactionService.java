package com.mvo.paymentprovider.service;

import com.mvo.paymentprovider.dto.*;
import com.mvo.paymentprovider.entity.OperationType;
import com.mvo.paymentprovider.entity.Transaction;
import com.mvo.paymentprovider.entity.TransactionStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public interface TransactionService {
    Mono<Transaction> createTransaction(RequestDTO requestDTO);

    Mono<Transaction> updateTransactionStatus(UUID transactionId, TransactionStatus newStatus);

    Flux<Transaction> getTransactionsByCreatedAtBetween(LocalDate startDate, LocalDate endDate, UUID merchantID);

    Mono<Transaction> getTransactionDetails(UUID transactionId);


}
