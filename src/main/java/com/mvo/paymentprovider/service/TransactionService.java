package com.mvo.paymentprovider.service;

import com.mvo.paymentprovider.dto.*;
import com.mvo.paymentprovider.entity.OperationType;
import com.mvo.paymentprovider.entity.Transaction;
import com.mvo.paymentprovider.entity.TransactionStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface TransactionService {
    Mono<Transaction> createTransaction(RequestDTO requestDTO);

    Mono<Transaction> updateTransactionStatus(UUID transactionId, TransactionStatus newStatus);

    public Flux<Transaction> getTransactionsByCreatedAtBetween(LocalDate startDate, LocalDate endDate);

    Mono<Transaction> getTransactionDetails(UUID transactionId);


}
